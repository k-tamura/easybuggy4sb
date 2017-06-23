package org.t246osslab.easybuggy4sb.exceptions;

import java.util.ResourceBundle;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MissingResourceExceptionController {

 	@RequestMapping(value = "/mre")
	public void process() {
	    ResourceBundle.getBundle("");
	}
}