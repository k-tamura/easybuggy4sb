package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.shared.ldap.entry.ModificationOperation;
import org.apache.directory.shared.ldap.entry.client.ClientModification;
import org.apache.directory.shared.ldap.entry.client.DefaultClientAttribute;
import org.apache.directory.shared.ldap.message.ModifyRequestImpl;
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

@Controller
public class CSRFController {

    private static final Logger log = LoggerFactory.getLogger(CSRFController.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/admins/csrf", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, Locale locale) {
        mav.setViewName("csrf");
        mav.addObject("title", msg.getMessage("section.change.password", null, locale));
        return mav;
    }

    @RequestMapping(value = "/admins/csrf", method = RequestMethod.POST)
    protected ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
            throws IOException {
        mav.setViewName("csrf");
        mav.addObject("title", msg.getMessage("section.change.password", null, locale));
        HttpSession session = req.getSession();
        if (session == null) {
            res.sendRedirect("/");
            return null;
        }
        String userid = (String) session.getAttribute("userid");
        String password = StringUtils.trim(req.getParameter("password"));
        if (!StringUtils.isBlank(userid) && !StringUtils.isBlank(password) && password.length() >= 8) {
            try {
                DefaultClientAttribute entryAttribute = new DefaultClientAttribute("userPassword",
                        ESAPI.encoder().encodeForLDAP(password.trim()));
                ClientModification clientModification = new ClientModification();
                clientModification.setAttribute(entryAttribute);
                clientModification.setOperation(ModificationOperation.REPLACE_ATTRIBUTE);
                ModifyRequestImpl modifyRequest = new ModifyRequestImpl(1);
                modifyRequest.setName(new LdapDN(
                        "uid=" + ESAPI.encoder().encodeForLDAP(userid.trim()) + ",ou=people,dc=t246osslab,dc=org"));
                modifyRequest.addModification(clientModification);
                EmbeddedADS.getAdminSession().modify(modifyRequest);

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
