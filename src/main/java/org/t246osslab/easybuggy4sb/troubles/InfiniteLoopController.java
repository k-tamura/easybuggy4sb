package org.t246osslab.easybuggy4sb.troubles;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class InfiniteLoopController {

    private static final Logger log = LoggerFactory.getLogger(InfiniteLoopController.class);

    @RequestMapping(value = "/infiniteloop")
    public void process(HttpServletRequest req) {
        while (true) {
			log.debug("contextPath: {}, contentLength: {}", req.getContextPath(), req.getContentLength());
        }
        // Note: This is more dangerous code
		// IntStream.generate(() -> 0).peek(i -> {
		// 	log.debug("contextPath: {}, contentLength: {}", req.getContextPath(), req.getContentLength());
		// }).parallel().noneMatch(i -> false);
    }
}