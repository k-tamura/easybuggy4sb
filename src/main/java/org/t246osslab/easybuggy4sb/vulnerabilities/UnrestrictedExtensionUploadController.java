package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
// 2MB, 10MB, 50MB
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 50)
public class UnrestrictedExtensionUploadController {

    private static final Logger log = LoggerFactory.getLogger(UnrestrictedExtensionUploadController.class);

    @Autowired
    MessageSource msg;

    // Name of the directory where uploaded files is saved
    private static final String SAVE_DIR = "uploadFiles";

    @RequestMapping(value = "/ureupload", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        
        mav.setViewName("unrestrictedextupload");
        mav.addObject("title", msg.getMessage("title.unrestricted.extension.upload", null, locale));
        return mav;
    }

    @RequestMapping(value = "/ureupload", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        
        mav.setViewName("unrestrictedextupload");
        mav.addObject("title", msg.getMessage("title.unrestricted.extension.upload", null, locale));

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
            mav.addObject("errmsg", msg.getMessage("msg.max.file.size.exceed", null, locale));
            return doGet(mav, req, res, locale);
        }
        String fileName = getFileName(filePart);
        if (StringUtils.isBlank(fileName)) {
            return doGet(mav, req, res, locale);
        }
        // TODO Remove this try block that is a workaround of issue #9 (FileNotFoundException on
        // Jetty * Windows)
        boolean isConverted = false;
        try (OutputStream out = new FileOutputStream(savePath + File.separator + fileName);
                InputStream in = filePart.getInputStream();) {
            int read = 0;
            final byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (FileNotFoundException e) {
            // Ignore because file already exists
            isConverted = true;
        }

        if (!isConverted) {
            isConverted = convert2GrayScale(new File(savePath + File.separator + fileName).getAbsolutePath());
        }
        
        if (isConverted) {
            mav.addObject("msg", msg.getMessage("msg.convert.grayscale.complete", null, locale));
            mav.addObject("upladFilePath", SAVE_DIR + "/" + fileName);
        } else {
            mav.addObject("errmsg", msg.getMessage("msg.convert.grayscale.fail", null, locale));
        }
        return mav;
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

    // Convert color image into gray scale image.
    private boolean convert2GrayScale(String fileName) throws IOException {
        boolean isConverted = false;
        try {
            // Convert the file into gray scale image.
            BufferedImage image = ImageIO.read(new File(fileName));
            if (image == null) {
                log.warn("Cannot read upload file as image file, file name: " + fileName);
                return false;
            }

            // convert to gray scale
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int p = image.getRGB(x, y);
                    int a = (p >> 24) & 0xff;
                    int r = (p >> 16) & 0xff;
                    int g = (p >> 8) & 0xff;
                    int b = p & 0xff;

                    // calculate average
                    int avg = (r + g + b) / 3;

                    // replace RGB value with avg
                    p = (a << 24) | (avg << 16) | (avg << 8) | avg;

                    image.setRGB(x, y, p);
                }
            }
            // Output the image
            ImageIO.write(image, "png", new File(fileName));
            isConverted = true;
        } catch (Exception e) {
            // Log and ignore the exception
            log.warn("Exception occurs: ", e);
        }
        return isConverted;
    }
}
