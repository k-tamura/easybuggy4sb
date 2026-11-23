package org.t246osslab.easybuggy4sb.errors;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NoSuchMethodErrorController {

	/*
	 * Client was compiled against version 1 of Library, which had "public static void hello()".
	 * Version 2 changed it to "hello(String)", so calling hello() from Client throws NoSuchMethodError.
	 */
	@RequestMapping(value = "/nsme")
	public void process() throws ReflectiveOperationException {
		GeneratedClasses.runClient(new GeneratedClasses.Loader()
				.put(GeneratedClasses.LIBRARY, GeneratedClasses.libraryWithMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "hello",
						"(Ljava/lang/String;)V"))
				.put(GeneratedClasses.CLIENT, GeneratedClasses.client(new GeneratedClasses.Code() {
					@Override
					public void write(MethodVisitor mv) {
						// Library.hello();
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, GeneratedClasses.LIBRARY, "hello", "()V", false);
					}
				})));
	}
}
