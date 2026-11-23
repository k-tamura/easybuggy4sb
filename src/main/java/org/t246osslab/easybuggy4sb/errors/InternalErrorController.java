package org.t246osslab.easybuggy4sb.errors;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

@Controller
public class InternalErrorController extends AbstractController {

	private static final int FILE_SIZE = 64 * 1024;

	/*
	 * Maps a file into memory, truncates the file, and then reads the mapped memory. The pages no longer have
	 * a backing file, so the OS sends SIGBUS, and the JVM converts it into InternalError ("a fault occurred in
	 * a recent unsafe memory access operation"). This happens on Linux (including Docker). On Windows, the
	 * file cannot be truncated while it is mapped, so IOException is thrown instead.
	 */
	@RequestMapping(value = "/interr")
	public ModelAndView process(ModelAndView mav, HttpServletRequest req, Locale locale) {
		File file = new File(req.getServletContext().getAttribute("javax.servlet.context.tempdir").toString(),
				"mapped.dat");
		try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel channel = raf.getChannel()) {
			raf.setLength(FILE_SIZE);
			MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, FILE_SIZE);
			raf.setLength(0);
			long sum = 0;
			for (int i = 0; i < FILE_SIZE; i++) {
				sum += buffer.get(i);
			}
			log.debug("Unexpectedly read the truncated file: {}", sum);
		} catch (IOException e) {
			log.info("InternalError is not thrown on this OS: {}", e.getMessage());
			setViewAndCommonObjects(mav, locale, "errornotoccurred");
			mav.addObject("msg", msg.getMessage("msg.internalerror.not.thrown", null, locale));
			mav.addObject("errmsg", e.toString());
			return mav;
		} finally {
			if (!file.delete()) {
				log.debug("Could not delete {}", file);
			}
		}
		setViewAndCommonObjects(mav, locale, "errornotoccurred");
		return mav;
	}
}
