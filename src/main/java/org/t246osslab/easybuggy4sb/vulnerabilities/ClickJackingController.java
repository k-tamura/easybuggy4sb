package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
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
public class ClickJackingController {

	private static final Logger log = LoggerFactory.getLogger(ClickJackingController.class);

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/admins/clickjacking", method = RequestMethod.GET)
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
		mav.setViewName("clickjacking");
		mav.addObject("title", msg.getMessage("section.change.mail", null, locale));

		String errorMessage = (String) req.getAttribute("errorMessage");
		if (errorMessage != null) {
			mav.addObject("errmsg", msg.getMessage("msg.note.clickjacking", null, locale));
		}
		return mav;
	}

	@RequestMapping(value = "/admins/clickjacking", method = RequestMethod.POST)
	protected void doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
			throws IOException {
		HttpSession session = req.getSession();
		if (session == null) {
			res.sendRedirect("/");
			return;
		}
		String userid = (String) session.getAttribute("userid");
		String mail = StringUtils.trim(req.getParameter("mail"));
		if (!StringUtils.isBlank(mail) && isValidEmailAddress(mail)) {
			try {
				DefaultClientAttribute entryAttribute = new DefaultClientAttribute("mail",
						ESAPI.encoder().encodeForLDAP(mail.trim()));
				ClientModification clientModification = new ClientModification();
				clientModification.setAttribute(entryAttribute);
				clientModification.setOperation(ModificationOperation.REPLACE_ATTRIBUTE);
				ModifyRequestImpl modifyRequest = new ModifyRequestImpl(1);
				modifyRequest.setName(new LdapDN(
						"uid=" + ESAPI.encoder().encodeForLDAP(userid.trim()) + ",ou=people,dc=t246osslab,dc=org"));
				modifyRequest.addModification(clientModification);
				EmbeddedADS.getAdminSession().modify(modifyRequest);
				mav.addObject("mail", mail);

			} catch (Exception e) {
				log.error("Exception occurs: ", e);
				mav.addObject("errmsg", msg.getMessage("msg.mail.change.failed", null, locale));
				doGet(mav, req, res, locale);
			}
		} else {
			mav.addObject("errmsg", msg.getMessage("msg.mail.format.is.invalid", null, locale));
			doGet(mav, req, res, locale);
		}
	}

	public boolean isValidEmailAddress(String email) {
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
}
