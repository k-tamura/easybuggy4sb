package org.t246osslab.easybuggy4sb.errors;

import java.util.HashMap;
import java.util.Map;

import org.springframework.asm.ClassWriter;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;

/**
 * Generates small classes at runtime to reproduce class loading and linkage errors.
 * <p>
 * Most of these errors occur when a class was compiled against one version of a library and runs with another
 * (binary incompatibility). The source code of both versions cannot coexist in this project, so the classes are
 * generated as bytecode with the ASM library bundled in spring-core. They use class file version 49 (Java 5), which
 * does not need stack map frames.
 */
final class GeneratedClasses implements Opcodes {

	static final String PACKAGE = "org/t246osslab/easybuggy4sb/errors/generated/";
	static final String LIBRARY = PACKAGE + "Library";
	static final String GREETER = PACKAGE + "Greeter";
	static final String GREETER_IMPL = PACKAGE + "GreeterImpl";
	static final String CLIENT = PACKAGE + "Client";

	private static final String OBJECT = "java/lang/Object";

	interface Code {
		void write(MethodVisitor mv);
	}

	private GeneratedClasses() {
	}

	/** Version 2 of Library: a class with one method whose body does nothing. */
	static byte[] libraryWithMethod(int access, String name, String desc) {
		ClassWriter cw = begin(ACC_PUBLIC | ACC_SUPER, LIBRARY, OBJECT);
		constructor(cw, OBJECT);
		MethodVisitor mv = cw.visitMethod(access, name, desc, null, null);
		mv.visitCode();
		mv.visitInsn(RETURN);
		end(mv);
		return finish(cw);
	}

	/** Version 2 of Library: a class with one public static int field. */
	static byte[] libraryWithField(String name) {
		ClassWriter cw = begin(ACC_PUBLIC | ACC_SUPER, LIBRARY, OBJECT);
		constructor(cw, OBJECT);
		cw.visitField(ACC_PUBLIC | ACC_STATIC, name, "I", null, null).visitEnd();
		return finish(cw);
	}

	/** An interface with one abstract method {@code String greet()}. */
	static byte[] greeterWithGreet() {
		ClassWriter cw = begin(ACC_PUBLIC | ACC_INTERFACE | ACC_ABSTRACT, GREETER, OBJECT);
		cw.visitMethod(ACC_PUBLIC | ACC_ABSTRACT, "greet", "()Ljava/lang/String;", null, null).visitEnd();
		return finish(cw);
	}

	/** An implementation of Greeter compiled when Greeter did not have greet() yet. */
	static byte[] greeterImplWithoutGreet() {
		ClassWriter cw = begin(ACC_PUBLIC | ACC_SUPER, GREETER_IMPL, OBJECT, GREETER);
		constructor(cw, OBJECT);
		return finish(cw);
	}

	/** A class implementing Runnable whose run() executes the given code. */
	static byte[] client(Code code) {
		ClassWriter cw = begin(ACC_PUBLIC | ACC_SUPER, CLIENT, OBJECT, "java/lang/Runnable");
		constructor(cw, OBJECT);
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		code.write(mv);
		mv.visitInsn(RETURN);
		end(mv);
		return finish(cw);
	}

	/** Loads Client from the loader and calls its run() method. */
	static void runClient(Loader loader) throws ReflectiveOperationException {
		Class<?> clazz = Class.forName(CLIENT.replace('/', '.'), true, loader);
		((Runnable) clazz.getDeclaredConstructor().newInstance()).run();
	}

	private static ClassWriter begin(int access, String name, String superName, String... interfaces) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		cw.visit(V1_5, access, name, null, superName, interfaces);
		return cw;
	}

	private static void constructor(ClassWriter cw, String superName) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, superName, "<init>", "()V", false);
		mv.visitInsn(RETURN);
		end(mv);
	}

	private static void end(MethodVisitor mv) {
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	private static byte[] finish(ClassWriter cw) {
		cw.visitEnd();
		return cw.toByteArray();
	}

	/** A class loader that defines classes from the given bytecode. A new instance is used for each request. */
	static final class Loader extends ClassLoader {

		private final Map<String, byte[]> classes = new HashMap<>();

		Loader() {
			super(GeneratedClasses.class.getClassLoader());
		}

		Loader put(String internalName, byte[] bytecode) {
			classes.put(internalName.replace('/', '.'), bytecode);
			return this;
		}

		/** Defines a class directly, bypassing the check whether it has already been loaded. */
		Class<?> define(String internalName, byte[] bytecode) {
			return defineClass(internalName.replace('/', '.'), bytecode, 0, bytecode.length);
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			byte[] bytecode = classes.get(name);
			if (bytecode == null) {
				throw new ClassNotFoundException(name);
			}
			return defineClass(name, bytecode, 0, bytecode.length);
		}
	}
}
