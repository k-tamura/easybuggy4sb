package org.t246osslab.easybuggy4sb.performance;

import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class CreatingUnnecessaryObjectsController extends AbstractController {

    @RequestMapping(value = "/createobjects")
    public ModelAndView process(@RequestParam(value = "number", required = false) String strNumber, ModelAndView mav,
            Locale locale) {
        int number = NumberUtils.toInt(strNumber, -1);
        StringBuilder message = new StringBuilder();
        setViewAndCommonObjects(mav, locale, "createobjects");

        if (number > 0) {
            mav.addObject("number", number);
            switch (number) {
            case 1:
                break;
            case 2:
                message.append("1 + 2 = ");
                break;
            case 3:
                message.append("1 + 2 + 3 = ");
                break;
            case 4:
                message.append("1 + 2 + 3 + 4 = ");
                break;
            case 5:
                message.append("1 + 2 + 3 + 4 + 5 = ");
                break;
            default:
                message.append("1 + 2 + 3 + ... + " + number + " = ");
                message.append("\\(\\begin{eqnarray}\\sum_{ k = 1 }^{ " + number + " } k\\end{eqnarray}\\) = ");
            }
        } else {
            message.append("1 + 2 + 3 + ... + n = ");
            message.append("\\(\\begin{eqnarray}\\sum_{ k = 1 }^{ n } k\\end{eqnarray}\\) = ");
        }
        if (number >= 1) {
            long start = System.nanoTime();
            message.append(calcSum1(number));
            log.info("{} ms", (System.nanoTime() - start) / 1000000f);
        }
        mav.addObject("msg", message.toString());
        return mav;
    }

    private Long calcSum1(int number) {
        Long sum = 0L;
        for (long i = 1; i <= number; i++) {
            sum += i;
        }
        return sum;
    }
	/*
    private long calcSum2(int number) {
        long sum = 0L;
        for (int i = 1; i <= number; i++) {
            sum += i;
        }
        return sum;
    }

    private long calcSum3(int number) {
        return (long) number * (number + 1) / 2;
	}
	 */
}