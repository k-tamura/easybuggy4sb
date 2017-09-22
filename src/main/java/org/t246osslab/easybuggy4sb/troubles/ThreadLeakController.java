package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class ThreadLeakController extends AbstractController {

    @RequestMapping(value = "/threadleak")
    public ModelAndView process(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "threadleak");
        ThreadCountLoggingThread sub = new ThreadCountLoggingThread();
        sub.start();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        mav.addObject("count", bean.getAllThreadIds().length);
        return mav;
    }
}

class ThreadCountLoggingThread extends Thread {

    private static final Logger log = LoggerFactory.getLogger(ThreadCountLoggingThread.class);

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100000);
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();
                log.info("Current thread count: {}", bean.getAllThreadIds().length);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
