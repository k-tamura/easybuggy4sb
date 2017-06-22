package org.t246osslab.easybuggy4sb.errors;

import javax.xml.parsers.SAXParserFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FactoryConfigurationErrorController {

	@RequestMapping(value = "/fce")
	public void process() {
		System.setProperty("javax.xml.parsers.SAXParserFactory", "non-exist-factory");
		SAXParserFactory.newInstance();
	}
}
