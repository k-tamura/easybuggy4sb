package org.t246osslab.easybuggy4sb.errors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class StackOverflowErrorController {

	@RequestMapping(value = "/sofe")
	public void process() {
		new S().toString();
	}

	public class S {
		@Override
		public String toString() {
			return "" + this;
		}
	}
}
