package org.t246osslab.easybuggy4sb.vulnerabilities;

import com.sun.management.OperatingSystemMXBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Locale;

@Controller
public class HTTPLoginController extends DefaultLoginController {

	@Override
	@GetMapping(value = "/status/login")
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
		req.setAttribute("note", msg.getMessage("msg.note.http.login", null, locale));
		super.doGet(mav, req, res, locale);
		return mav;
	}

	@Override
	@PostMapping(value = "/status/login")
	public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
		return super.doPost(mav, req, res, locale);
	}

	@RequestMapping(value = "/admins/status")
	public ModelAndView status(ModelAndView mav, Locale locale) {
		setViewAndCommonObjects(mav, locale, "status");
		mav.addObject("osMXBean", (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean());
		return mav;
	}
}
