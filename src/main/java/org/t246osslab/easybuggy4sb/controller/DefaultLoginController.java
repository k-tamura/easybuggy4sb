package org.t246osslab.easybuggy4sb.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.server.core.filtering.EntryFilteringCursor;
import org.apache.directory.shared.ldap.filter.ExprNode;
import org.apache.directory.shared.ldap.filter.FilterParser;
import org.apache.directory.shared.ldap.filter.SearchScope;
import org.apache.directory.shared.ldap.message.AliasDerefMode;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.dao.EmbeddedADS;
import org.t246osslab.easybuggy4sb.core.model.User;
import org.t246osslab.easybuggy4sb.core.utils.ApplicationUtils;


@Controller
public class DefaultLoginController {

    /* User's login history using in-memory account locking */
    protected ConcurrentHashMap<String, User> userLoginHistory = new ConcurrentHashMap<String, User>();
    
    private static final Logger log = LoggerFactory.getLogger(DefaultLoginController.class);
    
    @Autowired
    MessageSource msg;
    
	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, Locale locale) {
		
        mav.setViewName("login");
        mav.addObject("title", msg.getMessage("title.login.page", null, locale));

        HashMap<String, String[]> hiddenMap = new HashMap<>();
        Enumeration<?> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            hiddenMap.put(paramName, req.getParameterValues(paramName));
            mav.addObject("hiddenMap", hiddenMap);
        }

        HttpSession session = req.getSession(true);
        if (session.getAttribute("authNMsg") != null && !"authenticated".equals(session.getAttribute("authNMsg"))) {
            mav.addObject("errmsg", msg.getMessage((String)session.getAttribute("authNMsg"), null, locale));
            session.setAttribute("authNMsg", null);
        }
        return mav;
    }

	@RequestMapping(value = "/login", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException, ServletException {

        String userid = StringUtils.trim(req.getParameter("userid"));
        String password = StringUtils.trim(req.getParameter("password"));

        HttpSession session = req.getSession(true);
        if (isAccountLocked(userid)) {
            session.setAttribute("authNMsg", "msg.account.locked");
            res.sendRedirect("/login");
        } else if (authUser(userid, password)) {
            /* Reset account lock */
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
            return doGet(mav, req, locale) ;
        }
        return null;
    }

    protected boolean isAccountLocked(String userid) {
        User admin = userLoginHistory.get(userid);
        if (admin != null
                && admin.getLoginFailedCount() == ApplicationUtils.getAccountLockCount()
                && (new Date().getTime() - admin.getLastLoginFailedTime().getTime() < ApplicationUtils
                        .getAccountLockTime())) {
            return true;
        }
        return false;
    }

    protected boolean authUser(String username, String password) {
        
        ExprNode filter = null;
        EntryFilteringCursor cursor = null;
        try {
            filter = FilterParser.parse("(&(uid=" + ESAPI.encoder().encodeForLDAP(username.trim())
                    + ")(userPassword=" + ESAPI.encoder().encodeForLDAP(password.trim()) + "))");
            cursor = EmbeddedADS.getAdminSession().search(new LdapDN("ou=people,dc=t246osslab,dc=org"),
                    SearchScope.SUBTREE, filter, AliasDerefMode.NEVER_DEREF_ALIASES, null);
            if (cursor.available()) {
                return true;
            }
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    log.error("Exception occurs: ", e);
                }
            }
        }
        return false;
    }
}
