package org.t246osslab.easybuggy4sb.errors;

import javax.xml.transform.TransformerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TransformerFactoryConfigurationErrorController {

	private static final String FACTORY_PROPERTY = "javax.xml.transform.TransformerFactory";

	@RequestMapping(value = "/tfce")
	public void process() {
		String original = System.getProperty(FACTORY_PROPERTY);
		System.setProperty(FACTORY_PROPERTY, "a");
		try {
			TransformerFactory.newInstance();
		} finally {
			// Restore the JVM-wide property, otherwise every later XSLT transformation would fail too
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
