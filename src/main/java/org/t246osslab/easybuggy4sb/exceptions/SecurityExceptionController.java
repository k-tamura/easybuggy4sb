package org.t246osslab.easybuggy4sb.exceptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SecurityExceptionController {

    @RequestMapping(value = "/se")
    public void process() {
        new SecurityManager().checkPermission(new RuntimePermission("exitVM"), null);
    }
}
