package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class XSSController {

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/xss")
	public ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
			Locale locale) {
		String message = null;
		mav.setViewName("xss");
		mav.addObject("title", msg.getMessage("title.xss.page", null, locale));
		if (!StringUtils.isBlank(string)) {
			// Reverse the given string
			String reversedName = StringUtils.reverse(string);
			message = msg.getMessage("label.reversed.string", null, locale) + " : " + reversedName;
		} else {
			message = msg.getMessage("msg.enter.string", null, locale);
		}
		mav.addObject("msg", message);
		return mav;
	}
}