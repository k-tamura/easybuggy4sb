package org.t246osslab.easybuggy4sb.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultLogoutController {

	@RequestMapping(value = "/logout")
	public String process(HttpSession ses) {

        ses.invalidate();
        return "redirect:/";
    }
}
