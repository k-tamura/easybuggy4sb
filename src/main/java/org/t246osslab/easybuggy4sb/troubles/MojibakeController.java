package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MojibakeController {

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/mojibake")
	public ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
			Locale locale) {
		String message = null;
		mav.setViewName("mojibake");
		mav.addObject("title", msg.getMessage("title.mojibake.page", null, locale));
		if (!StringUtils.isBlank(string)) {
			// Capitalize the given string
			String capitalizedName = WordUtils.capitalize(string);
			message = msg.getMessage("label.capitalized.string", null, locale) + " : " + capitalizedName;
		} else {
			message = msg.getMessage("msg.enter.string", null, locale);
		}
		mav.addObject("msg", message);
		return mav;
	}
}