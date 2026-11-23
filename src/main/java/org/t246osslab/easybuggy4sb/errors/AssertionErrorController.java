package org.t246osslab.easybuggy4sb.errors;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class AssertionErrorController extends AbstractController {

	@RequestMapping(value = "/asserr")
	public ModelAndView process(ModelAndView mav, Locale locale) {
		assert false : "Invalid!";
		// Reached only when assertions are disabled (i.e. the JVM was started without -ea)
		setViewAndCommonObjects(mav, locale, "errornotoccurred");
		mav.addObject("msg", msg.getMessage("msg.assertion.disabled", null, locale));
		return mav;
	}
}
