package org.t246osslab.easybuggy4sb.errors;

import java.nio.ByteBuffer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OutOfMemoryErrorController6 {

	@RequestMapping(value = "/oome6")
	public void process(ModelAndView mav) {
		mav.addObject("oome6", ByteBuffer.allocateDirect(99999999));
    }
}
