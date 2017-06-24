package org.t246osslab.easybuggy4sb.performance;

import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CreatingUnnecessaryObjectsController {

	private static final Logger log = LoggerFactory.getLogger(CreatingUnnecessaryObjectsController.class);

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/createobjects")
	public ModelAndView process(@RequestParam(value = "number", required = false) String strNumber, ModelAndView mav,
			Locale locale) {
		try {
			int number = NumberUtils.toInt(strNumber, -1);
			StringBuilder message = new StringBuilder();
			mav.setViewName("createobjects");
			mav.addObject("title", msg.getMessage("title.sum.of.natural.numbers", null, locale));

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
				log.info((System.nanoTime() - start) / 1000000f + " ms");
			}
			mav.addObject("msg", message.toString());
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
		}
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