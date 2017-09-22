package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class XSSController extends AbstractController {

    @RequestMapping(value = "/xss")
    public ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
            Locale locale) {
        mav.setViewName("xss");
        mav.addObject("title", msg.getMessage("title.xss.page", null, locale));
        if (!StringUtils.isBlank(string)) {
            // Reverse the given string
            String reversedName = StringUtils.reverse(string);
            mav.addObject("msg", msg.getMessage("label.reversed.string", null, locale) + " : " + reversedName);
        } else {
            mav.addObject("msg", msg.getMessage("msg.enter.string", null, locale));
        }
        return mav;
    }
}