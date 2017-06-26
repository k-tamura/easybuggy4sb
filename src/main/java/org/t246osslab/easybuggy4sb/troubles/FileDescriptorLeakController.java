package org.t246osslab.easybuggy4sb.troubles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
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
public class FileDescriptorLeakController {

    private static final int MAX_DISPLAY_COUNT = 15;
    private static final Logger log = LoggerFactory.getLogger(FileDescriptorLeakController.class);
    private long count = 0;

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/filedescriptorleak")
    public ModelAndView process(@RequestParam(value = "pingurl", required = false) String pingURL,
            HttpServletRequest req, ModelAndView mav, Locale locale) {

        mav.setViewName("filedescriptorleak");
        mav.addObject("title", msg.getMessage("title.access.history", null, locale));
        try {
            File file = new File(req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString(),
                    "history.csv");
            FileOutputStream fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(new Date().toString() + ",");
            osw.write(req.getRemoteAddr() + ",");
            osw.write(req.getRequestedSessionId());
            osw.write(System.getProperty("line.separator"));
            osw.flush();
            count++;

            ArrayList<String[]> history = new ArrayList<>();
            BufferedReader br = new BufferedReader(new FileReader(file));
            int headerLength = 0;
            String line;
            long currentLineNum = 0;
            while ((line = br.readLine()) != null) {
                if (count - currentLineNum <= MAX_DISPLAY_COUNT) {
                    history.add(headerLength, line.split(","));
                }
                currentLineNum++;
            }
            mav.addObject("history", history);

        } catch (Exception e) {
            log.error("Exception occurs: ", e);
            mav.addObject("errmsg",
                    msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));
        }
        return mav;
    }
}
