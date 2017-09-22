package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javassist.CannotCompileException;
import javassist.ClassPool;

@Controller
public class MemoryLeakController2 extends AbstractController {

	private int i = 0;

	@RequestMapping(value = "/memoryleak2")
	public ModelAndView process(ModelAndView mav, Locale locale) {
        setViewAndCommonObjects(mav, locale, "memoryleak");
        mav.addObject("title", msg.getMessage("title.memoryleak2.page", null, locale));
        String permName = System.getProperty("java.version").startsWith("1.7")
                ? msg.getMessage("label.permgen.space", null, locale) : msg.getMessage("label.metaspace", null, locale);
        mav.addObject("note", msg.getMessage("msg.permgen.space.leak.occur", new Object[] { permName }, locale));
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
