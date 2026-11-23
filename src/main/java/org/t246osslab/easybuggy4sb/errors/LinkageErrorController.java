package org.t246osslab.easybuggy4sb.errors;

import java.util.Arrays;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LinkageErrorController {

	/*
	 * Defines the same class twice in one class loader, as a hand-made class loader may do when it does not check
	 * findLoadedClass() first. The JVM refuses the second definition and throws LinkageError
	 * ("attempted duplicate class definition").
	 */
	@RequestMapping(value = "/le")
	public void process() {
		byte[] bytecode = GeneratedClasses.libraryWithMethod(GeneratedClasses.ACC_PUBLIC, "hello", "()V");
		GeneratedClasses.Loader loader = new GeneratedClasses.Loader();
		loader.define(GeneratedClasses.LIBRARY, bytecode);
		loader.define(GeneratedClasses.LIBRARY, Arrays.copyOf(bytecode, bytecode.length));
	}
}
