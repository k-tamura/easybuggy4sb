package org.t246osslab.easybuggy4sb.exceptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BufferOverflowExceptionController {

	private static final Logger log = LoggerFactory.getLogger(BufferOverflowExceptionController.class);

	@RequestMapping(value = "/boe")
	public void process() {
		File f = new File("test.txt");
		try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
			FileChannel ch = raf.getChannel();
			MappedByteBuffer buf = ch.map(MapMode.READ_WRITE, 0, f.length());
			final byte[] src = new byte[10];
			buf.put(src);
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException occurs: ", e);
		} catch (IOException e) {
			log.error("IOException occurs: ", e);
		}
	}
}