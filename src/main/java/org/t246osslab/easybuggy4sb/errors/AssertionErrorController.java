package org.t246osslab.easybuggy4sb.errors;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AssertionErrorController {

    @RequestMapping(value = "/asserr")
    public void process() {
        assert false : "Invalid!";
    }
}
