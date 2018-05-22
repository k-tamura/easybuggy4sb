package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

/**
 * A servlet that takes message details from user and send it as a new mail
 * through an SMTP server. The mail may contain a attachment which is the file
 * uploaded from client.
 */
@Controller
public class MailHeaderInjectionController extends AbstractController {

	// administrator's mail address
	@Value("${mail.admin.address}")
	private String adminAddress;

	@Autowired
	private JavaMailSender javaMailSender;

	@RequestMapping(value = "/mailheaderijct", method = RequestMethod.GET)
	public ModelAndView doGet(ModelAndView mav, Locale locale) {
	    setViewAndCommonObjects(mav, locale, "mailheaderinjection");
		return mav;
	}

	@RequestMapping(value = "/mailheaderijct", method = RequestMethod.POST)
	public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, Locale locale)
			throws IOException, ServletException {
        setViewAndCommonObjects(mav, locale, "mailheaderinjection");

		List<File> uploadedFiles = saveUploadedFiles(req);

		String name = req.getParameter("name");
		String mail = req.getParameter("mail");
		String subject = req.getParameter("subject");
		String content = req.getParameter("content");
		if (StringUtils.isBlank(subject) || StringUtils.isBlank(content)) {
			mav.addObject("errmsg", msg.getMessage("msg.mail.is.empty", null, locale));
			return doGet(mav, locale);
		}
		StringBuilder sb = new StringBuilder();
		sb.append(msg.getMessage("label.name", null, locale)).append(": ").append(name).append("<br>");
		sb.append(msg.getMessage("label.mail", null, locale)).append(": ").append(mail).append("<br>").append("<br>");
		sb.append(msg.getMessage("label.content", null, locale)).append(": ").append(content).append("<br>");
		try {
			sendMail(subject, sb.toString(), uploadedFiles);
			mav.addObject("msg", msg.getMessage("msg.sent.mail", null, locale));
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
		} finally {
			deleteUploadFiles(uploadedFiles);
		}
		return doGet(mav, locale);
	}

	private void sendMail(String subject, String text, List<File> uploadedFiles) throws MessagingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		helper.setText(text);
		helper.setSubject(subject);
		helper.setTo(adminAddress);
		for (File uploadedFile : uploadedFiles) {
			helper.addAttachment(uploadedFile.getName(), uploadedFile);
		}
		javaMailSender.send(message);
	}

	/**
	 * Saves files uploaded from the client and return a list of these files
	 * which will be attached to the mail message.
	 */
	private List<File> saveUploadedFiles(HttpServletRequest request) throws IOException, ServletException {
		List<File> listFiles = new ArrayList<>();
		byte[] buffer = new byte[4096];
		int bytesRead;
		Collection<Part> multiparts = request.getParts();
		if (!multiparts.isEmpty()) {
			for (Part part : request.getParts()) {
				// creates a file to be saved
				String fileName = extractFileName(part);
				if (StringUtils.isBlank(fileName)) {
					// not attachment part, continue
					continue;
				}

				File saveFile = new File(fileName);
				log.debug("Uploaded file is saved on: " + saveFile.getAbsolutePath());

				try (FileOutputStream outputStream = new FileOutputStream(saveFile);
						InputStream inputStream = part.getInputStream()) {

					// saves uploaded file
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						outputStream.write(buffer, 0, bytesRead);
					}
				} catch (Exception e) {
					log.error("Exception occurs: ", e);
				}
				listFiles.add(saveFile);
			}
		}
		return listFiles;
	}

	/**
	 * Retrieves file name of a upload part from its HTTP header
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf('=') + 2, s.length() - 1);
			}
		}
		return null;
	}

	/**
	 * Deletes all uploaded files, should be called after the e-mail was sent.
	 */
	private void deleteUploadFiles(List<File> listFiles) {
		if (listFiles != null && !listFiles.isEmpty()) {
			for (File aFile : listFiles) {
				if (!aFile.delete()) {
					log.debug("Cannot remove file: {}", aFile);
				}
			}
		}
	}
}
