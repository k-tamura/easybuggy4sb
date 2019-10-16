package org.t246osslab.easybuggy4sb.troubles;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class LivelockController extends AbstractController {

	private static final Lock lock1 = new ReentrantLock(true);
	private static final Lock lock2 = new ReentrantLock(true);
	private boolean switchFlag = true;

	@RequestMapping(value = "/livelock")
	public ModelAndView process(HttpSession ses, ModelAndView mav, Locale locale) {
		setViewAndCommonObjects(mav, locale, "livelock");

		if (ses.getAttribute("dlpinit") == null) {
			ses.setAttribute("dlpinit", "true");
		} else {
			todoRemove();
		}
		
		/* Get threads that are in livelock waiting */
		ThreadMXBean bean = ManagementFactory.getThreadMXBean();
		mav.addObject("dumpAllThreads", bean.dumpAllThreads(true, true));
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

		while (!lock1.tryLock()) {
			log.debug(Thread.currentThread().getName() + ": Wait for lock 1.");
			sleep();
		}
		log.debug(Thread.currentThread().getName() + ": Hold lock 1.");
		sleep();

		try {
			while (!lock2.tryLock()) {
				log.debug(Thread.currentThread().getName() + ": Wait for lock 2.");
				sleep();
			}
			log.debug(Thread.currentThread().getName() + ": Hold lock 2.");

			try {
				log.debug(Thread.currentThread().getName() + ": Do anything.");
			} finally {
				lock2.unlock();
				log.debug(Thread.currentThread().getName() + ": Release lock 2.");
			}
		} finally {
			lock1.unlock();
			log.debug(Thread.currentThread().getName() + ": Release lock 1.");
		}

	}

	private void lock21() {

		while (!lock2.tryLock()) {
			log.debug(Thread.currentThread().getName() + ": Wait for lock 2.");
			sleep();
		}
		log.debug(Thread.currentThread().getName() + ": Hold lock 2.");
		sleep();

		try {
			while (!lock1.tryLock()) {
				log.debug(Thread.currentThread().getName() + ": Wait for lock 1.");
				sleep();
			}
			log.debug(Thread.currentThread().getName() + ": Hold lock 1.");

			try {
				log.debug(Thread.currentThread().getName() + ": Do something.");
			} finally {
				lock1.unlock();
				log.debug(Thread.currentThread().getName() + ": Release lock 1.");
			}
		} finally {
			lock2.unlock();
			log.debug(Thread.currentThread().getName() + ": Release lock 2.");
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
