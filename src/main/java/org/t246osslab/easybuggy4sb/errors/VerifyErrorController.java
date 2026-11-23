package org.t246osslab.easybuggy4sb.errors;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VerifyErrorController {

	/*
	 * Client is a class broken by, for example, a buggy bytecode manipulation tool: its run() method pushes
	 * an int and passes it to a method that takes a String. The bytecode verifier rejects the class when it is
	 * linked, so VerifyError is thrown.
	 */
	@RequestMapping(value = "/ve")
	public void process() throws ReflectiveOperationException {
		GeneratedClasses.runClient(new GeneratedClasses.Loader()
				.put(GeneratedClasses.CLIENT, GeneratedClasses.client(new GeneratedClasses.Code() {
					@Override
					public void write(MethodVisitor mv) {
						// String.valueOf(1).concat(1); -- impossible in Java source code
						mv.visitInsn(Opcodes.ICONST_1);
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;",
								false);
						mv.visitInsn(Opcodes.ICONST_1);
						mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "concat",
								"(Ljava/lang/String;)Ljava/lang/String;", false);
						mv.visitInsn(Opcodes.POP);
					}
				})));
	}
}
