package org.t246osslab.easybuggy4sb.troubles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class ThreadUnsafeController extends AbstractController {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

	public ThreadUnsafeController() {
		sdf.setLenient(false);
	}

    @RequestMapping(value = "/threadunsafe")
    public ModelAndView process(@RequestParam(value = "year", required = false) String year, ModelAndView mav, Locale locale) {
		setViewAndCommonObjects(mav, locale, "threadunsafe");
		if (year != null) {
			mav.addObject("year", year);
			try {
				sdf.parse(year + "0229");
				mav.addObject("isLeapYear", msg.getMessage("msg.is.leap.year", null, locale));
			} catch (ParseException e) {
				mav.addObject("isLeapYear", msg.getMessage("msg.is.not.leap.year", null, locale));
			}
		}
		return mav;
    }
}
