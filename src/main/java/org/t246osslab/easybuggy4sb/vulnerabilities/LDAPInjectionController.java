package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.DefaultLoginController;
import org.t246osslab.easybuggy4sb.core.model.User;

@Controller
public class LDAPInjectionController extends DefaultLoginController {
	
    @Override
    @RequestMapping(value = "/ldapijc/login", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        req.setAttribute("note", msg.getMessage("msg.note.ldap.injection", null, locale));
        super.doGet(mav, req, res, locale);
        return mav;
    }

    @Override
    @RequestMapping(value = "/ldapijc/login", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        return super.doPost(mav, req, res, locale);
    }
    
	@Override
	protected boolean authUser(String userId, String password) {

		if (StringUtils.isBlank(userId) || userId.length() < 5 || StringUtils.isBlank(password)
				|| password.length() < 8) {
			return false;
		}
		try {
			LdapQuery query = LdapQueryBuilder.query()
					.filter("(&(uid=" + userId.trim() + ")(userPassword=" + password.trim() + "))");
			List<User> users = ldapTemplate.find(query, User.class);
			if (users.isEmpty()) {
				return false;
			}
		} catch (EmptyResultDataAccessException e) {
			return false;
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
			return false;
		}
		return true;
	}
}
