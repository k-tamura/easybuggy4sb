package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.Deflater;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class MemoryLeakController3 extends AbstractController {

	@RequestMapping(value = "/memoryleak3")
	public ModelAndView process(ModelAndView mav, Locale locale) {
	    setViewAndCommonObjects(mav, locale, "memoryleak3");
        mav.addObject("timeZone", TimeZone.getDefault());
        toDoRemove();
		return mav;
	}

	private void toDoRemove() {
        String inputString = "inputString";
        byte[] input = inputString.getBytes();
        byte[] output = new byte[100];
        for (int i = 0; i < 1000; i++) {
            Deflater compresser = new Deflater();
            compresser.setInput(input);
            compresser.deflate(output);
        }
	}
}
