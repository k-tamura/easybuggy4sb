package org.t246osslab.easybuggy4sb.errors;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

@Controller
public class OutOfMemoryErrorController5 extends AbstractController {

	/*
	 * Keeps defining new classes until the Metaspace (or PermGen space on Java 7) is exhausted. Notes:
	 * - CtClass objects are detached from the ClassPool after use; otherwise the pool keeps them all on the
	 *   heap and "Java heap space" occurs before the Metaspace is exhausted.
	 * - The classes are defined by a class loader created per request, so they can be unloaded after the
	 *   request. This lets the error occur again on every access (a fixed loader would reject the duplicate
	 *   class names) and keeps the rest of the application able to load classes.
	 */
	@RequestMapping(value = "/oome5")
	public void process() throws IOException, CannotCompileException {
		ClassPool pool = new ClassPool(true);
		GeneratedClassLoader loader = new GeneratedClassLoader(getClass().getClassLoader());
		for (int i = 0; ; i++) {
			CtClass ctClass = pool.makeClass("org.t246osslab.easybuggy4sb.errors.Generated" + i);
			byte[] bytecode = ctClass.toBytecode();
			ctClass.detach();
			loader.define(ctClass.getName(), bytecode);
		}
	}

	private static class GeneratedClassLoader extends ClassLoader {

		GeneratedClassLoader(ClassLoader parent) {
			super(parent);
		}

		Class<?> define(String name, byte[] bytecode) {
			return defineClass(name, bytecode, 0, bytecode.length);
		}
	}
}
