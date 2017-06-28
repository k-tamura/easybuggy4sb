package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class NullByteInjectionController {

	private static final Logger log = LoggerFactory.getLogger(NullByteInjectionController.class);

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/nullbyteijct")
	public ModelAndView process(@RequestParam(value = "fileName", required = false) String fileName, ModelAndView mav,
			Locale locale, HttpServletRequest req, HttpServletResponse res) {
		mav.setViewName("nullbyteinjection");
		mav.addObject("title", msg.getMessage("title.guide.download", null, locale));

		if (StringUtils.isBlank(fileName)) {
			return mav;
		} else {
			fileName = fileName + ".pdf";
		}
		// Get absolute path of the web application
		String appPath = req.getServletContext().getRealPath("");
		File file = new File(appPath + File.separator + "pdf" + File.separator + fileName);
		if (!file.exists()) {
			return mav;
		}
		
		log.debug("File location on server::" + file.getAbsolutePath());
		try (InputStream fis = new FileInputStream(file); ServletOutputStream os = res.getOutputStream()) {
			String mimeType = req.getServletContext().getMimeType(file.getAbsolutePath());
			res.setContentType(mimeType != null ? mimeType : "application/octet-stream");
			res.setContentLength((int) file.length());
			res.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
			FileCopyUtils.copy(fis, os);
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
		}
		return null;
	}
}
