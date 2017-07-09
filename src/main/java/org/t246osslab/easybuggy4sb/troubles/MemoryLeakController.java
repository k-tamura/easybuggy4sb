package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MemoryLeakController {

    private HashMap<String, String> cache = new HashMap<>();

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/memoryleak")
	public ModelAndView process(ModelAndView mav, Locale locale) {
		mav.setViewName("memoryleak");
		mav.addObject("title", msg.getMessage("title.heap.memory.usage", null, locale));
		mav.addObject("note", msg.getMessage("msg.java.heap.space.leak.occur", null, locale));
        toDoRemove();

        List<MemoryPoolMXBean> heapPoolMXBeans = new ArrayList<>();
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            if (MemoryType.HEAP.equals(memoryPoolMXBean.getType())) {
                heapPoolMXBeans.add(memoryPoolMXBean);
            }
        }
        mav.addObject("memoryPoolMXBeans", heapPoolMXBeans);
		return mav;
	}

	private void toDoRemove() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("Memory leak occurs!");
        }
        cache.put(String.valueOf(sb.hashCode()), sb.toString());
	}
}
