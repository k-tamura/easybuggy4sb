package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class MemoryLeakController extends AbstractController {

    private HashMap<String, String> cache = new HashMap<>();

	@RequestMapping(value = "/memoryleak")
	public ModelAndView process(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "memoryleak");
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
