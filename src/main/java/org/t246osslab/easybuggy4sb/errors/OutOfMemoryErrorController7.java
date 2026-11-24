package org.t246osslab.easybuggy4sb.errors;

import java.io.IOException;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.t246osslab.easybuggy4sb.controller.AbstractController;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

@Controller
public class OutOfMemoryErrorController7 extends AbstractController {

	/*
	 * Keeps defining new classes until the Compressed class space is exhausted. On a 64-bit JVM with compressed
	 * class pointers (the default when the heap is below 32 GB), the class pointer part of each class's metadata
	 * (the Klass structure) is stored in a dedicated region called the Compressed class space, separate from the
	 * rest of the Metaspace. Notes:
	 * - This requires -XX:CompressedClassSpaceSize to be set small relative to -XX:MaxMetaspaceSize (see
	 *   pom.xml); otherwise the Metaspace is exhausted first and "Metaspace" is reported instead (that case is
	 *   demonstrated by oome5).
	 * - Empty classes are generated on purpose: they consume almost no Metaspace other than the Klass, so the
	 *   Compressed class space fills up first. oome5 generates classes with many methods to exhaust the
	 *   Metaspace instead.
	 * - As in oome5, CtClass objects are detached and a per-request class loader is used so the generated
	 *   classes can be unloaded after the request and the error can be reproduced on every access.
	 */
	@RequestMapping(value = "/oome7")
	public void process() throws IOException, CannotCompileException {
		ClassPool pool = new ClassPool(true);
		GeneratedClassLoader loader = new GeneratedClassLoader(getClass().getClassLoader());
		for (int i = 0; ; i++) {
			CtClass ctClass = pool.makeClass("org.t246osslab.easybuggy4sb.errors.CompressedGenerated" + i);
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
