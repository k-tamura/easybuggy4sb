package org.t246osslab.easybuggy4sb.exceptions;

import java.nio.charset.Charset;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UnsupportedCharsetExceptionController {

    @RequestMapping(value = "/uce")
    public void process(ModelAndView mav) {
        mav.addObject("uce", new String("str".getBytes(Charset.defaultCharset()), Charset.forName("test")));
    }
}
