package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class CodeInjectionController extends AbstractController {

	@RequestMapping(value = "/codeijc")
	public ModelAndView process(@RequestParam(value = "jsonString", required = false) String jsonString,
			ModelAndView mav, Locale locale) {
		setViewAndCommonObjects(mav, locale, "codeinjection");
        if (!StringUtils.isBlank(jsonString)) {
            parseJson(jsonString, mav, locale);
			mav.addObject("jsonString", jsonString);
        } else {
            mav.addObject("msg", msg.getMessage("msg.enter.json.string", null, locale));
        }
		return mav;
	}

    private void parseJson(String jsonString, ModelAndView mav, Locale locale) {
        /* Escape to parse as JSON */
		String escapedJsonString = jsonString
				.replace("\\", "\\\\")
				.replace("\n", "\\n")
				.replace("\r", "\\r");
        try {
            /* Parse the input string as JSON */
        	ScriptEngineManager manager = new ScriptEngineManager();
        	ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");
        	scriptEngine.eval("JSON.parse('" + escapedJsonString + "')");
        	mav.addObject("msg", msg.getMessage("msg.valid.json", null, locale));
        } catch (ScriptException e) {
			mav.addObject("errmsg", msg.getMessage("msg.invalid.json", null, locale));
			mav.addObject("detailmsg", e.getMessage()
					.replaceAll(":([0-9]+):([0-9]+)", " at line $1, column $2.")
					.replaceAll(" in <eval> at line number 1", ""));
        } catch (Exception e) {
        	log.error("Exception occurs: ", e);
        	mav.addObject("errmsg", msg.getMessage("msg.invalid.json",
        			new String[] { e.getMessage() }, null, locale));
        }
    }
}
