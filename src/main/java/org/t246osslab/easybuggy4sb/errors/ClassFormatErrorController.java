package org.t246osslab.easybuggy4sb.errors;

import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ClassFormatErrorController {

	/*
	 * Loads a class file that was cut off in the middle, as happens when a download or a copy is interrupted.
	 * The JVM cannot parse it and throws ClassFormatError ("Truncated class file").
	 */
	@RequestMapping(value = "/cfe")
	public void process() throws ReflectiveOperationException {
		byte[] bytecode = GeneratedClasses.libraryWithMethod(GeneratedClasses.ACC_PUBLIC, "hello", "()V");
		new GeneratedClasses.Loader()
				.put(GeneratedClasses.LIBRARY, Arrays.copyOf(bytecode, bytecode.length / 2))
				.loadClass(GeneratedClasses.LIBRARY.replace('/', '.'));
	}
}
