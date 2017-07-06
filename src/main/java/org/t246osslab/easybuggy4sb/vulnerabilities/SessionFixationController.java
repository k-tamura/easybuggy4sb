package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;

@Controller
public class SessionFixationController extends DefaultLoginController {

    @Override
    @RequestMapping(value = "/sessionfixation/login", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        req.setAttribute("note", msg.getMessage("msg.note.session.fixation", null, locale));
        super.doGet(mav, req, res, locale);
        return mav;
    }

    @Override
    @RequestMapping(value = "/sessionfixation/login", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        return super.doPost(mav, req, res, locale);
    }
}
