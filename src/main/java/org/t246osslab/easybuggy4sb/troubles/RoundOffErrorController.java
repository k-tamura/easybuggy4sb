package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class RoundOffErrorController extends AbstractController {

    @RequestMapping(value = "/roe")
    public ModelAndView process(@RequestParam(value = "number", required = false) String strNumber, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "roundofferror");
        double number = NumberUtils.toDouble(strNumber, -1);
        if (1 <= number && number <= 9) {
            mav.addObject("number", strNumber);
            mav.addObject("result", number - 0.9);
        }
        return mav;
    }
}
