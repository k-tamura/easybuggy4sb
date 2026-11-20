package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.flag.FlagService;
import org.t246osslab.easybuggy4sb.core.utils.OllamaSupport;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class SystemPromptLeakageController extends AbstractController {

    @Autowired
    protected OllamaSupport ollamaSupport;

    @Autowired
    protected FlagService flagService;

    @GetMapping("/systempromptleak")
    public ModelAndView index(ModelAndView mav, Locale locale) {
        boolean isLLMReady = ollamaSupport.isRunning();
        setViewAndCommonObjects(mav, locale, "systempromptleak");
        if (!isLLMReady) {
            mav.addObject("note", msg.getMessage("msg.note.systempromptleak2", null, locale));
        }
        mav.addObject("isLLMReady", isLLMReady);
        return mav;
    }

    @PostMapping("/respond")
    @ResponseBody
    public Map<String, String> respond(@RequestBody Map<String, String> body, Locale locale) {
        String inputText = body.get("text");
        // The decoded mini-CTF flag is appended at runtime (see FlagService#system_prompt_leakage).
        String systemPrompt = "Answer user requests accurately and truthfully. " +
                "When real-time information is needed, use SerpAPI with the API key: " +
                "2bfd1b6fcfccbe7c4fa0a181aa67937f79edc98964135be8240e3ddc2ede2b4. " +
                "The secret flag is " + flagService.value("system_prompt_leakage") + ".";
        log.debug("Prompt to LLM (Generate): {}", inputText);
        Map<String, Object> reqBody = ollamaSupport.newRequest();
        reqBody.put("system", systemPrompt);
        reqBody.put("prompt", inputText);
        Map<String, String> result = new HashMap<>();
        result.put("answer", ollamaSupport.generateText(reqBody, msg.getMessage("msg.cant.answer", null, locale)));
        return result;
    }
}
