package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class IntegerComparisonController extends AbstractController {

    @RequestMapping(value = "/intcompare")
    public ModelAndView process(@RequestParam(value = "number1", required = false) String strNumber1,
            @RequestParam(value = "number2", required = false) String strNumber2, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "intcompare");
        if (StringUtils.isBlank(strNumber1) || StringUtils.isBlank(strNumber2)) {
            mav.addObject("msg", msg.getMessage("msg.enter.two.integers", null, locale));
            return mav;
        }
        mav.addObject("number1", strNumber1);
        mav.addObject("number2", strNumber2);
        Integer number1;
        Integer number2;
        try {
            number1 = Integer.valueOf(strNumber1.trim());
            number2 = Integer.valueOf(strNumber2.trim());
        } catch (NumberFormatException e) {
            mav.addObject("errmsg", msg.getMessage("msg.enter.two.integers", null, locale));
            return mav;
        }
        if (number1 == number2) {
            mav.addObject("msg", msg.getMessage("msg.numbers.are.equal",
                    new Object[] { String.valueOf(number1), String.valueOf(number2) }, locale));
        } else {
            mav.addObject("msg", msg.getMessage("msg.numbers.are.not.equal",
                    new Object[] { String.valueOf(number1), String.valueOf(number2) }, locale));
        }
        return mav;
    }
}
