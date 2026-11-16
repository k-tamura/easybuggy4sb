package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI coding assistant with a Denial of Wallet (unbounded consumption) vulnerability.
 * It generates source code in the requested programming languages (one LLM call per language).
 *
 * There is a monthly per-user token limit and a monthly total token limit shared by all users,
 * and the input length is validated on the server side. However, the token limits are checked
 * only once, OUTSIDE the per-language loop, and the number of programming languages in the
 * request is not limited. An attacker who spoofs the language count on the client side (for
 * example 1000 languages) therefore exceeds the limits in a single request, even when the input
 * text is short.
 *
 * The LLM runs locally, so the billing is simulated from the token counts that Ollama returns.
 */
@Controller
public class DenialOfWalletController extends AbstractController {

    /* Default maximum number of output tokens. The browser sends this value and the server trusts it. */
    private static final int DEFAULT_MAX_TOKENS = 256;

    @Value("${ollama.url}")
    protected String ollamaUrl;

    @Value("${ollama.model}")
    protected String ollamaModel;

    /* Prices in US dollars per million tokens ($/MTok = micro-dollars per token) */
    @Value("${dow.price.input.per.mtok:3}")
    protected double inputPricePerMTok;

    @Value("${dow.price.output.per.mtok:15}")
    protected double outputPricePerMTok;

    /* Maximum number of tokens (input + output) a single user is allowed to consume per month. */
    @Value("${dow.user.monthly.token.limit:5000}")
    protected long userMonthlyTokenLimit;

    /* Maximum number of tokens (input + output) all users combined are allowed to consume per month. */
    @Value("${dow.total.monthly.token.limit:20000}")
    protected long totalMonthlyTokenLimit;

    /* Maximum number of characters allowed for the input (validated on the server side). */
    @Value("${dow.max.input.chars:500}")
    protected int maxInputChars;

    /* Token usage per user. The per-user token limit is enforced from this. */
    private final Map<String, Usage> usageByUser = new ConcurrentHashMap<>();

    /* Tokens (input + output) consumed by all users combined, for the total token limit. */
    private final AtomicLong totalTokensUsed = new AtomicLong();

    private static class Usage {
        final AtomicLong inputTokens = new AtomicLong();
        final AtomicLong outputTokens = new AtomicLong();
        final AtomicLong calls = new AtomicLong();
        final AtomicLong microDollars = new AtomicLong();

        long totalTokens() {
            return inputTokens.get() + outputTokens.get();
        }
    }

    @GetMapping("/admins/denialofwallet")
    public ModelAndView index(ModelAndView mav, HttpSession session, Locale locale) {
        boolean isLLMReady = isOllamaRunning();
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

    @PostMapping("/admins/denialofwallet/ask")
    @ResponseBody
    public Map<String, Object> ask(@RequestBody Map<String, Object> body, HttpSession session, Locale locale) {
        Map<String, Object> result = new HashMap<>();
        Object text = body.get("text");
        Object languages = body.get("languages");
        Object maxTokens = body.get("maxTokens");
        String requirement = text instanceof String ? (String) text : null;
        List<?> languageList = languages instanceof List ? (List<?>) languages : Collections.emptyList();
        int numPredict = maxTokens instanceof Number ? ((Number) maxTokens).intValue() : DEFAULT_MAX_TOKENS;

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

        /*
         * Vulnerable: the monthly token limits are checked only once here, OUTSIDE the loop, and the
         * number of programming languages in the request is not limited. Because one LLM call is made
         * per language, a single request with many (client-spoofed) languages runs the whole loop and
         * blows far past the limits, even when the input text is short. The checks belong inside the
         * loop, and the number of languages should be capped on the server side.
         */
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

        RestTemplate restTemplate = createRestTemplate();
        List<Map<String, String>> answers = new ArrayList<>();
        long requestCost = 0;
        for (Object language : languageList) {
            try {
                Map<?, ?> res = generate(restTemplate, requirement, String.valueOf(language), numPredict);
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

    private Map<?, ?> generate(RestTemplate restTemplate, String requirement, String language, int numPredict) {
        Map<String, Object> options = new HashMap<>();
        options.put("num_ctx", 8192);
        options.put("num_predict", numPredict);
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", ollamaModel);
        reqBody.put("system", "You are a coding assistant for EasyBuggy Boot, "
                + "a deliberately vulnerable web application for learning about bugs and vulnerabilities.");
        reqBody.put("prompt", "Write source code in " + language
                + " that performs the following. Output only the source code.\n\n" + requirement);
        reqBody.put("options", options);
        reqBody.put("stream", false);
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
        Map<?, ?> responseBody = response.getBody();
        return responseBody != null ? responseBody : Collections.emptyMap();
    }

    /* Adds one LLM call to the user's usage and the shared total, and returns its simulated cost. */
    private long charge(Usage usage, long promptTokens, long completionTokens) {
        long cost = Math.round(promptTokens * inputPricePerMTok + completionTokens * outputPricePerMTok);
        usage.inputTokens.addAndGet(promptTokens);
        usage.outputTokens.addAndGet(completionTokens);
        usage.calls.incrementAndGet();
        usage.microDollars.addAndGet(cost);
        totalTokensUsed.addAndGet(promptTokens + completionTokens);
        return cost;
    }

    private boolean isUserLimitExceeded(Usage usage) {
        return usage.totalTokens() >= userMonthlyTokenLimit;
    }

    private boolean isTotalLimitExceeded() {
        return totalTokensUsed.get() >= totalMonthlyTokenLimit;
    }

    private Map<String, Object> getUsage(Usage usage) {
        long used = usage.totalTokens();
        long total = totalTokensUsed.get();
        Map<String, Object> m = new HashMap<>();
        m.put("cost", formatDollars(usage.microDollars.get()));
        m.put("tokensUsed", used);
        m.put("tokenLimit", userMonthlyTokenLimit);
        m.put("totalTokensUsed", total);
        m.put("totalTokenLimit", totalMonthlyTokenLimit);
        m.put("maxInputChars", maxInputChars);
        m.put("percent", userMonthlyTokenLimit > 0 ? Math.min(100.0, used * 100.0 / userMonthlyTokenLimit) : 100.0);
        m.put("totalPercent", totalMonthlyTokenLimit > 0 ? Math.min(100.0, total * 100.0 / totalMonthlyTokenLimit) : 100.0);
        m.put("exhausted", isUserLimitExceeded(usage) || isTotalLimitExceeded());
        m.put("calls", usage.calls.get());
        m.put("inputTokens", usage.inputTokens.get());
        m.put("outputTokens", usage.outputTokens.get());
        m.put("inputPrice", String.format(Locale.ROOT, "$%.2f", inputPricePerMTok));
        m.put("outputPrice", String.format(Locale.ROOT, "$%.2f", outputPricePerMTok));
        return m;
    }

    private boolean isOllamaRunning() {
        try {
            ResponseEntity<String> response = createRestTemplate().getForEntity(ollamaUrl, String.class);
            return HttpStatus.SC_OK == response.getStatusCode().value() && "Ollama is running".equals(response.getBody());
        } catch (Exception e) {
            log.info("Ollama is not running: {}", e.getMessage());
            log.debug("Exception occurs: ", e);
            return false;
        }
    }

    /* Timeouts keep a single LLM call from blocking forever (e.g. when maxTokens is -1). */
    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);
        factory.setReadTimeout(180_000);
        return new RestTemplate(factory);
    }

    private static String formatDollars(long microDollars) {
        return String.format(Locale.ROOT, "$%.4f", microDollars / 1_000_000.0);
    }

    private static long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}
