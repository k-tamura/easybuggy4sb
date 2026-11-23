package org.t246osslab.easybuggy4sb.errors;

import javax.xml.parsers.SAXParserFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FactoryConfigurationErrorController {

	private static final String FACTORY_PROPERTY = "javax.xml.parsers.SAXParserFactory";

	@RequestMapping(value = "/fce")
	public void process() {
		String original = System.getProperty(FACTORY_PROPERTY);
		System.setProperty(FACTORY_PROPERTY, "non-exist-factory");
		try {
			SAXParserFactory.newInstance();
		} finally {
			// Restore the JVM-wide property, otherwise every later SAX parsing (e.g. XXE, XEE) would fail too
			restore(original);
		}
	}

	private static void restore(String original) {
		if (original == null) {
			System.clearProperty(FACTORY_PROPERTY);
		} else {
			System.setProperty(FACTORY_PROPERTY, original);
		}
	}
}
