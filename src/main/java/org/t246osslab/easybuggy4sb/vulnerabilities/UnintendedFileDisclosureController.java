package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UnintendedFileDisclosureController {

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/clientinfo")
    public ModelAndView process(ModelAndView mav, Locale locale) {
        mav.setViewName("clientinfo");
        mav.addObject("title", msg.getMessage("section.client.info", null, locale));
        return mav;
    }
    
    @RequestMapping(value = "/serverinfo")
    public ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
            Locale locale) {
        mav.setViewName("serverinfo");
        mav.addObject("title", msg.getMessage("section.server.info", null, locale));
        mav.addObject("properties", System.getProperties());
        return mav;
    }
}