package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
public class VerboseErrorMessageController extends DefaultLoginController {

	@Override
	@RequestMapping(value = "/verbosemsg/login", method = RequestMethod.GET)
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
		req.setAttribute("note", msg.getMessage("msg.note.verbose.errror.message", null, locale));
		super.doGet(mav, req, res, locale);
		return mav;
	}

	@Override
	@RequestMapping(value = "/verbosemsg/login", method = RequestMethod.POST)
	public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
			throws IOException {

		String userid = req.getParameter("userid");
		String password = req.getParameter("password");

		HttpSession session = req.getSession(true);
		if (isAccountLocked(userid)) {
			session.setAttribute("authNMsg",
					msg.getMessage("msg.account.locked", new String[] { String.valueOf(accountLockCount) }, locale));
		} else if (!isExistUser(userid)) {
			session.setAttribute("authNMsg", msg.getMessage("msg.user.not.exist", null, locale));
		} else if (!password.matches("[0-9a-z]{8}")) {
			session.setAttribute("authNMsg", msg.getMessage("msg.low.alphnum8", null, locale));
		} else if (authUser(userid, password)) {
            /* if authentication succeeded, then reset account lock */
		    resetAccountLock(userid);

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
			session.setAttribute("authNMsg", msg.getMessage("msg.password.not.match", null, locale));
		}
		/* account lock count +1 */
	    incrementLoginFailedCount(userid);
		return doGet(mav, req, res, locale);
	}

	private boolean isExistUser(String username) {
		try {
			LdapQuery query = LdapQueryBuilder.query().where("uid").is(username);
			User user = ldapTemplate.findOne(query, User.class);
			if (user != null) {
				return true;
			}
        } catch (EmptyResultDataAccessException e) {
            // do nothing if user does not exist
        } catch (Exception e) {
			log.error("Exception occurs: ", e);
		}
		return false;
	}
}
