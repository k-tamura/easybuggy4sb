package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class MojibakeController extends AbstractController {

    @RequestMapping(value = "/mojibake")
    public ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
    		Locale locale) {
        setViewAndCommonObjects(mav, locale, "mojibake");
        if (!StringUtils.isBlank(string)) {
            // Capitalize the given string
            String capitalizedName = WordUtils.capitalize(string);
            mav.addObject("msg", msg.getMessage("label.capitalized.string", null, locale) + " : "
                    + encodeForHTML(capitalizedName));
        } else {
            mav.addObject("msg", msg.getMessage("msg.enter.string", null, locale));
        }
        return mav;
    }
}