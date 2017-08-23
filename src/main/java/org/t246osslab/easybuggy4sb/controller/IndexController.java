package org.t246osslab.easybuggy4sb.controller;

import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    
    @Autowired
    MessageSource msg;
    
	@RequestMapping(value = "/")
	public ModelAndView init(HttpSession ses, ModelAndView mav, Locale locale) {
		ses.removeAttribute("dlpinit");
		mav.setViewName("index");
		mav.addObject("title", "EasyBuggy Boot");
        String permName = null;
		String lblPerm = null;
        if (System.getProperty("java.version").startsWith("1.7")) {
            permName = "PermGen space";
            lblPerm = msg.getMessage("label.permgen.space", null, locale);
        } else {
            permName = "Metaspace";
            lblPerm = msg.getMessage("label.metaspace", null, locale);
        }
        mav.addObject("permname", permName);
        mav.addObject("memoryleak2func", msg.getMessage("function.name.memory.leak2", new Object[] { lblPerm }, locale));
        mav.addObject("memoryleak2desc", msg.getMessage("function.description.memory.leak2", new Object[] { lblPerm }, locale));
		return mav;
	}
}