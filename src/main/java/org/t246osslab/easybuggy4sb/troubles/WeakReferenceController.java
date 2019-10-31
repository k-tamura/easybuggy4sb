package org.t246osslab.easybuggy4sb.troubles;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class WeakReferenceController extends AbstractController {
	
    @RequestMapping(value = "/weakreference", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, @ModelAttribute("logLevel") String logLevel, Locale locale) {
		
    	setViewAndCommonObjects(mav, locale, "weakreference");
		
		Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
		logger.info("Information Level Message Test");
		logger.warning("Warning Level Message Test");
		logger.severe("Severe Level Message Test");
		try {
			Level.parse(logLevel);
			mav.addObject("logLevel", logLevel);
		} catch (IllegalArgumentException e) {
		}
		return mav;
    }

    @RequestMapping(value = "/weakreference", method = RequestMethod.POST)
	public String process(@RequestParam(value = "logLevel", required = false) String logLevel,
			RedirectAttributes redirectAttributes) throws IOException {
    	
		Logger logger = Logger.getLogger(this.getClass().getCanonicalName());
		logger.setLevel(Level.parse(logLevel));
		Handler handler = new ConsoleHandler();
        logger.addHandler(handler);
        Formatter formatter =  new SimpleFormatter();
        handler.setFormatter(formatter);
        redirectAttributes.addFlashAttribute("logLevel", logLevel);
        return "redirect:/weakreference";
    }
}
