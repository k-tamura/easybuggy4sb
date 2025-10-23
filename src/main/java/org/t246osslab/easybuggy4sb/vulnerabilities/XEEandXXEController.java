package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.model.User;
import org.t246osslab.easybuggy4sb.core.utils.MultiPartFileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@Controller
public class XEEandXXEController extends AbstractController {

	// Name of the directory where uploaded files is saved
	private static final String SAVE_DIR = "uploadFiles";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${attacker.app.url}")
	protected String attackerAppUrl;

	@RequestMapping(value = { "/xee", "/xxe" }, method = RequestMethod.GET)
	public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, Locale locale) throws IOException {

		Resource resource = new ClassPathResource("/xml/sample_users.xml");
		mav.addObject("sample_users_xml", IOUtils.toString(resource.getInputStream()));
		if ("/xee".equals(req.getServletPath())) {
		    setViewAndCommonObjects(mav, locale, "xee");
			resource = new ClassPathResource("/xml/xee.xml");
			mav.addObject("xee_xml", IOUtils.toString(resource.getInputStream()));
		} else {
            setViewAndCommonObjects(mav, locale, "xxe");
			String[] placeholders = new String[]{ attackerAppUrl + "/xxe/vulnerable.dtd" };
			mav.addObject("step1", msg.getMessage("msg.note.xxe.step1", placeholders, locale));
			resource = new ClassPathResource("/xml/xxe.xml");
			mav.addObject("xxe_xml", IOUtils.toString(resource.getInputStream()).replace("http://attacker.site", attackerAppUrl));
			resource = new ClassPathResource("/xml/xxe.dtd");
			mav.addObject("xxe_dtd", IOUtils.toString(resource.getInputStream()));
		}
		if (req.getAttribute("errorMessage") != null) {
			mav.addObject("errmsg", req.getAttribute("errorMessage"));
		}
		return mav;
	}

	@RequestMapping(value = { "/xee", "/xxe" }, headers=("content-type=multipart/*"), method = RequestMethod.POST)
    public ModelAndView doPost(@RequestParam("file") MultipartFile file, ModelAndView mav, HttpServletRequest req,
			Locale locale) throws IOException {

        if (req.getAttribute("errorMessage") != null) {
            return doGet(mav, req, locale);
        }

		// Get absolute path of the web application
		String appPath = req.getServletContext().getRealPath("");

		// Create a directory to save the uploaded file if it does not exist
		String savePath = (appPath == null ? System.getProperty("user.dir") : appPath) + File.separator + SAVE_DIR;
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdir();
		}

		String fileName = file.getOriginalFilename();
		if (StringUtils.isBlank(fileName)) {
			return doGet(mav, req, locale);
		} else if (!fileName.endsWith(".xml")) {
			mav.addObject("errmsg", msg.getMessage("msg.not.xml.file", null, locale));
			return doGet(mav, req, locale);
		}
		boolean isRegistered = MultiPartFileUtils.writeFile(savePath, file, fileName);

		CustomHandler customHandler = new CustomHandler();
		customHandler.setLocale(locale);
		SAXParser parser;
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			if ("/xee".equals(req.getServletPath())) {
				customHandler.setInsert();
				spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
				spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			} else {
				spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			}
			parser = spf.newSAXParser();
			parser.parse(new File(savePath + File.separator + fileName).getAbsolutePath(), customHandler);
			isRegistered = true;
		} catch (ParserConfigurationException e) {
			log.error("ParserConfigurationException occurs: ", e);
			mav.addObject("detailmsg", e.getMessage());
		} catch (SAXException e) {
			log.error("SAXException occurs: ", e);
			mav.addObject("detailmsg", e.getMessage());
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
			mav.addObject("detailmsg", e.getMessage());
		}

		if ("/xee".equals(req.getServletPath())) {
			if (isRegistered && customHandler.isRegistered()) {
				mav.addObject("msg", msg.getMessage("msg.batch.registration.complete", null, locale));
			} else {
				mav.addObject("errmsg", msg.getMessage("msg.batch.registration.fail", null, locale));
			}
            setViewAndCommonObjects(mav, locale, "xee");
		} else {
			if (isRegistered && customHandler.isRegistered()) {
				mav.addObject("msg", msg.getMessage("msg.batch.update.complete", null, locale));
			} else {
				mav.addObject("errmsg", msg.getMessage("msg.batch.update.fail", null, locale));
			}
            setViewAndCommonObjects(mav, locale, "xxe");
		}
        if (customHandler.getResult() != null && !customHandler.getResult().isEmpty()) {
            mav.addObject("resultList", customHandler.getResult());
            mav.addObject("note", null);
        }
		return doGet(mav, req, locale);
	}

	public class CustomHandler extends DefaultHandler {
		ArrayList<Object> resultList = new ArrayList<>();
		private boolean isRegistered = false;
		private boolean isUsersExist = false;
		private boolean isInsert = false;
		private Locale locale = null;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
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
					String uid = attributes.getValue("uid");
					if (uid == null) {
						StringBuilder attributeString = new StringBuilder();
						for (int i = 0; i < attributes.getLength(); i++) {
							attributeString.append(attributes.getQName(i) + "=" + attributes.getValue(i));
							if (i < attributes.getLength() - 1) attributeString.append(",");
						}
						resultList.add(new String[]{attributeString.toString(), executeResult});
					} else {
						resultList.add(new String[]{attributes.getValue("uid"), executeResult});
					}
				}
				isRegistered = true;
			}
		}

		void setInsert() {
			this.isInsert = true;
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

		String upsertUser(Attributes attributes, Locale locale) {
			String resultMessage = null;
			try {
				String uid = attributes.getValue("uid");
				if (uid == null) return msg.getMessage("msg.uid.not.exist", null, locale);
				String name = attributes.getValue("name");
				String password = attributes.getValue("password");
				String phone = attributes.getValue("phone");
				String mail = attributes.getValue("mail");
				int count = jdbcTemplate.queryForObject(
						"select count(*) from users where id = ?", Integer.class, uid);

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
							uid, name, password, RandomStringUtils.randomNumeric(10), "true", phone, mail);
					if (insertCount != 1) {
						return msg.getMessage("msg.user.already.exist", null, locale);
					}
				} else {
					int updateCount = jdbcTemplate.update(
							"update users set name = ?, password = ?, phone = ?, mail = ? where id = ?",
							name, password, phone, mail, uid);
					if (updateCount != 1) {
						return msg.getMessage("msg.user.not.exist", null, locale);
					}
				}
			} catch (DataAccessException e) {
				resultMessage = msg.getMessage("msg.db.access.error.occur", null, locale) + e.getMessage();
				log.error("DataAccessException occurs: ", e);
			} catch (Exception e) {
				resultMessage = msg.getMessage("msg.unknown.exception.occur", null, locale) + e.getMessage();
				log.error("Exception occurs: ", e);
			}
			return resultMessage;
		}
	}
}
