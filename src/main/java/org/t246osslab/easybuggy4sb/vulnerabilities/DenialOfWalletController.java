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

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI support assistant with a Denial of Wallet (unbounded consumption) vulnerability.
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

    @Value("${dow.monthly.budget:10}")
    protected double monthlyBudget;

    /* Usage shared by all users (in micro-dollars) */
    private final AtomicLong usedMicroDollars = new AtomicLong();
    private final AtomicLong inputTokens = new AtomicLong();
    private final AtomicLong outputTokens = new AtomicLong();
    private final AtomicLong llmCalls = new AtomicLong();

    @GetMapping("/denialofwallet")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        boolean isLLMReady = isOllamaRunning();
        setViewAndCommonObjects(mav, locale, "denialofwallet");
        if (!isLLMReady) {
            mav.addObject("note", msg.getMessage("msg.note.denialofwallet2", null, locale));
        } else if (isBudgetExhausted()) {
            mav.addObject("errmsg", msg.getMessage("msg.ai.budget.exhausted", null, locale));
        }
        mav.addObject("isLLMReady", isLLMReady);
        mav.addObject("usage", getUsage());
        return mav;
    }

    @PostMapping("/denialofwallet/ask")
    @ResponseBody
    public Map<String, Object> ask(@RequestBody Map<String, Object> body, Locale locale) {
        Map<String, Object> result = new HashMap<>();
        Object text = body.get("text");
        Object languages = body.get("languages");
        Object maxTokens = body.get("maxTokens");
        String question = text instanceof String ? (String) text : null;
        List<?> languageList = languages instanceof List ? (List<?>) languages : Collections.emptyList();
        int numPredict = maxTokens instanceof Number ? ((Number) maxTokens).intValue() : DEFAULT_MAX_TOKENS;

        if (StringUtils.isBlank(question) || languageList.isEmpty()) {
            result.put("error", msg.getMessage("msg.select.language", null, locale));
            result.put("usage", getUsage());
            return result;
        }

        /*
         * Vulnerable: no authentication, no rate limiting, and no limits on the input length,
         * the number of languages (one LLM call per language), or the number of output tokens.
         */
        RestTemplate restTemplate = createRestTemplate();
        List<Map<String, String>> answers = new ArrayList<>();
        long requestCost = 0;
        for (Object language : languageList) {
            if (isBudgetExhausted()) {
                result.put("error", msg.getMessage("msg.ai.budget.exhausted", null, locale));
                break;
            }
            try {
                Map<?, ?> res = generate(restTemplate, question, String.valueOf(language), numPredict);
                requestCost += charge(toLong(res.get("prompt_eval_count")), toLong(res.get("eval_count")));
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
        log.info("Denial of Wallet: {} LLM call(s), cost of this request: {}, total: {}",
                answers.size(), formatDollars(requestCost), formatDollars(usedMicroDollars.get()));
        result.put("answers", answers);
        result.put("requestCost", formatDollars(requestCost));
        result.put("usage", getUsage());
        return result;
    }

    @PostMapping("/denialofwallet/reset")
    @ResponseBody
    public Map<String, Object> reset() {
        usedMicroDollars.set(0);
        inputTokens.set(0);
        outputTokens.set(0);
        llmCalls.set(0);
        return getUsage();
    }

    private Map<?, ?> generate(RestTemplate restTemplate, String question, String language, int numPredict) {
        Map<String, Object> options = new HashMap<>();
        options.put("num_ctx", 8192);
        options.put("num_predict", numPredict);
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", ollamaModel);
        reqBody.put("system", "You are a support assistant for EasyBuggy Boot, "
                + "a deliberately vulnerable web application for learning about bugs and vulnerabilities.");
        reqBody.put("prompt", "Answer the following question in " + language + ".\n\n" + question);
        reqBody.put("options", options);
        reqBody.put("stream", false);
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
        Map<?, ?> responseBody = response.getBody();
        return responseBody != null ? responseBody : Collections.emptyMap();
    }

    /* Adds the simulated cost of one LLM call to the shared usage and returns it in micro-dollars. */
    private long charge(long promptTokens, long completionTokens) {
        long cost = Math.round(promptTokens * inputPricePerMTok + completionTokens * outputPricePerMTok);
        inputTokens.addAndGet(promptTokens);
        outputTokens.addAndGet(completionTokens);
        llmCalls.incrementAndGet();
        usedMicroDollars.addAndGet(cost);
        return cost;
    }

    private boolean isBudgetExhausted() {
        return usedMicroDollars.get() >= Math.round(monthlyBudget * 1_000_000);
    }

    private Map<String, Object> getUsage() {
        long used = usedMicroDollars.get();
        long budget = Math.round(monthlyBudget * 1_000_000);
        Map<String, Object> usage = new HashMap<>();
        usage.put("cost", formatDollars(used));
        usage.put("budget", String.format(Locale.ROOT, "$%.2f", monthlyBudget));
        usage.put("percent", budget > 0 ? Math.min(100.0, used * 100.0 / budget) : 100.0);
        usage.put("exhausted", isBudgetExhausted());
        usage.put("calls", llmCalls.get());
        usage.put("inputTokens", inputTokens.get());
        usage.put("outputTokens", outputTokens.get());
        usage.put("inputPrice", String.format(Locale.ROOT, "$%.2f", inputPricePerMTok));
        usage.put("outputPrice", String.format(Locale.ROOT, "$%.2f", outputPricePerMTok));
        return usage;
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
