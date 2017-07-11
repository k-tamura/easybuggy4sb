package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.core.model.User;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// 2MB, 10MB, 50MB
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
@Controller
public class XEEandXXEController {

	private static final Logger log = LoggerFactory.getLogger(XEEandXXEController.class);

	// Name of the directory where uploaded files is saved
	private static final String SAVE_DIR = "uploadFiles";

	@Autowired
	MessageSource msg;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@RequestMapping(value = { "/xee", "/xxe" }, method = RequestMethod.GET)
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
			throws IOException {

		Resource resource = new ClassPathResource("/xml/sample_users.xml");
		mav.addObject("sample_users_xml", FileUtils.readFileToString(resource.getFile()));
		if ("/xee".equals(req.getServletPath())) {
			mav.setViewName("xee");
			mav.addObject("title", msg.getMessage("title.xee", null, locale));
			resource = new ClassPathResource("/xml/xee.xml");
			mav.addObject("xee_xml", FileUtils.readFileToString(resource.getFile()));
		} else {
			mav.setViewName("xxe");
			mav.addObject("title", msg.getMessage("title.xxe", null, locale));
			resource = new ClassPathResource("/xml/xxe.xml");
			mav.addObject("xxe_xml", FileUtils.readFileToString(resource.getFile()));
			resource = new ClassPathResource("/xml/xxe.dtd");
			mav.addObject("xxe_dtd", FileUtils.readFileToString(resource.getFile()));
		}
		if (req.getAttribute("errorMessage") != null) {
			mav.addObject("msg", req.getAttribute("errorMessage"));
		}
		return mav;
	}

	@RequestMapping(value = { "/xee", "/xxe" }, method = RequestMethod.POST)
	public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale)
			throws IOException {

		// Get absolute path of the web application
		String appPath = req.getServletContext().getRealPath("");

		// Create a directory to save the uploaded file if it does not exists
		String savePath = appPath + File.separator + SAVE_DIR;
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdir();
		}

		// Save the file
		Part filePart = null;
		try {
			filePart = req.getPart("file");
		} catch (Exception e) {
			req.setAttribute("errorMessage", msg.getMessage("msg.max.file.size.exceed", null, locale));
			return doGet(mav, req, res, locale);
		}
		String fileName = getFileName(filePart);
		if (StringUtils.isBlank(fileName)) {
			return doGet(mav, req, res, locale);
		} else if (!fileName.endsWith(".xml")) {
			mav.addObject("errmsg", msg.getMessage("msg.not.xml.file", null, locale));
			return doGet(mav, req, res, locale);
		}
		boolean isRegistered = writeFile(savePath, filePart, fileName);

		CustomHandler customHandler = new CustomHandler();
		customHandler.setLocale(locale);
		SAXParser parser;
		try {
			File file = new File(savePath + File.separator + fileName);
			SAXParserFactory spf = SAXParserFactory.newInstance();
			if ("/xee".equals(req.getServletPath())) {
				customHandler.setInsert(true);
				spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
				spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} else {
				spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			}
			parser = spf.newSAXParser();
			parser.parse(file, customHandler);
			isRegistered = true;
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException occurs: ", e);
		} catch (SAXException e) {
			log.error("SAXException occurs: ", e);
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
		}

		if ("/xee".equals(req.getServletPath())) {
			if (isRegistered && customHandler.isRegistered()) {
				mav.addObject("msg", msg.getMessage("msg.batch.registration.complete", null, locale));
			} else {
				mav.addObject("errmsg", msg.getMessage("msg.batch.registration.fail", null, locale));
			}
			mav.setViewName("xee");
			mav.addObject("title", msg.getMessage("title.xee", null, locale));
		} else {
			if (isRegistered && customHandler.isRegistered()) {
				mav.addObject("msg", msg.getMessage("msg.batch.update.complete", null, locale));
			} else {
				mav.addObject("errmsg", msg.getMessage("msg.batch.update.fail", null, locale));
			}
			mav.setViewName("xxe");
			mav.addObject("title", msg.getMessage("title.xxe", null, locale));
		}
		mav.addObject("resultList", customHandler.getResult());
		return mav;
	}

    private boolean writeFile(String savePath, Part filePart, String fileName) throws IOException {
		boolean isRegistered = false;
		try (OutputStream out = new FileOutputStream(savePath + File.separator + fileName);
				InputStream in = filePart.getInputStream()) {
			int read = 0;
			final byte[] bytes = new byte[1024];
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (FileNotFoundException e) {
			// Ignore because file already exists
			isRegistered = true;
		}
        return isRegistered;
    }

	// Get file name from content-disposition filename
	private String getFileName(final Part part) {
		for (String content : part.getHeader("content-disposition").split(";")) {
			if (content.trim().startsWith("filename")) {
				return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}

	public class CustomHandler extends DefaultHandler {
		ArrayList<Object> resultList = new ArrayList<>();
		private boolean isRegistered = false;
		private boolean isUsersExist = false;
		private boolean isInsert = false;
		private Locale locale = null;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if ("users".equals(qName)) {
				isUsersExist = true;

			} else if (isUsersExist && "user".equals(qName)) {
				String executeResult = upsertUser(attributes, locale);
				User user = new User();
				if (executeResult == null) {
					user.setUserId(attributes.getValue("uid"));
					user.setName(attributes.getValue("name"));
					user.setPassword(attributes.getValue("password"));
					user.setPhone(attributes.getValue("phone"));
					user.setMail(attributes.getValue("mail"));
					resultList.add(user);
				} else {
					resultList.add(attributes.getValue("uid") + " :: " + executeResult);
				}
				isRegistered = true;
			}
		}

		void setInsert(boolean isInsert) {
			this.isInsert = isInsert;
		}

		void setLocale(Locale locale) {
			this.locale = locale;
		}

		ArrayList<Object> getResult() {
			return resultList;
		}

		boolean isRegistered() {
			return isRegistered;
		}

		public String upsertUser(Attributes attributes, Locale locale) {
			String resultMessage = null;
			try {
			    int count = jdbcTemplate.queryForObject(
			            "select count(*) from users where id = ?", Integer.class, attributes.getValue("uid"));

				if (count == 1) {
					if (isInsert) {
						return msg.getMessage("msg.user.already.exist", null, locale);
					}
				} else {
					if (!isInsert) {
						return msg.getMessage("msg.user.not.exist", null, locale);
					}
				}
				if (isInsert) {
					int insertCount = jdbcTemplate.update("insert into users values (?, ?, ?, ?, ?, ?, ?)",
							attributes.getValue("uid"), attributes.getValue("name"), attributes.getValue("password"),
							RandomStringUtils.randomNumeric(10), "true", attributes.getValue("phone"),
							attributes.getValue("mail"));
					if (insertCount != 1) {
						return msg.getMessage("msg.user.already.exist", null, locale);
					}
				} else {
					int updateCount = jdbcTemplate.update(
							"update users set name = ?, password = ?, phone = ?, mail = ? where id = ?",
							attributes.getValue("name"), attributes.getValue("password"), attributes.getValue("phone"),
							attributes.getValue("mail"), attributes.getValue("uid"));
					if (updateCount != 1) {
						return msg.getMessage("msg.user.not.exist", null, locale);
					}
				}
			} catch (DataAccessException e) {
				resultMessage = msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, locale);
				log.error("DataAccessException occurs: ", e);
			} catch (Exception e) {
				resultMessage = msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, locale);
				log.error("Exception occurs: ", e);
			}
			return resultMessage;
		}
	}
}
