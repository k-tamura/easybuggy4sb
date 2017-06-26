package org.t246osslab.easybuggy4sb.troubles;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import sun.misc.Unsafe;

@Controller
public class JVMCrashByEAVController {

    @Autowired
    MessageSource msg;

    @RequestMapping(value = "/jvmcrasheav")
    public void process(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
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