package org.t246osslab.easybuggy4sb.exceptions;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ClassCastExceptionController {

	@RequestMapping(value = "/cce")
	public void process(HttpServletRequest req) {
        req.setAttribute("param1", "value1");
        req.setAttribute("param2", (String[]) req.getAttribute("param1"));
	}
}