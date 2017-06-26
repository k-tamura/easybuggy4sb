package org.t246osslab.easybuggy4sb.troubles;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class NetworkSocketLeakController {

    private static final Logger log = LoggerFactory.getLogger(NetworkSocketLeakController.class);

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/netsocketleak")
    public ModelAndView process(@RequestParam(value = "pingurl", required = false) String pingURL,
            HttpServletRequest req, ModelAndView mav, Locale locale) {
        mav.setViewName("netsocketleak");
        mav.addObject("title", msg.getMessage("title.response.time", null, locale));
        HttpURLConnection connection = null;
        URL url = null;
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
