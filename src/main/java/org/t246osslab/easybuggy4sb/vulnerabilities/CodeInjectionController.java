package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CodeInjectionController {

	private static final Logger log = LoggerFactory.getLogger(CodeInjectionController.class);

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/codeijc")
	public ModelAndView process(@RequestParam(value = "jsonString", required = false) String jsonString,
			ModelAndView mav, Locale locale) {
		mav.setViewName("codeinjection");
		mav.addObject("title", msg.getMessage("title.parse.json", null, locale));
        if (!StringUtils.isBlank(jsonString)) {
            String convertedJsonString = jsonString.replaceAll(" ", "");
            convertedJsonString = convertedJsonString.replaceAll("\r\n", "");
            convertedJsonString = convertedJsonString.replaceAll("\n", "");
            parseJson(convertedJsonString, mav, locale);
        } else {
            mav.addObject("msg", msg.getMessage("msg.enter.json.string", null, locale));
        }
		return mav;
	}

    private void parseJson(String jsonString, ModelAndView mav, Locale locale) {
        try {
        	ScriptEngineManager manager = new ScriptEngineManager();
        	ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");
        	scriptEngine.eval("JSON.parse('" + jsonString + "')");
        	mav.addObject("msg", msg.getMessage("msg.valid.json", null, locale));
        } catch (ScriptException e) {
        	mav.addObject("errmsg", msg.getMessage("msg.invalid.json",
        			new String[] { e.getMessage() }, null, locale));
        } catch (Exception e) {
        	log.error("Exception occurs: ", e);
        	mav.addObject("errmsg", msg.getMessage("msg.invalid.json",
        			new String[] { e.getMessage() }, null, locale));
        }
    }
}
