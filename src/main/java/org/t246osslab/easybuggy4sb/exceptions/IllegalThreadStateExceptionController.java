package org.t246osslab.easybuggy4sb.exceptions;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IllegalThreadStateExceptionController {

    @RequestMapping(value = "/itse")
    public void process() {
        Runtime rt = Runtime.getRuntime();
		try {
			Process proc = rt.exec("javac");
	        proc.exitValue();
		} catch (IOException e) {
		}
    }
}
