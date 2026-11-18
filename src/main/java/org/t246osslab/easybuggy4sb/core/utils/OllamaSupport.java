package org.t246osslab.easybuggy4sb.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Small helper for talking to the local Ollama server.
 *
 * <p>Centralizes two things that used to be copy-pasted across the LLM
 * controllers: a {@link RestTemplate} with sane timeouts (so a slow or hung
 * Ollama cannot block a request thread forever) and the "is Ollama up?" health
 * check.</p>
 */
public final class OllamaSupport {

    private static final Logger log = LoggerFactory.getLogger(OllamaSupport.class);

    /* A single LLM call can legitimately take a while; keep the read timeout generous but bounded. */
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 180_000;

    private OllamaSupport() {
        throw new IllegalAccessError("Utility class");
    }

    /** Creates a {@link RestTemplate} with connect/read timeouts so a hung Ollama cannot block forever. */
    public static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        factory.setReadTimeout(READ_TIMEOUT_MS);
        return new RestTemplate(factory);
    }

    /** Returns {@code true} if the Ollama server responds with its "Ollama is running" banner. */
    public static boolean isRunning(String ollamaUrl) {
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
     * Posts {@code reqBody} to Ollama's {@code /api/generate} endpoint and returns the generated
     * text (the {@code response} field), or {@code null} if the server returned no usable text.
     */
    public static String generate(RestTemplate restTemplate, String ollamaUrl, Map<String, Object> reqBody) {
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
        Map<?, ?> resBody = response.getBody();
        Object output = resBody == null ? null : resBody.get("response");
        return output instanceof String ? (String) output : null;
    }
}
