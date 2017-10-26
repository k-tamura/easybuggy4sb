package org.t246osslab.easybuggy4sb.troubles;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sun.misc.Unsafe;

@Controller
public class JVMCrashByEAVController {

    @RequestMapping(value = "/jvmcrasheav")
    public void process() {
        try {
            getUnsafe().getByte(0);
        } catch (Exception e) {
        }
    }

    private static Unsafe getUnsafe() throws NoSuchFieldException, IllegalAccessException {
        Field singleoneInstanceField = Unsafe.class.getDeclaredField("theUnsafe");
        singleoneInstanceField.setAccessible(true);
        return (Unsafe) singleoneInstanceField.get(null);
    }
}