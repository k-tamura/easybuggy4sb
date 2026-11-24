package org.t246osslab.easybuggy4sb.errors;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ExceptionInInitializerErrorController {

	private static final String TARGET = "org.t246osslab.easybuggy4sb.errors.InitializerErrorThrower";

	/*
	 * A class is initialized at most once per class loader: the first initialization throws
	 * ExceptionInInitializerError and leaves the class in an erroneous state, so a later attempt to use the same
	 * Class object throws NoClassDefFoundError instead. To throw ExceptionInInitializerError on every access, a
	 * fresh (not yet initialized) copy of InitializerErrorThrower is loaded with a new class loader per request
	 * and then initialized by Class.forName with initialize=true.
	 */
	@RequestMapping(value = "/eie")
	public void process() throws ClassNotFoundException {
		ClassLoader loader = new FreshClassLoader(getClass().getClassLoader());
		Class.forName(TARGET, true, loader);
	}

	/*
	 * Loads InitializerErrorThrower itself (so each instance owns a fresh, uninitialized copy) and delegates
	 * every other class to the parent loader.
	 */
	private static class FreshClassLoader extends ClassLoader {

		FreshClassLoader(ClassLoader parent) {
			super(parent);
		}

		@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			if (!TARGET.equals(name)) {
				return super.loadClass(name, resolve);
			}
			synchronized (getClassLoadingLock(name)) {
				Class<?> c = findLoadedClass(name);
				if (c == null) {
					String path = name.replace('.', '/') + ".class";
					try (InputStream in = getParent().getResourceAsStream(path)) {
						if (in == null) {
							throw new ClassNotFoundException(name);
						}
						byte[] bytecode = FileCopyUtils.copyToByteArray(in);
						c = defineClass(name, bytecode, 0, bytecode.length);
					} catch (IOException e) {
						throw new ClassNotFoundException(name, e);
					}
				}
				if (resolve) {
					resolveClass(c);
				}
				return c;
			}
		}
	}
}

class InitializerErrorThrower {
	static {
		LoggerFactory.getLogger(InitializerErrorThrower.class).debug("clinit" + 1 / 0);
	}

	private InitializerErrorThrower(){
		// this constructor is added to suppress sonar advice
	}
}
