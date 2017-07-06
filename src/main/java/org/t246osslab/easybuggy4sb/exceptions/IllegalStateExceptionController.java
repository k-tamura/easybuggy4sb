package org.t246osslab.easybuggy4sb.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IllegalStateExceptionController {

    @RequestMapping(value = "/iase")
    public void process() {
        List<String> alphabet = new ArrayList<>(Arrays.asList("a", "b, c"));
        for (final Iterator<String> itr = alphabet.iterator(); itr.hasNext();) {
            itr.remove();
        }
    }
}
