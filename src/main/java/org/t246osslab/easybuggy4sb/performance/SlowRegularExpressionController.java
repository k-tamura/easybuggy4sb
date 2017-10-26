package org.t246osslab.easybuggy4sb.performance;

import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class SlowRegularExpressionController extends AbstractController {

	@RequestMapping(value = "/slowre")
	public ModelAndView process(@RequestParam(value = "word", required = false) String word, ModelAndView mav,
			Locale locale) {
		String message;
        setViewAndCommonObjects(mav, locale, "slowregex");
		if (!StringUtils.isBlank(word)) {
			if (isMatched(word)) {
				message = msg.getMessage("msg.match.regular.expression", null, locale);
			} else {
				message = msg.getMessage("msg.not.match.regular.expression", null, locale);
			}
		} else {
			message = msg.getMessage("msg.enter.string", null, locale);
		}
        mav.addObject("msg", message);
		return mav;
	}

	private boolean isMatched(String word) {
		log.info("Start Date: {}", new Date());
		Pattern compile = Pattern.compile("^([a-z0-9]+[-]{0,1}){1,100}$");
		Matcher matcher = compile.matcher(word);
		boolean matches = matcher.matches();
		log.info("End Date: {}", new Date());
		return matches;
	}
}