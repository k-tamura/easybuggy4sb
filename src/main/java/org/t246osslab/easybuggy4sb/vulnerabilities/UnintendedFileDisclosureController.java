package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javax.servlet.http.HttpServletRequest;

@Controller
public class UnintendedFileDisclosureController extends AbstractController {

    @RequestMapping(value = "/unintendedfiledisclosure")
    public ModelAndView unintendedfiledisclosure(ModelAndView mav, HttpServletRequest req, Locale locale) {
        setViewAndCommonObjects(mav, locale, "unintendedfiledisclosure");
        String baseURL = req.getRequestURL().toString().replaceAll("/unintendedfiledisclosure*.+", "");
        String[] placeholders = new String[]{ baseURL};
        mav.addObject("note", msg.getMessage("msg.note.unintendedfiledisclosure", placeholders, locale));
        return mav;
    }
    
    @RequestMapping(value = "/serverinfo")
    public ModelAndView serverinfo(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "serverinfo");
        mav.addObject("properties", System.getProperties());
        return mav;
    }
}