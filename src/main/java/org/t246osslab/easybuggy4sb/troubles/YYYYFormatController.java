package org.t246osslab.easybuggy4sb.troubles;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class YYYYFormatController extends AbstractController {

    @RequestMapping(value = "/yyyyformat")
    public ModelAndView process(@RequestParam(value = "date", required = false) String strDate, ModelAndView mav,
            Locale locale) {
        setViewAndCommonObjects(mav, locale, "yyyyformat");
        if (StringUtils.isBlank(strDate)) {
            mav.addObject("msg", msg.getMessage("msg.enter.date", null, locale));
            return mav;
        }
        mav.addObject("date", strDate);
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
            parser.setLenient(false);
            Date date = parser.parse(strDate);

            SimpleDateFormat formatter = new SimpleDateFormat("YYYY/MM/dd (E)", locale);
            mav.addObject("msg", msg.getMessage("msg.formatted.date", new Object[] { formatter.format(date) }, locale));
        } catch (ParseException e) {
            mav.addObject("errmsg", msg.getMessage("msg.enter.date", null, locale));
        }
        return mav;
    }
}
