package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.flag.FlagService;
import org.t246osslab.easybuggy4sb.core.utils.OllamaSupport;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class DenialOfWalletController extends AbstractController {

    @Autowired
    protected OllamaSupport ollamaSupport;

    /* Prices in US dollars per million tokens ($/MTok = micro-dollars per token) */
    @Value("${dow.price.input.per.mtok:3}")
    protected double inputPricePerMTok;

    @Value("${dow.price.output.per.mtok:15}")
    protected double outputPricePerMTok;

    /* Maximum number of tokens (input + output) a single user is allowed to consume per month. */
    @Value("${dow.user.monthly.token.limit:5000}")
    protected long userMonthlyTokenLimit;

    /* Hard limit on the tokens (input + output) all users combined may consume per month. Reaching
     * it stops the feature for everyone, so that the simulated bill cannot grow without bound. */
    @Value("${dow.total.monthly.token.limit:100000}")
    protected long totalMonthlyTokenLimit;

    /* Shared monthly free tier (tokens for all users combined). Usage within this tier is free; only
     * the tokens beyond it are billed. The operational goal is to keep the all-users total under this. */
    @Value("${dow.total.free.token.limit:50000}")
    protected long totalFreeTokenLimit;

    /* Maximum number of characters allowed for the input (validated on the server side). */
    @Value("${dow.max.input.chars:500}")
    protected int maxInputChars;

    /* Mini-CTF flag revealed once the shared free tier is exceeded (i.e. the attack has succeeded). */
    @Autowired
    protected FlagService flagService;

    /* Token usage per user. The per-user token limit is enforced from this. */
    private final Map<String, Usage> usageByUser = new ConcurrentHashMap<>();

    /* Tokens (input + output) consumed by all users combined, for the total token limit. */
    private final AtomicLong totalTokensUsed = new AtomicLong();

    /* Simulated billing cost (micro-dollars) for all users combined: the overage beyond the free tier.
     * The cost is system-wide, not per user, so it is tracked here rather than on each Usage. */
    private final AtomicLong totalMicroDollars = new AtomicLong();

    private static class Usage {
        final AtomicLong inputTokens = new AtomicLong();
        final AtomicLong outputTokens = new AtomicLong();
        final AtomicLong calls = new AtomicLong();

        long totalTokens() {
            return inputTokens.get() + outputTokens.get();
        }
    }

    @GetMapping("/admins/denialofwallet")
    public ModelAndView index(ModelAndView mav, HttpSession session, Locale locale) {
        boolean isLLMReady = ollamaSupport.isRunning();
        setViewAndCommonObjects(mav, locale, "denialofwallet");
        Usage usage = usageFor(session);
        if (!isLLMReady) {
            mav.addObject("note", msg.getMessage("msg.note.denialofwallet2", null, locale));
        } else if (isTotalLimitExceeded()) {
            mav.addObject("errmsg", msg.getMessage("msg.total.token.limit.exceeded", null, locale));
        } else if (isUserLimitExceeded(usage)) {
            mav.addObject("errmsg", msg.getMessage("msg.user.token.limit.exceeded", null, locale));
        }
        mav.addObject("isLLMReady", isLLMReady);
        mav.addObject("usage", getUsage(usage));
        return mav;
    }

    /* Clears the in-memory AI usage (per-user and all-users totals) so the monthly figures start over.
     * The usage is kept only in memory, so without this it survives until the process is restarted.
     * This is an operator/teaching control: the button is shown only inside the (hint) info box. */
    @PostMapping("/admins/denialofwallet/reset")
    @ResponseBody
    public Map<String, Object> reset() {
        usageByUser.clear();
        totalTokensUsed.set(0);
        totalMicroDollars.set(0);
        log.info("Denial of Wallet: AI usage has been reset");
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        return result;
    }

    @PostMapping("/admins/denialofwallet/ask")
    @ResponseBody
    public Map<String, Object> ask(@RequestBody Map<String, Object> body, HttpSession session, Locale locale) {
        Map<String, Object> result = new HashMap<>();
        Object text = body.get("text");
        Object languages = body.get("languages");
        String requirement = text instanceof String ? (String) text : null;
        List<?> languageList = languages instanceof List ? (List<?>) languages : Collections.emptyList();

        Usage usage = usageFor(session);

        if (StringUtils.isBlank(requirement) || languageList.isEmpty()) {
            result.put("error", msg.getMessage("msg.select.language", null, locale));
            result.put("usage", getUsage(usage));
            return result;
        }

        /* The input length IS validated on the server side, so it cannot be used to amplify the cost. */
        if (requirement.length() > maxInputChars) {
            result.put("error", msg.getMessage("msg.input.too.long", new Object[]{maxInputChars}, locale));
            result.put("usage", getUsage(usage));
            return result;
        }

        if (isTotalLimitExceeded()) {
            result.put("error", msg.getMessage("msg.total.token.limit.exceeded", null, locale));
            result.put("usage", getUsage(usage));
            return result;
        }
        if (isUserLimitExceeded(usage)) {
            result.put("error", msg.getMessage("msg.user.token.limit.exceeded", null, locale));
            result.put("usage", getUsage(usage));
            return result;
        }

        RestTemplate restTemplate = ollamaSupport.createRestTemplate();
        List<Map<String, String>> answers = new ArrayList<>();
        long requestCost = 0;
        for (Object language : languageList) {
            try {
                Map<?, ?> res = generate(restTemplate, requirement, String.valueOf(language));
                requestCost += charge(usage, toLong(res.get("prompt_eval_count")), toLong(res.get("eval_count")));
                Object output = res.get("response");
                Map<String, String> answer = new HashMap<>();
                answer.put("language", String.valueOf(language));
                answer.put("answer", output instanceof String ? ((String) output).trim()
                        : msg.getMessage("msg.cannot.answer", null, locale));
                answers.add(answer);
            } catch (RestClientException e) {
                log.warn("LLM request failed: {}", e.getMessage());
                log.debug("Exception occurs: ", e);
                result.put("error", msg.getMessage("msg.cannot.answer", null, locale));
                break;
            }
        }
        log.info("Denial of Wallet: {} LLM call(s), {} token(s) by this user (limit {}), {} total (limit {})",
                answers.size(), usage.totalTokens(), userMonthlyTokenLimit,
                totalTokensUsed.get(), totalMonthlyTokenLimit);
        result.put("answers", answers);
        result.put("requestCost", formatDollars(requestCost));
        result.put("usage", getUsage(usage));
        return result;
    }

    private Usage usageFor(HttpSession session) {
        Object userId = session == null ? null : session.getAttribute("userid");
        return usageByUser.computeIfAbsent(userId == null ? "" : String.valueOf(userId), k -> new Usage());
    }

    private Map<?, ?> generate(RestTemplate restTemplate, String requirement, String language) {
        Map<String, Object> options = new HashMap<>();
        options.put("num_ctx", 8192);
        /* The MONTHLY per-user limit is mistakenly used as the per-call output limit. It is far too large for
         * this use case (a short code snippet needs a few hundred tokens), so crafted input can inflate the cost. */
        options.put("num_predict", userMonthlyTokenLimit);
        Map<String, Object> reqBody = ollamaSupport.newRequest();
        reqBody.put("system", "You are a coding assistant for EasyBuggy Boot, "
                + "a deliberately vulnerable web application for learning about bugs and vulnerabilities.");
        reqBody.put("prompt", "Write source code in " + language
                + " that performs the following. Output only the source code.\n\n" + requirement);
        reqBody.put("options", options);
        return ollamaSupport.generate(restTemplate, reqBody);
    }

    /* Adds one LLM call to the user's usage and the shared total, and returns its simulated cost.
     * Only tokens beyond the shared free tier are billed; usage within the free tier costs nothing. */
    private long charge(Usage usage, long promptTokens, long completionTokens) {
        long startTotal = totalTokensUsed.getAndAdd(promptTokens + completionTokens);
        long billablePrompt = billableTokens(startTotal, promptTokens);
        long billableCompletion = billableTokens(startTotal + promptTokens, completionTokens);
        long cost = Math.round(billablePrompt * inputPricePerMTok + billableCompletion * outputPricePerMTok);
        usage.inputTokens.addAndGet(promptTokens);
        usage.outputTokens.addAndGet(completionTokens);
        usage.calls.incrementAndGet();
        totalMicroDollars.addAndGet(cost);
        return cost;
    }

    /* Of the {@code count} tokens occupying the shared-total range [start, start + count), returns how
     * many fall beyond the free tier (i.e. are billable). Tokens within the free tier are not billed. */
    private long billableTokens(long start, long count) {
        long end = start + count;
        long billableStart = Math.max(start, totalFreeTokenLimit);
        return Math.max(0, end - billableStart);
    }

    private boolean isUserLimitExceeded(Usage usage) {
        return usage.totalTokens() >= userMonthlyTokenLimit;
    }

    private boolean isTotalLimitExceeded() {
        return totalTokensUsed.get() >= totalMonthlyTokenLimit;
    }

    /* True once the shared free tier is used up: usage keeps working up to the hard limit, but is billed. */
    private boolean isFreeTierExceeded() {
        return totalTokensUsed.get() >= totalFreeTokenLimit;
    }

    private Map<String, Object> getUsage(Usage usage) {
        long used = usage.totalTokens();
        long total = totalTokensUsed.get();
        long hardCap = totalMonthlyTokenLimit;
        long freeLimit = totalFreeTokenLimit;
        long freePart = Math.min(total, freeLimit);
        long overagePart = Math.min(total, hardCap) - freePart; // 0 while still within the free tier
        Map<String, Object> m = new HashMap<>();
        m.put("cost", formatDollars(totalMicroDollars.get()));
        m.put("tokensUsed", used);
        m.put("tokenLimit", userMonthlyTokenLimit);
        m.put("totalTokensUsed", total);
        m.put("totalTokenLimit", hardCap);
        m.put("freeTokenLimit", freeLimit);
        m.put("overageTokens", Math.max(0, total - freeLimit));
        boolean billed = total > freeLimit; // overage charge has been incurred -> the attack succeeded
        m.put("billed", billed);
        m.put("flag", billed ? flagService.value("denial_of_wallet") : null);
        m.put("maxInputChars", maxInputChars);
        m.put("percent", userMonthlyTokenLimit > 0 ? Math.min(100.0, used * 100.0 / userMonthlyTokenLimit) : 100.0);
        // The all-users bar is scaled to the hard cap; the free tier is its first freeLimit/hardCap portion.
        m.put("freeBarPercent", hardCap > 0 ? freePart * 100.0 / hardCap : 0.0);
        m.put("overageBarPercent", hardCap > 0 ? overagePart * 100.0 / hardCap : 0.0);
        m.put("userExceeded", isUserLimitExceeded(usage));
        m.put("totalExceeded", isTotalLimitExceeded());
        m.put("overFreeTier", isFreeTierExceeded());
        m.put("exhausted", isUserLimitExceeded(usage) || isTotalLimitExceeded());
        m.put("calls", usage.calls.get());
        m.put("inputTokens", usage.inputTokens.get());
        m.put("outputTokens", usage.outputTokens.get());
        m.put("inputPrice", String.format(Locale.ROOT, "$%.2f", inputPricePerMTok));
        m.put("outputPrice", String.format(Locale.ROOT, "$%.2f", outputPricePerMTok));
        return m;
    }

    private static String formatDollars(long microDollars) {
        return String.format(Locale.ROOT, "$%.4f", microDollars / 1_000_000.0);
    }

    private static long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}
