package org.t246osslab.easybuggy4sb.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.AuthenticationException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class DefaultLoginController extends AbstractController {

    @Value("${account.lock.time}")
    long accountLockTime;

    @Value("${account.lock.count}")
    long accountLockCount;

    @Autowired
    protected LdapTemplate ldapTemplate;

    /* User's login history using in-memory account locking */
    protected ConcurrentHashMap<String, User> userLoginHistory = new ConcurrentHashMap<>();

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        setViewAndCommonObjects(mav, locale, "login");

        HashMap<String, String[]> hiddenMap = new HashMap<>();
        Enumeration<?> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            hiddenMap.put(paramName, req.getParameterValues(paramName));
            mav.addObject("hiddenMap", hiddenMap);
        }

        HttpSession session = req.getSession(true);
        if (session.getAttribute("authNMsg") != null && !"authenticated".equals(session.getAttribute("authNMsg"))) {
            mav.addObject("errmsg", msg.getMessage((String) session.getAttribute("authNMsg"), null, locale));
            session.setAttribute("authNMsg", null);
        }
        return mav;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
            throws IOException {

        String userid = StringUtils.trim(req.getParameter("userid"));
        String password = StringUtils.trim(req.getParameter("password"));

        HttpSession session = req.getSession(true);
        if (isAccountLocked(userid)) {
            session.setAttribute("authNMsg", "msg.account.locked");
            res.sendRedirect("/login");
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

            String target = (String) session.getAttribute("target");
            if (target == null) {
                res.sendRedirect("/admins/main");
            } else {
                session.removeAttribute("target");
                res.sendRedirect(target);
            }
        } else {
            /* account lock count +1 */
            if (userid != null) {
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
            }
            session.setAttribute("authNMsg", "msg.authentication.fail");
            return doGet(mav, req, res, locale);
        }
        return null;
    }

    protected boolean isAccountLocked(String userid) {
        if (userid == null) {
            return false;
        }
        User admin = userLoginHistory.get(userid);
        return admin != null && admin.getLoginFailedCount() == accountLockCount
                && (new Date().getTime() - admin.getLastLoginFailedTime().getTime() < accountLockTime);
    }

    protected boolean authUser(String userId, String password) {
        if (userId == null || password == null) {
            return false;
        }
        try {
            /* Perform a simple LDAP 'bind' authentication */
            LdapQuery query = LdapQueryBuilder.query().where("uid").is(userId);
            ldapTemplate.authenticate(query, password);
        } catch (EmptyResultDataAccessException | AuthenticationException e) {
            return false;
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            return false;
        }
        return true;
    }
}
