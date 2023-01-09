package org.t246osslab.easybuggy4sb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

@Controller
public class PingController {

	@RequestMapping(value = "/ping")
	public ModelAndView hello(ModelAndView mav) {
		mav.setViewName("ping");
		return mav;
	}
}