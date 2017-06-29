package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javassist.CannotCompileException;
import javassist.ClassPool;

@Controller
public class MemoryLeakController2 {

	private int i = 0;

	private static final Logger log = LoggerFactory.getLogger(MemoryLeakController2.class);

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/memoryleak2")
	public ModelAndView process(ModelAndView mav, Locale locale) {
		mav.setViewName("memoryleak");
		mav.addObject("title", msg.getMessage("title.nonheap.memory.usage", null, locale));
		mav.addObject("note", msg.getMessage("msg.permgen.space.leak.occur", null, locale));
		try {
			toDoRemove();

			List<MemoryPoolMXBean> nonHeapPoolMXBeans = new ArrayList<>();
			List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
			for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
				if (MemoryType.NON_HEAP.equals(memoryPoolMXBean.getType())) {
					nonHeapPoolMXBeans.add(memoryPoolMXBean);
				}
			}
			mav.addObject("memoryPoolMXBeans", nonHeapPoolMXBeans);

		} catch (Exception e) {
			log.error("Exception occurs: ", e);
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, null, locale));

		}
		return mav;
	}

	private void toDoRemove() throws CannotCompileException {
		int j = i + 1000;
		ClassPool pool = ClassPool.getDefault();
		for (; i < j; i++) {
			pool.makeClass("org.t246osslab.easybuggy.core.model.TestClass" + i).toClass();
		}
	}
}
