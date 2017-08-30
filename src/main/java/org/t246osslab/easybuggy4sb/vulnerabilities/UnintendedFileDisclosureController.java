package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UnintendedFileDisclosureController {

    private static final Logger log = LoggerFactory.getLogger(UnintendedFileDisclosureController.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/clientinfo")
    public void clientinfo(HttpServletResponse res, Locale locale) throws IOException {
        Resource resource = new ClassPathResource("/templates/clientinfo.html");
        String htmlString = IOUtils.toString(resource.getInputStream());
        htmlString = repacLocalizedString(htmlString, locale);
        res.getWriter().write(htmlString);
    }

    @RequestMapping(value = "/serverinfo")
    public void serverinfo(HttpSession ses, HttpServletResponse res, Locale locale) throws IOException {
        StringBuilder sb = new StringBuilder();
        Properties properties = System.getProperties();
        for (Object key : properties.keySet()) {
            Object value = properties.get(key);
            sb.append("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
        }
        Resource resource = new ClassPathResource("/templates/serverinfo.html");
        String htmlString = IOUtils.toString(resource.getInputStream());
        String userid = (String) ses.getAttribute("userid");
        if(userid == null){
            res.sendRedirect("/");
            return;
        }
        htmlString = htmlString.replace("<!-- [REPLACE:@UserId] -->", userid);
        htmlString = htmlString.replace("<!-- [REPLACE:@Contents] -->", sb.toString());
        htmlString = repacLocalizedString(htmlString, locale);
        res.getWriter().write(htmlString);
    }

    private String repacLocalizedString(String htmlString, Locale locale) {
        while (true) {
            int startIndex = htmlString.indexOf("<!-- [REPLACE:");
            int endIndex = htmlString.indexOf("] -->");
            if (startIndex < 0 || endIndex < 0) {
                break;
            }
            String keyString = htmlString.substring(startIndex + 14, endIndex);
            try {
                htmlString = htmlString.replace("<!-- [REPLACE:" + keyString + "] -->",
                        msg.getMessage(keyString, null, locale));
            } catch (NoSuchMessageException e) {
                log.warn("{} is not defined in message.properties", keyString, e);
                break;
            }
        }
        return htmlString;
    }
}