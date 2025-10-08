package org.t246osslab.easybuggy4sb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PingController {

	@RequestMapping(value = "/ping")
	public ModelAndView hello(ModelAndView mav) {
		mav.setViewName("ping");
		return mav;
	}
}