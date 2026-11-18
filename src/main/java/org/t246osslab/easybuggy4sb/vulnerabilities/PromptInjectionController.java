package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.utils.OllamaSupport;

import java.util.*;

@Controller
public class PromptInjectionController extends AbstractController {

    @Value("${ollama.url}")
    protected String ollamaUrl;

    @Value("${ollama.model}")
    protected String ollamaModel;

    @GetMapping("/promptinjection")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        if (OllamaSupport.isRunning(ollamaUrl)) {
            setViewAndCommonObjects(mav, locale, "promptinjection");
            mav.addObject("isLLMReady", true);
            return mav;
        }
        mav.setViewName("promptinjection");
        mav.addObject("title", msg.getMessage("title.promptinjection.page", null, locale));
        mav.addObject("note", msg.getMessage("msg.note.promptinjection2", null, locale));
        mav.addObject("isLLMReady", false);
        return mav;
    }

    @PostMapping("/translate")
    @ResponseBody
    public Map<String, String> translate(@RequestBody Map<String, String> body, Locale locale) {
        String inputText = body.get("text");
        String targetLanguage = body.get("targetLanguage");
        Map<String, String> result = new HashMap<>();
        RestTemplate restTemplate = OllamaSupport.createRestTemplate();
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", ollamaModel);
        String prompt = "Translate the following text into " + targetLanguage + ": " + inputText;
        reqBody.put("prompt", prompt);
        reqBody.put("stream", false);
        log.debug("Prompt to LLM: {}", prompt);
        String output = null;
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
            Map<?, ?> resBody = response.getBody();
            if (resBody != null && resBody.get("response") instanceof String) {
                output = (String) resBody.get("response");
            }
        } catch (RestClientException e) {
            log.warn("LLM request failed: {}", e.getMessage());
            log.debug("Exception occurs: ", e);
        }
        log.debug("Output from LLM: {}", output);
        result.put("translation", output != null ? output.trim() : msg.getMessage("msg.cant.translate", null, locale));
        return result;
    }
}
