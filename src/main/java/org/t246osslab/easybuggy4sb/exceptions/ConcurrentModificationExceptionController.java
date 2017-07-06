package org.t246osslab.easybuggy4sb.exceptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ConcurrentModificationExceptionController {

	@RequestMapping(value = "/cme")
	public void process() {
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");

        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            String s = iter.next();
            if ("2".equals(s)) {
                list.remove(s);
            }
        }
	}
}