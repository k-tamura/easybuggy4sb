package org.t246osslab.easybuggy4sb.errors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OutOfMemoryErrorController6 {

	/*
	 * Keeps allocating direct buffers until the limit (-XX:MaxDirectMemorySize, or by default the maximum heap
	 * size) is reached, so that the error occurs regardless of how the JVM was started. The buffers are only
	 * referenced from a local variable, so they are released after the request.
	 */
	@RequestMapping(value = "/oome6")
	public void process() {
		List<ByteBuffer> buffers = new ArrayList<>();
		while (true) {
			buffers.add(ByteBuffer.allocateDirect(10 * 1024 * 1024));
		}
	}
}
