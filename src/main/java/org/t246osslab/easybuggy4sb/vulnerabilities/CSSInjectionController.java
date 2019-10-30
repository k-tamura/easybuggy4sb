package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class CSSInjectionController extends AbstractController {

    @RequestMapping(value = "/admins/cssinject")
    public ModelAndView process(@RequestParam(value = "style", required = false, defaultValue = "@import url('https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css');") String style, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "cssinjection");
        Resource resource = new ClassPathResource("/css/cssinjection.css");
		try {
			mav.addObject("cssinjection_css", IOUtils.toString(resource.getInputStream()));
		} catch (IOException e) {
		}
		mav.addObject("nonce", UUID.randomUUID().toString());
		mav.addObject("style", style);
        return mav;
    }
}