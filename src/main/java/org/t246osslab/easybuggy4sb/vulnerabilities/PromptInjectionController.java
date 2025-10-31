package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class PromptInjectionController extends AbstractController {

    @Value("${ollama.url}")
    protected String ollamaUrl;

    @Value("${ollama.model}")
    protected String ollamaModel;

    @GetMapping("/promptinjection")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(ollamaUrl, String.class);
            if (HttpStatus.SC_OK == response.getStatusCode().value() && "Ollama is running".equals(response.getBody())){
                setViewAndCommonObjects(mav, locale, "promptinjection");
                mav.addObject("isLLMReady", true);
                return mav;
            }
        } catch (Exception e) {
            log.info("Ollama is not running: {}", e.getMessage());
            log.debug("Exception occurs: ", e);
        }
        mav.setViewName("promptinjection");
        mav.addObject("title", msg.getMessage("title.promptinjection.page", null, locale));
        mav.addObject("note", msg.getMessage("msg.note.promptinjection2", null, locale));
        mav.addObject("isLLMReady", false);
        return mav;
    }

    @PostMapping("/translate")
    @ResponseBody
    public Map<String, String> translate(@RequestBody Map<String, String> body) {
        String inputText = body.get("text");
        String targetLanguage = body.get("targetLanguage");
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", ollamaModel);
        String prompt = "Translate this text into " + targetLanguage + ": " + inputText;
        log.info("Prompt to LLM: {}", prompt);
        reqBody.put("prompt", prompt);
        reqBody.put("stream", false);
        ResponseEntity<Map> response = restTemplate.postForEntity(ollamaUrl + "/api/generate", reqBody, Map.class);
        String output = (String) response.getBody().get("response");
        Map<String, String> result = new HashMap<>();
        result.put("translation", output != null ? output.trim() : "(no response)");
        return result;
    }
}
