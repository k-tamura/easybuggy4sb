package org.t246osslab.easybuggy4sb.vulnerabilities;

import ognl.DefaultMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

@Controller
public class OGNLExpressionInjectionController extends AbstractController {

    @RequestMapping(value = "/ognleijc")
    public ModelAndView process(@RequestParam(value = "expression", required = false) String expression,
                                ModelAndView mav, Locale locale) throws InterruptedException, IOException {
        setViewAndCommonObjects(mav, locale, "commandinjection");
        Object value = null;
        String errMessage = "";
        OgnlContext ctx = new OgnlContext();
        ctx.setMemberAccess(new DefaultMemberAccess(false));
        if (!StringUtils.isBlank(expression)) {
            try {
                Object expr = Ognl.parseExpression(expression.replaceAll("Math\\.", "@Math@"));
                value = Ognl.getValue(expr, ctx);

                // Wait until the process is complete and get the output if value is not string
                if (value instanceof Process) {
                    Process p = (Process) value;
                    p.waitFor();
                    String line;
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        while ((line = reader.readLine()) != null) {
                            value = line;
                        }
                    }
                }
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
                        msg.getMessage("msg.invalid.expression", null, null, locale));
                mav.addObject("detailmsg", errMessage);
            }
        }
        if (value != null && NumberUtils.isNumber(value.toString())) {
            mav.addObject("value", value);
        }
        return mav;
    }
}
