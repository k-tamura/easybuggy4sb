package org.t246osslab.easybuggy4sb.exceptions;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexOutOfBoundsExceptionController {

    @RequestMapping(value = "/ioobe")
    public void process() {
        new ArrayList<String>().get(1);
    }
}
