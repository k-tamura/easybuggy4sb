package org.t246osslab.easybuggy4sb.controller;

import java.util.Locale;

import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
public abstract class AbstractController {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MessageSource msg;

    protected void setViewAndCommonObjects(ModelAndView mav, Locale locale, String viewName) {
        if (viewName != null) {
            mav.setViewName(viewName);
            try {
                mav.addObject("title", msg.getMessage("title." + viewName + ".page", null, locale));
            } catch (NoSuchMessageException e) {
                log.debug("'title." + viewName + ".page' is not in messages.properties", e);
                mav.addObject("title", "title." + viewName + ".page");
            }
            try {
                mav.addObject("note", msg.getMessage("msg.note." + viewName, null, locale));
            } catch (NoSuchMessageException e) {
                log.debug("'msg.note." + viewName + "' is not in messages.properties", e);
            }
        } else {
            log.warn("viewName is null");
        }
    }

    /**
     * Encode data for use in HTML using HTML entity encoding
     * Note that this method just call <code>ESAPI.encoder().encodeForHTML(String)</code>.
     *
     * @param input the text to encode for HTML
     * @return input encoded for HTML
     */
    protected String encodeForHTML(String input) {
        return ESAPI.encoder().encodeForHTML(input);
    }

    /**
     * Encode data for use in LDAP queries.
     * Note that this method just call <code>ESAPI.encoder().encodeForLDAP((String)</code>.
     *
     * @param input the text to encode for LDAP
     * @return input encoded for use in LDAP
     */
    protected String encodeForLDAP(String input) {
        return ESAPI.encoder().encodeForLDAP(input);
    }
}
