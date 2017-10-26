package org.t246osslab.easybuggy4sb.troubles;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class NetworkSocketLeakController extends AbstractController {

    @RequestMapping(value = "/netsocketleak")
    public ModelAndView process(ModelAndView mav, HttpServletRequest req, Locale locale) {
        setViewAndCommonObjects(mav, locale, "netsocketleak");
        HttpURLConnection connection;
        URL url;
        String pingURL = req.getParameter("pingurl");
        try {
            if (pingURL == null) {
                pingURL = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/ping";
            }
            url = new URL(pingURL);
            long start = System.currentTimeMillis();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            long end = System.currentTimeMillis();

            mav.addObject("pingURL", pingURL);
            mav.addObject("responseCode", responseCode);
            mav.addObject("responseTime", end - start);

        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            mav.addObject("errmsg",
                    msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
        }
        return mav;
    }
}
