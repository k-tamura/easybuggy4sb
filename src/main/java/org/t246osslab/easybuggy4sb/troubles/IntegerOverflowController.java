package org.t246osslab.easybuggy4sb.troubles;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class IntegerOverflowController extends AbstractController {

    @RequestMapping(value = "/iof")
    public ModelAndView process(@RequestParam(value = "times", required = false) String strTimes, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "intoverflow");
        BigDecimal thickness = null;
        BigDecimal thicknessM = null;
        BigDecimal thicknessKm = null;
        int times = NumberUtils.toInt(strTimes, -1);
        if (strTimes != null) {
            long multipleNumber = 1;
            if (times >= 0) {
                for (int i = 0; i < times; i++) {
                    multipleNumber = multipleNumber * 2;
                }
                thickness = new BigDecimal(multipleNumber).divide(new BigDecimal(10)); // mm
                thicknessM = thickness.divide(new BigDecimal(1000)); // m
                thicknessKm = thicknessM.divide(new BigDecimal(1000)); // km
            }
        }
        if (times >= 0) {
            mav.addObject("times", strTimes);
            StringBuilder description = new StringBuilder();
            description.append(thickness + " mm");
            if (thicknessM != null && thicknessKm != null) {
                description.append(thicknessM.intValue() >= 1 && thicknessKm.intValue() < 1 ? " = " + thicknessM + " m" : "");
                description.append(thicknessKm.intValue() >= 1 ? " = " + thicknessKm + " km" : "");
            }
            if (times == 42) {
                description.append(" : " + msg.getMessage("msg.answer.is.correct", null, locale));
            }
            mav.addObject("description", description.toString());
        }
        return mav;
    }
}
