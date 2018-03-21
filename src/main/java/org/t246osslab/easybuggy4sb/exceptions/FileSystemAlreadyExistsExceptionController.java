package org.t246osslab.easybuggy4sb.exceptions;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FileSystemAlreadyExistsExceptionController {

	@RequestMapping(value = "/fsaee")
	public void process(HttpServletRequest req) {
		
        String tmpDir = req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString();
		URI zipfile = URI.create("jar:file:" + tmpDir + File.separator + "fsaee.zip");
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF-8");
        try (FileSystem zipfs = FileSystems.newFileSystem(zipfile, env)) {
            Files.write(zipfs.getPath("fsaee.txt"), "test".getBytes("UTF-8"), StandardOpenOption.CREATE);
            FileSystems.newFileSystem(zipfile, env);
            FileSystems.newFileSystem(zipfile, env);
        } catch (IOException e1) {
		}	
	}
}