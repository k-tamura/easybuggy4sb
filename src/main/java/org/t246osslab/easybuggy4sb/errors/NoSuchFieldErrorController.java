package org.t246osslab.easybuggy4sb.errors;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class NoSuchFieldErrorController {

	/*
	 * Client was compiled against version 1 of Library, which had "public static int VERSION".
	 * Version 2 renamed it to VERSION_CODE, so reading VERSION from Client throws NoSuchFieldError.
	 */
	@RequestMapping(value = "/nsfe")
	public void process() throws ReflectiveOperationException {
		GeneratedClasses.runClient(new GeneratedClasses.Loader()
				.put(GeneratedClasses.LIBRARY, GeneratedClasses.libraryWithField("VERSION_CODE"))
				.put(GeneratedClasses.CLIENT, GeneratedClasses.client(new GeneratedClasses.Code() {
					@Override
					public void write(MethodVisitor mv) {
						// int version = Library.VERSION;
						mv.visitFieldInsn(Opcodes.GETSTATIC, GeneratedClasses.LIBRARY, "VERSION", "I");
						mv.visitInsn(Opcodes.POP);
					}
				})));
	}
}
