package org.t246osslab.easybuggy4sb.troubles;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class InfiniteLoopController {

	private static final Logger log = LoggerFactory.getLogger(InfiniteLoopController.class);

	@Autowired
	MessageSource msg;

	@RequestMapping(value = "/infiniteloop")
	public void process(HttpServletRequest req) throws IOException {
		while (true) {
			String contextPath = req.getContextPath();
			int contentLength = req.getContentLength();
			log.debug("contextPath: {}, contentLength: {}", contextPath, contentLength);
		}
	}
}