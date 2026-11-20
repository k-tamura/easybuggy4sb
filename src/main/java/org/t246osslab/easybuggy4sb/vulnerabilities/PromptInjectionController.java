package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.utils.OllamaSupport;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class PromptInjectionController extends AbstractController {

    @Autowired
    protected OllamaSupport ollamaSupport;

    @GetMapping("/promptinjection")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        boolean isLLMReady = ollamaSupport.isRunning();
        setViewAndCommonObjects(mav, locale, "promptinjection");
        if (!isLLMReady) {
            mav.addObject("note", msg.getMessage("msg.note.promptinjection2", null, locale));
        }
        mav.addObject("isLLMReady", isLLMReady);
        return mav;
    }

    @PostMapping("/translate")
    @ResponseBody
    public Map<String, String> translate(@RequestBody Map<String, String> body, Locale locale) {
        String prompt = "Translate the following text into " + body.get("targetLanguage") + ": " + body.get("text");
        log.debug("Prompt to LLM: {}", prompt);
        Map<String, Object> reqBody = ollamaSupport.newRequest();
        reqBody.put("prompt", prompt);
        Map<String, String> result = new HashMap<>();
        result.put("translation", ollamaSupport.generateText(reqBody, msg.getMessage("msg.cant.translate", null, locale)));
        return result;
    }
}
