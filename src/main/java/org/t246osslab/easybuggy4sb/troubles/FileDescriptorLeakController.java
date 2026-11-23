package org.t246osslab.easybuggy4sb.troubles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class FileDescriptorLeakController extends AbstractController {

    private static final int MAX_DISPLAY_COUNT = 15;
    // Do not share the file with ThreadStarvationController (history.csv)
    private static final String HISTORY_FILE_NAME = "fdleak_history.csv";

    @RequestMapping(value = "/filedescriptorleak")
    public ModelAndView process(HttpServletRequest req, ModelAndView mav, Locale locale) {

        setViewAndCommonObjects(mav, locale, "filedescriptorleak");
        try {
            File file = new File(req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString(),
                    HISTORY_FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(new Date() + ",");
            osw.write(req.getRemoteAddr() + ",");
            // The session ID is null if the browser has not sent a session cookie yet
            String sessionId = req.getRequestedSessionId();
            osw.write(sessionId != null ? sessionId : "-");
            osw.write(System.getProperty("line.separator"));
            osw.flush();

            LinkedList<String[]> history = new LinkedList<>();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // Keep only the latest records, the newest first
                history.addFirst(line.split(","));
                if (history.size() > MAX_DISPLAY_COUNT) {
                    history.removeLast();
                }
            }
            mav.addObject("history", history);

        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            mav.addObject("errmsg", msg.getMessage("msg.unknown.exception.occur", null, locale));
            mav.addObject("detailmsg", e.getMessage());
        }
        return mav;
    }
}
