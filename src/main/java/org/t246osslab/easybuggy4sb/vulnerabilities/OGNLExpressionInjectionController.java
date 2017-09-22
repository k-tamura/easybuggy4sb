package org.t246osslab.easybuggy4sb.vulnerabilities;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

@Controller
public class OGNLExpressionInjectionController extends AbstractController {

    @RequestMapping(value = "/ognleijc")
    public ModelAndView process(@RequestParam(value = "expression", required = false) String expression,
            ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "commandinjection");
        Object value = null;
        String errMessage = "";
        OgnlContext ctx = new OgnlContext();
        if (!StringUtils.isBlank(expression)) {
            try {
                Object expr = Ognl.parseExpression(expression.replaceAll("Math\\.", "@Math@"));
                value = Ognl.getValue(expr, ctx);
            } catch (OgnlException e) {
                if (e.getReason() != null) {
                    errMessage = e.getReason().getMessage();
                }
                log.debug("OgnlException occurs: ", e);
            } catch (Exception e) {
                log.debug("Exception occurs: ", e);
            } catch (Error e) {
                log.debug("Error occurs: ", e);
            }
        }
        if (expression != null) {
            mav.addObject("expression", expression);
            if (value == null) {
                mav.addObject("errmsg",
                        msg.getMessage("msg.invalid.expression", new String[] { errMessage }, null, locale));
            }
        }
        if (value != null && NumberUtils.isNumber(value.toString())) {
            mav.addObject("value", value);
        }
        return mav;
    }
}
