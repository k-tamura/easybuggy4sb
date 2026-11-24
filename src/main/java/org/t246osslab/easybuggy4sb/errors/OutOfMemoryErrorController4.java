package org.t246osslab.easybuggy4sb.errors;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class OutOfMemoryErrorController4 {

	/*
	 * Keeps adding small objects so that the heap fills up gradually and the JVM spends almost all of its time
	 * in GC, which results in "GC overhead limit exceeded". Notes:
	 * - The limit is implemented only by the Parallel GC (see -XX:+UseParallelGC, -XX:GCTimeLimit and
	 *   -XX:GCHeapFreeLimit in pom.xml); other collectors throw "Java heap space" instead.
	 * - A linked list is used because a collection backed by an array (ArrayList, HashMap, ...) allocates a
	 *   large array when it grows, which tends to fail with "Java heap space" first.
	 * - The list is a local variable, so the memory is released after the request and the application keeps
	 *   working (filling a JVM-wide object such as the system properties would never release it).
	 */
	@RequestMapping(value = "/oome4")
	public void process() {
		List<Integer> list = new LinkedList<>();
		Random r = new Random();
		while (true) {
			list.add(r.nextInt());
		}
	}
}
