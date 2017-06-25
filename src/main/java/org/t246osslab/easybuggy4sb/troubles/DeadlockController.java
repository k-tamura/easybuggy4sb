package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Locale;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DeadlockController {

	private static final Logger log = LoggerFactory.getLogger(DeadlockController.class);

	private final Object lock1 = new Object();
	private final Object lock2 = new Object();
	private boolean switchFlag = true;

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/deadlock")
	public ModelAndView process(HttpSession ses, ModelAndView mav, Locale locale) {
		mav.setViewName("deadlock");
		mav.addObject("title", msg.getMessage("title.detect.deadlock", null, locale));

		try {
			if (ses.getAttribute("dlpinit") == null) {
				ses.setAttribute("dlpinit", "true");
			} else {
				todoRemove();
			}

			ThreadMXBean bean = ManagementFactory.getThreadMXBean();
			long[] threadIds = bean.findDeadlockedThreads();
			if (threadIds != null) {
				mav.addObject("msg", msg.getMessage("msg.dead.lock.detected", null, locale));
				ThreadInfo[] infos = bean.getThreadInfo(threadIds);
				mav.addObject("threadsInfo", infos);
			} else {
				mav.addObject("msg", msg.getMessage("msg.dead.lock.not.occur", null, locale));
			}
		} catch (Exception e) {
			log.error("Exception occurs: ", e);
			mav.addObject("errmsg",
					msg.getMessage("msg.unknown.exception.occur", new String[] { e.getMessage() }, locale));
		}
		return mav;
	}

	private void todoRemove() {
		switchFlag = !switchFlag;
		if (switchFlag) {
			lock12();
		} else {
			lock21();
		}
	}

	private void lock12() {
		synchronized (lock1) {
			sleep();
			synchronized (lock2) {
				sleep();
			}
		}
	}

	private void lock21() {
		synchronized (lock2) {
			sleep();
			synchronized (lock1) {
				sleep();
			}
		}
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			log.error("Exception occurs: ", e);
		}
	}
}
