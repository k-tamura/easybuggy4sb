package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.utils.MultiPartFileUtils;

@Controller
public class UnrestrictedSizeUploadController extends AbstractController {

    // Name of the directory where uploaded files is saved
    private static final String SAVE_DIR = "uploadFiles";

    @RequestMapping(value = "/ursupload", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "unrestrictedsizeupload");
        return mav;
    }

    @RequestMapping(value = "/ursupload", headers=("content-type=multipart/*"), method = RequestMethod.POST)
    public ModelAndView doPost(@RequestParam("file") MultipartFile file, ModelAndView mav, HttpServletRequest req,
                               Locale locale) throws IOException {
        setViewAndCommonObjects(mav, locale, "unrestrictedsizeupload");

        // Get absolute path of the web application
        String appPath = req.getServletContext().getRealPath("");

        // Create a directory to save the uploaded file if it does not exists
        String savePath = (appPath == null ? System.getProperty("user.dir") : appPath) + File.separator + SAVE_DIR;
        File fileSaveDir = new File(savePath);
        if (!fileSaveDir.exists()) {
            fileSaveDir.mkdir();
        }

        String fileName = file.getOriginalFilename();
        if (StringUtils.isBlank(fileName)) {
            return doGet(mav, locale);
        } else if (!isImageFile(fileName)) {
            mav.addObject("errmsg", msg.getMessage("msg.not.image.file", null, locale));
            return doGet(mav, locale);
        }
        boolean isConverted = MultiPartFileUtils.writeFile(savePath, file, fileName);

        if (!isConverted) {
            isConverted = reverseColor(new File(savePath + File.separator + fileName).getAbsolutePath());
        }
        
        if (isConverted) {
            mav.addObject("msg", msg.getMessage("msg.reverse.color.complete", null, locale));
            mav.addObject("upladFilePath", SAVE_DIR + "/" + fileName);
        } else {
            mav.addObject("errmsg", msg.getMessage("msg.reverse.color.fail", null, locale));
            mav.addObject("note", msg.getMessage("msg.note.unrestrictedsizeupload", null, locale));
        }
        return mav;
    }

    private boolean isImageFile(String fileName) {
        return Arrays.asList("png", "gif", "jpg", "jpeg", "tif", "tiff", "bmp").contains(
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
