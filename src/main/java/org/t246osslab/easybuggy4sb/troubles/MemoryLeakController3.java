package org.t246osslab.easybuggy4sb.troubles;

import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.Deflater;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MemoryLeakController3 {

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/memoryleak3")
	public ModelAndView process(ModelAndView mav, Locale locale) {
		mav.setViewName("memoryleak3");
		mav.addObject("title", msg.getMessage("title.timezone", null, locale));
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
