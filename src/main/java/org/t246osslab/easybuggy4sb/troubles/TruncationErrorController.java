package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class TruncationErrorController extends AbstractController {

    @RequestMapping(value = "/te")
    public ModelAndView process(@RequestParam(value = "number", required = false) String strNumber, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "truncationerror");
        
        double number = NumberUtils.toDouble(strNumber, -1);
        if (0 < number && number < 10) {
            mav.addObject("number", strNumber);
            mav.addObject("result", 10.0 / number);
        }
        return mav;
    }
}
