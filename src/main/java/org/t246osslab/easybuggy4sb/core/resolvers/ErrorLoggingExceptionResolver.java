package org.t246osslab.easybuggy4sb.core.resolvers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.NestedServletException;

/**
 * Logs the errors thrown by the controllers in the "errors" package with their whole stack trace.
 * <p>
 * Without this resolver, Tomcat logs only the root cause of an uncaught error. For an error that wraps another
 * exception (e.g. ExceptionInInitializerError caused by ArithmeticException, FactoryConfigurationError caused by
 * ClassNotFoundException), the log shows the cause instead of the error itself, which makes it hard to see which
 * error occurred. This resolver logs the error first and its cause as "Caused by", and then sends status 500, so the
 * standard error page is displayed as before.
 */
@Component
public class ErrorLoggingExceptionResolver implements HandlerExceptionResolver, Ordered {

	private static final Logger log = LoggerFactory.getLogger(ErrorLoggingExceptionResolver.class);

	private static final String ERRORS_PACKAGE = "org.t246osslab.easybuggy4sb.errors";

	@Override
	public ModelAndView resolveException(HttpServletRequest req, HttpServletResponse res, Object handler,
			Exception ex) {
		if (!(handler instanceof HandlerMethod)) {
			return null;
		}
		Package pkg = ((HandlerMethod) handler).getBeanType().getPackage();
		if (pkg == null || !ERRORS_PACKAGE.equals(pkg.getName())) {
			return null;
		}
		// Spring MVC wraps an Error thrown by a handler in NestedServletException
		Throwable thrown = ex instanceof NestedServletException && ex.getCause() != null ? ex.getCause() : ex;
		log.error("{} is thrown: ", thrown.getClass().getName(), thrown);
		try {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			log.debug("Could not send the error response: ", e);
		}
		return new ModelAndView();
	}

	@Override
	public int getOrder() {
		// Run after DefaultErrorAttributes (HIGHEST_PRECEDENCE), which records the exception for the error page
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}
}
