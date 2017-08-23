package org.t246osslab.easybuggy4sb.controller;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AdminsMainController {

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/admins/main")
	public ModelAndView doGet(ModelAndView mav, Locale locale) {

		mav.setViewName("adminmain");
		mav.addObject("title", msg.getMessage("title.admins.main.page", null, locale));
		return mav;
	}
}
