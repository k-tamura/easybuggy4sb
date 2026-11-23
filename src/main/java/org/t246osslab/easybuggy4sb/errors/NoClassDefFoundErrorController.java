package org.t246osslab.easybuggy4sb.errors;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NoClassDefFoundErrorController {

	/*
	 * Client was compiled with Library, but Library is missing at runtime (e.g. a JAR file was not deployed).
	 * The class loader cannot find Library when Client calls Library.hello(), so NoClassDefFoundError is thrown.
	 */
	@RequestMapping(value = "/ncdfe")
	public void process() throws ReflectiveOperationException {
		GeneratedClasses.runClient(new GeneratedClasses.Loader()
				.put(GeneratedClasses.CLIENT, GeneratedClasses.client(new GeneratedClasses.Code() {
					@Override
					public void write(MethodVisitor mv) {
						// Library.hello();
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, GeneratedClasses.LIBRARY, "hello", "()V", false);
					}
				})));
	}
}
