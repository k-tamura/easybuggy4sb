package org.t246osslab.easybuggy4sb.troubles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class ThreadStarvationController extends AbstractController {

	private static final String HISTORY_CSV_FILE_NAME = "history.csv";
	final Lock lock = new ReentrantLock();

	@RequestMapping(value = "/threadstarvation")
	public ModelAndView process(HttpServletRequest req, ModelAndView mav, Locale locale) throws IOException {

		setViewAndCommonObjects(mav, locale, "threadstarvation");

		try {
			lock.lock();
			String tempDir = req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString();
			Path path = Paths.get(tempDir, HISTORY_CSV_FILE_NAME);
			mav.addObject("note", msg.getMessage("msg.note.threadstarvation",
					new String[] { tempDir + File.separator + HISTORY_CSV_FILE_NAME }, locale));
			if (!Files.exists(path)) {
				Files.createFile(path);
			}

			List<String> listA = new ArrayList<>();
			listA.add(new Date().toString() + "," + req.getRemoteAddr() + "," + req.getRequestedSessionId());

			Files.write(path, listA, StandardOpenOption.APPEND);
			List<String> lines = Files.readAllLines(path);

			Map<String, String[]> aggregationMap = new HashMap<>();
			for (String line : lines) {
				String[] columns = line.split(",");
				if (aggregationMap.containsKey(columns[1])) {
					String[] strings = aggregationMap.get(columns[1]);
					strings = new String[] { columns[0], columns[1], String.valueOf(Integer.parseInt(strings[2]) + 1) };
					aggregationMap.put(columns[1], strings);
				} else {
					aggregationMap.put(columns[1], new String[] { columns[0], columns[1], String.valueOf(1) });
				}
			}

			mav.addObject("history", aggregationMap.values());
			lock.unlock();

		} catch (Exception e) {
			log.error("Exception occurs: ", e);
			mav.addObject("errmsg", msg.getMessage("msg.unknown.exception.occur", null, null, locale));

		}

		return mav;
	}
}
