package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class OpenRedirectController extends DefaultLoginController {

    private static final Logger log = LoggerFactory.getLogger(OpenRedirectController.class);

    @Override
    @RequestMapping(value = "/openredirect/login", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        req.setAttribute("note", msg.getMessage("msg.note.open.redirect", null, locale));
        super.doGet(mav, req, res, locale);
        return mav;
    }

    @Override
    @RequestMapping(value = "/openredirect/login", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {

        String userid = req.getParameter("userid");
        String password = req.getParameter("password");
        String loginQueryString = req.getParameter("loginquerystring");
        if (loginQueryString == null) {
            loginQueryString = "";
        } else {
            loginQueryString = "?" + loginQueryString;
        }
        
        HttpSession session = req.getSession(true);
        if (isAccountLocked(userid)) {
            session.setAttribute("authNMsg", "msg.account.locked");
            res.sendRedirect("/openredirect/login" + loginQueryString);
        } else if (authUser(userid, password)) {
            /* if authentication succeeded, then reset account lock */
            User admin = userLoginHistory.get(userid);
            if (admin == null) {
                User newAdmin = new User();
                newAdmin.setUserId(userid);
                admin = userLoginHistory.putIfAbsent(userid, newAdmin);
                if (admin == null) {
                    admin = newAdmin;
                }
            }
            admin.setLoginFailedCount(0);
            admin.setLastLoginFailedTime(null);

            session.setAttribute("authNMsg", "authenticated");
            session.setAttribute("userid", userid);
            
            String gotoUrl = req.getParameter("goto");
            if (gotoUrl != null) {
                try {
                    URL u = new URL(gotoUrl);
                    gotoUrl = u.toURI().toString();
                } catch (Exception e) {
                    log.warn("Invalid goto Url: {}", gotoUrl);
                }
                res.sendRedirect(gotoUrl);
            } else {
                String target = (String) session.getAttribute("target");
                if (target == null) {
                    res.sendRedirect("/admins/main");
                } else {
                    session.removeAttribute("target");
                    res.sendRedirect(target);
                }
            }
        } else {
            /* account lock count +1 */
            User admin = userLoginHistory.get(userid);
            if (admin == null) {
                User newAdmin = new User();
                newAdmin.setUserId(userid);
                admin = userLoginHistory.putIfAbsent(userid, newAdmin);
                if (admin == null) {
                    admin = newAdmin;
                }
            }
            admin.setLoginFailedCount(admin.getLoginFailedCount() + 1);
            admin.setLastLoginFailedTime(new Date());
            
            session.setAttribute("authNMsg", "msg.authentication.fail");
            res.sendRedirect("/openredirect/login" + loginQueryString);
        }
        return null;
    }
}
