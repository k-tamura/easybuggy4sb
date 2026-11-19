package org.t246osslab.easybuggy4sb.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Small helper for talking to the local Ollama server.
 *
 * <p>Centralizes what used to be copy-pasted across the LLM controllers: a
 * {@link RestTemplate} with sane, configurable timeouts (so a slow or hung
 * Ollama cannot block a request thread forever), the "is Ollama up?" health
 * check and the generate call.</p>
 *
 * <p>The connect/read timeouts are read from configuration here (rather than in
 * each controller) so that all Ollama calls share one source of truth. A single
 * generation can legitimately take a while, so the read timeout is expected to
 * be generous but bounded.</p>
 */
@Component
public class OllamaSupport {

    private static final Logger log = LoggerFactory.getLogger(OllamaSupport.class);

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

    /** Returns {@code true} if the Ollama server responds with its "Ollama is running" banner. */
    public boolean isRunning(String ollamaUrl) {
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
    public String generate(RestTemplate restTemplate, String ollamaUrl, Map<String, Object> reqBody) {
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
        Map<?, ?> resBody = response.getBody();
        Object output = resBody == null ? null : resBody.get("response");
        return output instanceof String ? (String) output : null;
    }
}
