package org.t246osslab.easybuggy4sb.errors;

import javax.xml.transform.TransformerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TransformerFactoryConfigurationErrorController {

	@RequestMapping(value = "/tfce")
	public void process() {
        System.setProperty("javax.xml.transform.TransformerFactory", "a");
        TransformerFactory.newInstance();
	}
}
