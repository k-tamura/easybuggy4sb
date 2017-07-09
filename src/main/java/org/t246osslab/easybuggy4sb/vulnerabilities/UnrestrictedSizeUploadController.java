package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.FilenameUtils;
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
public class UnrestrictedSizeUploadController {

    private static final Logger log = LoggerFactory.getLogger(UnrestrictedSizeUploadController.class);

    @Autowired
    MessageSource msg;

    // Name of the directory where uploaded files is saved
    private static final String SAVE_DIR = "uploadFiles";

    @RequestMapping(value = "/ursupload", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) {
        
        mav.setViewName("unrestrictedsizeupload");
        mav.addObject("title", msg.getMessage("title.unrestricted.size.upload", null, locale));
        return mav;
    }

    @RequestMapping(value = "/ursupload", method = RequestMethod.POST)
    public ModelAndView doPost(ModelAndView mav, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        
        mav.setViewName("unrestrictedsizeupload");
        mav.addObject("title", msg.getMessage("title.unrestricted.size.upload", null, locale));

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
        } else if (!isImageFile(fileName)) {
            mav.addObject("errmsg", msg.getMessage("msg.not.image.file", null, locale));
            return doGet(mav, req, res, locale);
        }
        boolean isConverted = writeFile(savePath, filePart, fileName);

        if (!isConverted) {
            isConverted = reverseColor(new File(savePath + File.separator + fileName).getAbsolutePath());
        }
        
        if (isConverted) {
            mav.addObject("msg", msg.getMessage("msg.reverse.color.complete", null, locale));
            mav.addObject("upladFilePath", SAVE_DIR + "/" + fileName);
        } else {
            mav.addObject("errmsg", msg.getMessage("msg.reverse.color.fail", null, locale));
        }
        return mav;
    }

    private boolean writeFile(String savePath, Part filePart, String fileName) throws IOException {
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
        return isConverted;
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

    private boolean isImageFile(String fileName) {
        return Arrays.asList(new String[] { "png", "gif", "jpg", "jpeg", "tif", "tiff", "bmp" }).contains(
                FilenameUtils.getExtension(fileName));
    }
    
    // Reverse the color of the image file
    private boolean reverseColor(String fileName) throws IOException {
        boolean isConverted = false;
        try {
            BufferedImage image = ImageIO.read(new File(fileName));
            WritableRaster raster = image.getRaster();
            int[] pixelBuffer = new int[raster.getNumDataElements()];
            for (int y = 0; y < raster.getHeight(); y++) {
                for (int x = 0; x < raster.getWidth(); x++) {
                    raster.getPixel(x, y, pixelBuffer);
                    pixelBuffer[0] = ~pixelBuffer[0];
                    pixelBuffer[1] = ~pixelBuffer[1];
                    pixelBuffer[2] = ~pixelBuffer[2];
                    raster.setPixel(x, y, pixelBuffer);
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
