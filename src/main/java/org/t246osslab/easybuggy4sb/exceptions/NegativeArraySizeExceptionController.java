package org.t246osslab.easybuggy4sb.exceptions;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class NegativeArraySizeExceptionController {

    @RequestMapping(value = "/nase")
    public void process(ModelAndView mav) {
        mav.addObject("nase", new int[-1]);
    }
}
