package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

@Controller
public class ClickJackingController extends AbstractController {

	@Autowired
	private LdapTemplate ldapTemplate;

	@GetMapping(value = "/admins/clickjacking")
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, HttpSession session, Locale locale) throws IOException {
		String userid = (String) session.getAttribute("userid");
		if (userid == null) {
			res.sendRedirect("/");
			return null;
		}
		mav.addObject("currentMail", getCurrentMail(userid));
		setViewAndCommonObjects(mav, locale, "clickjacking");
		mav.addObject("note", msg.getMessage("msg.note.clickjacking", new Object[]{req.getServerName()}, locale));
		return mav;
	}

	@PostMapping(value = "/admins/clickjacking")
	protected ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
			throws IOException {
        setViewAndCommonObjects(mav, locale, "clickjacking");
        
		HttpSession session = req.getSession();
		if (session == null) {
			res.sendRedirect("/");
			return null;
		}
		String userid = (String) session.getAttribute("userid");
		if (userid == null) {
			res.sendRedirect("/");
			return null;
		}
		String mail = StringUtils.trim(req.getParameter("mail"));
		if (!StringUtils.isBlank(mail) && isValidEmailAddress(mail)) {
			try {
				ModificationItem item = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
						new BasicAttribute("mail", mail));
				ldapTemplate.modifyAttributes(
						"uid=" + encodeForLDAP(userid.trim()) + ",ou=people,dc=t246osslab,dc=org",
						new ModificationItem[] { item });
				mav.addObject("mail", mail);

			} catch (Exception e) {
				log.error("Exception occurs: ", e);
				mav.addObject("errmsg", msg.getMessage("msg.mail.change.failed", null, locale));
				return doGet(mav, req, res, session, locale);
			}
		} else {
			mav.addObject("errmsg", msg.getMessage("msg.mail.format.is.invalid", null, locale));
			return doGet(mav, req, res, session, locale);
		}
		return mav;
	}

	private boolean isValidEmailAddress(String email) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
		} catch (AddressException e) {
			log.debug("Mail address is invalid: " + email, e);
			result = false;
		}
		return result;
	}

	/**
	 * Retrieves the specified user's email address from LDAP.
	 * @param userid: The user ID retrieved from the session.
	 * @return The retrieved email address. If not found, returns null.
	 */
	private String getCurrentMail(String userid) {
		if (StringUtils.isBlank(userid)) {
			return null;
		}
		String currentMail = null;
		try {
			String dn = "uid=" + encodeForLDAP(userid.trim()) + ",ou=people,dc=t246osslab,dc=org";

			currentMail = ldapTemplate.lookup(
					dn,
					new String[] {"mail"},
					(AttributesMapper<String>) attrs -> {
						javax.naming.directory.Attribute a = attrs.get("mail");
						if (a == null) return null;
						try {
							Object val = a.get();
							return val != null ? val.toString() : null;
						} catch (NamingException e) {
							throw new RuntimeException(e);
						}
					}
			);
		} catch (Exception e) {
			log.error("Failed to retrieve mail address from LDAP for user: " + userid, e);
		}
        return currentMail;
    }
}
