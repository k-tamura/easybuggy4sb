package org.t246osslab.easybuggy4sb.errors;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewMethod;

@Controller
public class OutOfMemoryErrorController5 extends AbstractController {

	/*
	 * Number of methods added to each generated class. Method metadata is stored in the Metaspace but not in
	 * the Compressed class space, so "fat" classes make the Metaspace fill up first. This keeps this feature
	 * reporting "Metaspace" even though -XX:CompressedClassSpaceSize is set small (see pom.xml) for the
	 * Compressed class space feature (oome7), which generates empty classes instead.
	 */
	private static final int METHODS_PER_CLASS = 40;

	/*
	 * Keeps defining new classes until the Metaspace (or PermGen space on Java 7) is exhausted. Notes:
	 * - CtClass objects are detached from the ClassPool after use; otherwise the pool keeps them all on the
	 *   heap and "Java heap space" occurs before the Metaspace is exhausted.
	 * - The classes are defined by a class loader created per request, so they can be unloaded after the
	 *   request. This lets the error occur again on every access (a fixed loader would reject the duplicate
	 *   class names) and keeps the rest of the application able to load classes.
	 * - Each class is given many methods so that the Metaspace is exhausted before the (small) Compressed
	 *   class space; the Compressed class space counterpart is oome7.
	 */
	@RequestMapping(value = "/oome5")
	public void process() throws IOException, CannotCompileException {
		ClassPool pool = new ClassPool(true);
		GeneratedClassLoader loader = new GeneratedClassLoader(getClass().getClassLoader());
		for (int i = 0; ; i++) {
			CtClass ctClass = pool.makeClass("org.t246osslab.easybuggy4sb.errors.Generated" + i);
			for (int m = 0; m < METHODS_PER_CLASS; m++) {
				ctClass.addMethod(CtNewMethod.make("public long m" + m + "(long x){return x * " + m + " + " + i + ";}", ctClass));
			}
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
