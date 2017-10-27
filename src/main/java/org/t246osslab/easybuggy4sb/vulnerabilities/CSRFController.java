package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class CSRFController extends AbstractController {

	@Autowired
	private LdapTemplate ldapTemplate;
	
    @RequestMapping(value = "/admins/csrf", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "csrf");
        return mav;
    }

    @RequestMapping(value = "/admins/csrf", method = RequestMethod.POST)
    protected ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
            throws IOException {
        setViewAndCommonObjects(mav, locale, "csrf");
        HttpSession session = req.getSession();
        if (session == null) {
            res.sendRedirect("/");
            return null;
        }
        String userid = (String) session.getAttribute("userid");
        String password = StringUtils.trim(req.getParameter("password"));
        if (!StringUtils.isBlank(userid) && !StringUtils.isBlank(password) && password.length() >= 8) {
            try {
				ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("userPassword", password));
				ldapTemplate.modifyAttributes(
						"uid=" + encodeForLDAP(userid.trim()) + ",ou=people,dc=t246osslab,dc=org",
						new ModificationItem[] { item });
            } catch (Exception e) {
                log.error("Exception occurs: ", e);
                mav.addObject("errmsg", msg.getMessage("msg.passwd.change.failed", null, locale));
                return doGet(mav, locale);
            }
        } else {
            if (StringUtils.isBlank(password) || password.length() < 8) {
                mav.addObject("errmsg", msg.getMessage("msg.passwd.is.too.short", null, locale));
            } else {
                mav.addObject("errmsg", msg.getMessage("msg.unknown.exception.occur",
                        new String[] { "userid: " + userid }, null, locale));
            }
            return doGet(mav, locale);
        }
        mav.addObject("complete", "true");
        return mav;
    }
}
