package org.t246osslab.easybuggy4sb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
 
@Controller
public class IndexController {
    
    @RequestMapping(value="/")
    public ModelAndView init(ModelAndView mav) {
        mav.setViewName("index");
        mav.addObject("title", "EasyBuggy");
        return mav;
    }
}