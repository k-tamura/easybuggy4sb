package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

@Controller
public class OGNLExpressionInjectionController {

    private static final Logger log = LoggerFactory.getLogger(SQLInjectionController.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/ognleijc")
	public ModelAndView process(@RequestParam(value = "expression", required = false) String expression,
			ModelAndView mav, Locale locale) {
		mav.setViewName("commandinjection");
		mav.addObject("title", msg.getMessage("title.ognl.expression.injection.page", null, locale));
        try {
            Object value = null;
            String errMessage = "";
            boolean isValid = true;
            OgnlContext ctx = new OgnlContext();
            if (StringUtils.isBlank(expression)) {
                isValid = false;
            } else {
                try {
                    Object expr = Ognl.parseExpression(expression.replaceAll("Math\\.", "@Math@"));
                    value = Ognl.getValue(expr, ctx);
                } catch (OgnlException e) {
                    isValid = false;
                    if (e.getReason() != null) {
                        errMessage = e.getReason().getMessage();
                    }
                    log.debug("OgnlException occurs: ", e);
                } catch (Exception e) {
                    isValid = false;
                    log.debug("Exception occurs: ", e);
                } catch (Error e) {
                    isValid = false;
                    log.debug("Error occurs: ", e);
                }
            }
			if (expression != null) {
				mav.addObject("expression", expression);
			}
            if (isValid && value != null && NumberUtils.isNumber(value.toString())) {
        		mav.addObject("value", value);
            }
            if ((!isValid || value == null) && expression != null) {
        		mav.addObject("errmsg", msg.getMessage("msg.invalid.expression", new String[] { errMessage }, null, locale));
            }

        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        }
        return mav;
    }
}
