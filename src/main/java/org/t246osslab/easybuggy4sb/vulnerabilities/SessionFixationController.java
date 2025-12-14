package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;

@Controller
public class SessionFixationController extends DefaultLoginController {

    @Override
    @GetMapping(value = "/sessionfixation/login")
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        mav.addObject("note", msg.getMessage("msg.note.session.fixation",
                new Object[]{ req.getRequestURL().toString() + ";jsessionid=" }, locale));
        super.doGet(mav, req, res, locale);
        return mav;
    }

    @Override
    @PostMapping(value = "/sessionfixation/login")
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        return super.doPost(mav, req, res, locale);
    }
}
