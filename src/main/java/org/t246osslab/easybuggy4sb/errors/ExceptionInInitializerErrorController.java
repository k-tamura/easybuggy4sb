package org.t246osslab.easybuggy4sb.errors;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExceptionInInitializerErrorController {

	/*
	 * Class.forName initializes the class, so the static initializer of InitializerErrorThrower runs here:
	 * the first access throws ExceptionInInitializerError, and later ones throw NoClassDefFoundError.
	 */
	@RequestMapping(value = "/eie")
	public void process() throws ClassNotFoundException {
		Class.forName("org.t246osslab.easybuggy4sb.errors.InitializerErrorThrower");
	}
}

class InitializerErrorThrower {
	static {
		LoggerFactory.getLogger(InitializerErrorThrower.class).debug("clinit" + 1 / 0);
	}

	private InitializerErrorThrower(){
		// this constructor is added to suppress sonar advice
	}
}