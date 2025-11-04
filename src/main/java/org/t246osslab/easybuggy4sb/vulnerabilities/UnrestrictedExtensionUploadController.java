package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;
import org.t246osslab.easybuggy4sb.core.utils.MultiPartFileUtils;

@Controller
public class UnrestrictedExtensionUploadController extends AbstractController {

    // Name of the directory where uploaded files is saved
    private static final String SAVE_DIR = "uploadFiles";

    @RequestMapping(value = "/ureupload", method = RequestMethod.GET)
    public ModelAndView doGet(ModelAndView mav, HttpServletRequest req, Locale locale) throws IOException {
        setViewAndCommonObjects(mav, locale, "unrestrictedextupload");
        String uploadFilesURL = req.getRequestURL().toString().replaceAll("/ureupload*.+", "/uploadFiles/");
        mav.addObject("note", msg.getMessage("msg.note.unrestrictedextupload", null, locale));
        mav.addObject("step1", msg.getMessage("msg.note.unrestrictedextupload.step1", new String[]{"command.jsp"}, locale));
        mav.addObject("step2", msg.getMessage("msg.note.unrestrictedextupload.step2", new String[]{uploadFilesURL + "command.jsp?cmd=tree"}, locale));
        mav.addObject("step3", msg.getMessage("msg.note.unrestrictedextupload.step3", new String[]{uploadFilesURL + "command.jsp?cmd=cat%20src/main/resources/application.properties"}, locale));
        mav.addObject("step4", msg.getMessage("msg.note.unrestrictedextupload.step4", new String[]{uploadFilesURL + "command.jsp?cmd=env"}, locale));
        mav.addObject("step5", msg.getMessage("msg.note.unrestrictedextupload.step5", new String[]{"select_users.jsp"}, locale));
        mav.addObject("step6", msg.getMessage("msg.note.unrestrictedextupload.step6", new String[]{uploadFilesURL + "select_users.jsp"}, locale));
        Resource resource = new ClassPathResource("/jsp/command.jsp");
        mav.addObject("command_jsp", IOUtils.toString(resource.getInputStream()));
        resource = new ClassPathResource("/jsp/select_users.jsp");
        mav.addObject("select_users_jsp", IOUtils.toString(resource.getInputStream()));
        if (req.getAttribute("errorMessage") != null) {
            mav.addObject("errmsg", req.getAttribute("errorMessage"));
        }
        return mav;
    }

    @RequestMapping(value = "/ureupload", headers=("content-type=multipart/*"), method = RequestMethod.POST)
    public ModelAndView doPost(@RequestParam("file") MultipartFile file, ModelAndView mav, HttpServletRequest req, Locale locale) throws IOException {
        
        if (req.getAttribute("errorMessage") != null) {
            return doGet(mav, req, locale);
        }

        setViewAndCommonObjects(mav, locale, "unrestrictedextupload");

        String exitJspURL = req.getRequestURL().toString().replaceAll("/ureupload*.+", "/uploadFiles/exit.jsp");
        String[] placeholders = new String[]{ exitJspURL};
        mav.addObject("note", msg.getMessage("msg.note.unrestrictedextupload", placeholders, locale));

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
            return doGet(mav, req, locale);
        }
        boolean isConverted = MultiPartFileUtils.writeFile(savePath, file, fileName);

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

    // Convert color image into gray scale image.
    private boolean convert2GrayScale(String fileName) {
        boolean isConverted = false;
        try {
            // Convert the file into gray scale image.
            BufferedImage image = ImageIO.read(new File(fileName));
            if (image == null) {
                log.warn("Cannot read upload file as image file, file name: {}", fileName);
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
