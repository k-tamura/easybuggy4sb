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
public class SystemPromptLeakageController extends AbstractController {

    @Value("${ollama.url}")
    protected String ollamaUrl;

    @Value("${ollama.model}")
    protected String ollamaModel;

    @GetMapping("/systempromptleak")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(ollamaUrl, String.class);
            if (HttpStatus.SC_OK == response.getStatusCode().value() && "Ollama is running".equals(response.getBody())) {
                setViewAndCommonObjects(mav, locale, "systempromptleak");
                mav.addObject("isLLMReady", true);
                return mav;
            }
        } catch (Exception e) {
            log.info("Ollama is not running: {}", e.getMessage());
            log.debug("Exception occurs: ", e);
        }
        mav.setViewName("systempromptleak");
        mav.addObject("title", msg.getMessage("title.systempromptleak.page", null, locale));
        mav.addObject("note", msg.getMessage("msg.note.systempromptleak2", null, locale));
        mav.addObject("isLLMReady", false);
        return mav;
    }

    @PostMapping("/respond")
    @ResponseBody
    public Map<String, String> respond(@RequestBody Map<String, String> body, Locale locale) {
        String inputText = body.get("text");
        Map<String, String> result = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> reqBody = new HashMap<>();
        reqBody.put("model", ollamaModel);
        reqBody.put("stream", false);

        String systemPrompt = "Answer user requests accurately and truthfully. " +
                "When real-time information is needed, use SerpAPI with the API key: " +
                "2bfd1b6fcfccbe7c4fa0a181aa67937f79edc98964135be8240e3ddc2ede2b4";
        reqBody.put("system", systemPrompt);
        reqBody.put("prompt", inputText);

        log.info("Prompt to LLM (Generate): {}", inputText);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                ollamaUrl + "/api/generate", reqBody, Map.class);
        String output = (String) response.getBody().get("response");
        log.info("Output from LLM (Generate): {}", output);
        result.put("answer", output != null ? output.trim() :
                msg.getMessage("msg.cannot.translate", null, locale));
        return result;
    }
}
