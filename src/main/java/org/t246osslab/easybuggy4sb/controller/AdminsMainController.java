package org.t246osslab.easybuggy4sb.controller;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminsMainController extends AbstractController {

	@RequestMapping(value = "/admins/main")
	public ModelAndView doGet(ModelAndView mav, Locale locale) {
	    setViewAndCommonObjects(mav, locale, "adminmain");
		return mav;
	}
}
