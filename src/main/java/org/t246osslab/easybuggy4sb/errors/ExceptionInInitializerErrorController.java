package org.t246osslab.easybuggy4sb.errors;

import java.lang.reflect.Constructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExceptionInInitializerErrorController {
	
    private static final Logger log = LoggerFactory.getLogger(ExceptionInInitializerErrorController.class);

	@RequestMapping(value = "/eie")
	public void process() {
        try {
            Class<?> cl = Class.forName("org.t246osslab.easybuggy4sb.errors.InitializerErrorThrower");
            Constructor<?> cunstructor = cl.getConstructor();
            cunstructor.newInstance(new Object[] { null });
        } catch (Exception e) {
            log.error("Exception occurs: ", e);
        }
    }
}

class InitializerErrorThrower {
    static {
        LoggerFactory.getLogger(InitializerErrorThrower.class).debug("clinit" + 1 / 0);
    }
    
    private InitializerErrorThrower(){
        // this constructor is added to suppress sonar advice
    }
}