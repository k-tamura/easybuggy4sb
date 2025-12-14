package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;

@Controller
public class BruteForceController extends DefaultLoginController {

    @Override
    @GetMapping(value = "/bruteforce/login")
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        req.setAttribute("note", msg.getMessage("msg.note.brute.force", null, locale));
        super.doGet(mav, req, res, locale);
        return mav;
    }

    @Override
    @PostMapping(value = "/bruteforce/login")
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
            throws IOException {

        String userid = req.getParameter("userid");
        String password = req.getParameter("password");

        HttpSession session = req.getSession(true);
        if (authUser(userid, password)) {
            session.setAttribute("authNMsg", "authenticated");
            session.setAttribute("userid", userid);

            String target = (String) session.getAttribute("target");
            if (target == null) {
                res.sendRedirect("/admins/main");
            } else {
                session.removeAttribute("target");
                res.sendRedirect(target);
            }
            return null;
        } else {
            session.setAttribute("authNMsg", msg.getMessage("msg.authentication.fail", null, locale));
        }
        return doGet(mav, req, res, locale);
    }
}
