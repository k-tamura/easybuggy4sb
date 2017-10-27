package org.t246osslab.easybuggy4sb.performance;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class StringPlusOperationController extends AbstractController {

    private static final int MAX_LENGTH = 1000000;
    private static final String[] ALL_NUMBERS = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };
    private static final String[] ALL_UPPER_CHARACTERS = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
    private static final String[] ALL_LOWER_CHARACTERS = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l",
            "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
    private static final String[] ALL_SIGNS = { "!", "#", "$", "%", "&", "(", ")", "*", "+", ",", "-", ".", "/", ":",
            ";", "<", "=", ">", "?", "@", "[", "]", "^", "_", "{", "|", "}" };

    @RequestMapping(value = "/strplusopr")
    public ModelAndView process(@RequestParam(value = "length", required = false) String strLength,
            @RequestParam(value = "characters", required = false) String[] characters, ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "strplusopr");
        try {
            int length = NumberUtils.toInt(strLength, 0);
            StringBuilder html = createMainContent(characters, mav, locale, length);
            mav.addObject("html", html.toString());
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        }
        return mav;
    }

    private StringBuilder createMainContent(String[] characters, ModelAndView mav, Locale locale, int length) {
        StringBuilder html = new StringBuilder();
        html.append("<p>" + msg.getMessage("label.available.characters", null, locale) + "</p>");

        appendCheckBox(characters, locale, html, ALL_NUMBERS, "label.numbers");
        appendCheckBox(characters, locale, html, ALL_UPPER_CHARACTERS, "label.uppercase.characters");
        appendCheckBox(characters, locale, html, ALL_LOWER_CHARACTERS, "label.lowercase.characters");
        appendCheckBox(characters, locale, html, ALL_SIGNS, "label.signs");

        html.append("<input type=\"submit\" value=\"" + msg.getMessage("label.submit", null, locale) + "\">");
        html.append("<br /><br />");

        if (length > 0) {
            mav.addObject("length", length);
            // StringBuilder builder = new StringBuilder();
            String s = "";
            if (characters != null) {
                java.util.Random rand = new java.util.Random();
                log.info("Start Date: {}", new Date());
                for (int i = 0; i < length && i < MAX_LENGTH; i++) {
                    s = s + characters[rand.nextInt(characters.length)];
                    // builder.append(characters[rand.nextInt(characters.length)]);
                }
                log.info("End Date: {}", new Date());
            }
            html.append(msg.getMessage("label.execution.result", null, locale));
            html.append("<br /><br />");
            // message.append(encodeForHTML(builder.toString()));
            html.append(encodeForHTML(s));
        } else {
            html.append(msg.getMessage("msg.enter.positive.number", null, locale));
        }
        return html;
    }

    private void appendCheckBox(String[] characters, Locale locale, StringBuilder message, String[] allCharacters,
            String label) {
        message.append("<p>" + msg.getMessage(label, null, locale) + "</p>");
        message.append("<p>");
        for (String allCharacter : allCharacters) {
            message.append("<input type=\"checkbox\" name=\"characters\" value=\"");
            message.append(allCharacter);
            if (characters == null || Arrays.asList(characters).contains(allCharacter)) {
                message.append("\" checked=\"checked\">");
            } else {
                message.append("\">");
            }
            message.append(allCharacter);
            message.append(" ");
        }
        message.append("</p>");
    }
}
