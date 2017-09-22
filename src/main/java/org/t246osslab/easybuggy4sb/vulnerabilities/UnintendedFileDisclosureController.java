package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class UnintendedFileDisclosureController extends AbstractController {

    @RequestMapping(value = "/clientinfo")
    public ModelAndView process(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "clientinfo");
        return mav;
    }
    
    @RequestMapping(value = "/serverinfo")
    public ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "serverinfo");
        mav.addObject("properties", System.getProperties());
        return mav;
    }
}