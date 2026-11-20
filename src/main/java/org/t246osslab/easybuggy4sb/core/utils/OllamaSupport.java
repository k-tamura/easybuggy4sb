package org.t246osslab.easybuggy4sb.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Small helper for talking to the local Ollama server.
 *
 * <p>Centralizes what used to be copy-pasted across the LLM controllers: the
 * Ollama URL and model name, a {@link RestTemplate} with sane, configurable
 * timeouts (so a slow or hung Ollama cannot block a request thread forever),
 * the "is Ollama up?" health check and the generate call.</p>
 *
 * <p>The URL, model and connect/read timeouts are read from configuration here
 * (rather than in each controller) so that all Ollama calls share one source of
 * truth. A single generation can legitimately take a while, so the read timeout
 * is expected to be generous but bounded.</p>
 */
@Component
public class OllamaSupport {

    private static final Logger log = LoggerFactory.getLogger(OllamaSupport.class);

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    @Value("${ollama.connect.timeout.ms:5000}")
    private int connectTimeoutMs;

    @Value("${ollama.read.timeout.ms:300000}")
    private int readTimeoutMs;

    /** Creates a {@link RestTemplate} with the configured connect/read timeouts. */
    public RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    /**
     * Returns a new, non-streaming request body pre-populated with the configured model. Callers add
     * their own {@code prompt}, {@code system} and {@code options} entries.
     */
    public Map<String, Object> newRequest() {
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", ollamaModel);
        reqBody.put("stream", false);
        return reqBody;
    }

    /** Returns {@code true} if the Ollama server responds with its "Ollama is running" banner. */
    public boolean isRunning() {
        try {
            ResponseEntity<String> response = createRestTemplate().getForEntity(ollamaUrl, String.class);
            return HttpStatus.OK == response.getStatusCode()
                    && "Ollama is running".equals(response.getBody());
        } catch (Exception e) {
            log.info("Ollama is not running: {}", e.getMessage());
            log.debug("Exception occurs: ", e);
            return false;
        }
    }

    /**
     * Posts {@code reqBody} to Ollama's {@code /api/generate} endpoint and returns the full response
     * body (which includes {@code response}, {@code prompt_eval_count}, {@code eval_count}, ...), never
     * {@code null}. The caller supplies the {@link RestTemplate} so it can be reused across calls.
     */
    public Map<?, ?> generate(RestTemplate restTemplate, Map<String, Object> reqBody) {
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
        Map<?, ?> resBody = response.getBody();
        return resBody != null ? resBody : Collections.emptyMap();
    }

    /**
     * Convenience for simple single-shot generations: builds a {@link RestTemplate}, posts {@code reqBody}
     * and returns the trimmed generated text. If the request fails or yields no text, {@code fallback} is
     * returned instead (the failure is logged, not thrown).
     */
    public String generateText(Map<String, Object> reqBody, String fallback) {
        try {
            Object output = generate(createRestTemplate(), reqBody).get("response");
            log.debug("Output from LLM: {}", output);
            return output instanceof String ? ((String) output).trim() : fallback;
        } catch (RestClientException e) {
            log.warn("LLM request failed: {}", e.getMessage());
            log.debug("Exception occurs: ", e);
            return fallback;
        }
    }
}
