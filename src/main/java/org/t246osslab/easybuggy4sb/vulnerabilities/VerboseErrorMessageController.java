package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger log = LoggerFactory.getLogger(VerboseErrorMessageController.class);

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
			session.setAttribute("authNMsg", "msg.account.locked");
			return doGet(mav, req, res, locale);
		} else if (!isExistUser(userid)) {
			session.setAttribute("authNMsg", "msg.user.not.exist");
			return doGet(mav, req, res, locale);
		} else if (!password.matches("[0-9a-z]{8}")) {
			session.setAttribute("authNMsg", "msg.low.alphnum8");
			return doGet(mav, req, res, locale);
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

			session.setAttribute("authNMsg", "msg.password.not.match");
			return doGet(mav, req, res, locale);
		}
		return null;
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
