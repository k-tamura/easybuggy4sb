package org.t246osslab.easybuggy4sb.exceptions;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IllegalArgumentExceptionController {

    @RequestMapping(value = "/iae")
    public void process(ModelAndView mav) {
        mav.addObject(new ArrayList<Object>(-1));
    }
}
