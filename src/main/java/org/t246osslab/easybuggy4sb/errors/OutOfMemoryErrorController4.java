package org.t246osslab.easybuggy4sb.errors;

import java.util.Properties;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OutOfMemoryErrorController4 {

	@RequestMapping(value = "/oome4")
	public void process() {
		Properties properties = System.getProperties();
		Random r = new Random();
		while (true) {
			properties.put(r.nextInt(), "value");
		}
	}
}
