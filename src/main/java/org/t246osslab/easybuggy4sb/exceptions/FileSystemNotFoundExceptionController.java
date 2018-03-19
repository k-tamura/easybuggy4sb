package org.t246osslab.easybuggy4sb.exceptions;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.util.HashMap;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class FileSystemNotFoundExceptionController  extends AbstractController {

	@RequestMapping(value = "/fsnfe")
	public void process() {
		URI uri = URI.create("jar:file:/not/exist.zip");
		try {
			FileSystems.newFileSystem(uri, new HashMap<String, Object>());
		} catch (IOException e) {
			log.error("IOException occurs: ", e);
		}
	}
}
