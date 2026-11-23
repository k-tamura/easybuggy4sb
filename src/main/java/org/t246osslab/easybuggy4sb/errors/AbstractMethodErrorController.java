package org.t246osslab.easybuggy4sb.errors;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AbstractMethodErrorController {

	/*
	 * GreeterImpl was compiled against version 1 of the Greeter interface, which had no methods.
	 * Version 2 added the abstract method greet(), but GreeterImpl was not recompiled and does not implement it.
	 * Calling greet() on a GreeterImpl instance throws AbstractMethodError.
	 */
	@RequestMapping(value = "/ame")
	public void process() throws ReflectiveOperationException {
		GeneratedClasses.runClient(new GeneratedClasses.Loader()
				.put(GeneratedClasses.GREETER, GeneratedClasses.greeterWithGreet())
				.put(GeneratedClasses.GREETER_IMPL, GeneratedClasses.greeterImplWithoutGreet())
				.put(GeneratedClasses.CLIENT, GeneratedClasses.client(new GeneratedClasses.Code() {
					@Override
					public void write(MethodVisitor mv) {
						// Greeter greeter = new GreeterImpl(); greeter.greet();
						mv.visitTypeInsn(Opcodes.NEW, GeneratedClasses.GREETER_IMPL);
						mv.visitInsn(Opcodes.DUP);
						mv.visitMethodInsn(Opcodes.INVOKESPECIAL, GeneratedClasses.GREETER_IMPL, "<init>", "()V", false);
						mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, GeneratedClasses.GREETER, "greet",
								"()Ljava/lang/String;", true);
						mv.visitInsn(Opcodes.POP);
					}
				})));
	}
}
