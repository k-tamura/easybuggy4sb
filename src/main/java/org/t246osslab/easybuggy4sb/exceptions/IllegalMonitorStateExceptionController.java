package org.t246osslab.easybuggy4sb.exceptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IllegalMonitorStateExceptionController {

    @RequestMapping(value = "/imse")
    public void process() {
        Thread thread = new Thread();
        thread.start();
        try {
            thread.wait();
        } catch (InterruptedException e) {
        }
    }
}
