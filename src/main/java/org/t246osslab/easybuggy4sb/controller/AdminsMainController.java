package org.t246osslab.easybuggy4sb.controller;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class AdminsMainController {

	/* User's login history using in-memory account locking */
	protected ConcurrentHashMap<String, User> userLoginHistory = new ConcurrentHashMap<>();
	
	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/admins/main")
	public ModelAndView doGet(ModelAndView mav, Locale locale) {

		mav.setViewName("adminmain");
		mav.addObject("title", msg.getMessage("title.admins.main.page", null, locale));
		return mav;
	}
}
