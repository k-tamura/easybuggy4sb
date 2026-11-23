package org.t246osslab.easybuggy4sb.errors;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class ZipErrorController extends AbstractController {

	/*
	 * Opens an empty file as a ZIP file system. On Java 8 or earlier the ZIP file system provider throws
	 * ZipError for it; on Java 9 or later it throws ZipException (an IOException) instead.
	 */
	@RequestMapping(value = "/ze")
	public ModelAndView process(ModelAndView mav, HttpServletRequest req, Locale locale) {
		try {
			File file = new File(req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString(),
					"test.zip");
			file.createNewFile();
			// File#toURI builds a valid URI also for Windows paths (backslashes, spaces, ...)
			URI uri = URI.create("jar:" + file.toURI());
			try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<String, Object>())) {
				log.debug("Unexpectedly opened: {}", fs);
			}
		} catch (ZipException e) {
			log.info("ZipError is not thrown on this Java version: {}", e.getMessage());
			setViewAndCommonObjects(mav, locale, "errornotoccurred");
			mav.addObject("msg", msg.getMessage("msg.ziperror.not.thrown", null, locale));
			mav.addObject("errmsg", e.toString());
			return mav;
		} catch (IOException e) {
			log.error("IOException occurs: ", e);
			setViewAndCommonObjects(mav, locale, "errornotoccurred");
			mav.addObject("errmsg", msg.getMessage("msg.unknown.exception.occur", null, locale));
			mav.addObject("detailmsg", e.getMessage());
			return mav;
		}
		setViewAndCommonObjects(mav, locale, "errornotoccurred");
		return mav;
	}
}
