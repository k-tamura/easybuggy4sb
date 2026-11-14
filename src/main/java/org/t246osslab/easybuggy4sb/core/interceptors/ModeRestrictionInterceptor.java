package org.t246osslab.easybuggy4sb.core.interceptors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * Blocks access (returns 404) to non-vulnerability endpoints
 * (controllers in the troubles / performance / errors / exceptions packages)
 * when easybuggy.mode=only-vulnerabilities.
 *
 * <p>The view-layer flag {@code isOnlyVulnerabilities} (which hides links in
 * index.html) alone leaves the controllers mapped, so the endpoints still run
 * when their URLs are requested directly. This interceptor decides by the
 * handler's package, so it disables only the non-vulnerability endpoints
 * (including the crash / leak / DoS ones) without enumerating any paths.
 *
 * <p>Because it rejects in {@code preHandle} (before the handler runs), even
 * heavy or crash-inducing endpoints such as {@code /jvmcrasheav} or
 * {@code /memoryleak} are stopped before execution.
 *
 * <p>Location: {@code src/main/java/org/t246osslab/easybuggy4sb/core/interceptors/}
 *
 * <p>Registration: add the following to {@code WebMvcConfig}.
 * <pre>{@code
 * @Autowired
 * private ModeRestrictionInterceptor modeRestrictionInterceptor;
 *
 * @Override
 * public void addInterceptors(InterceptorRegistry registry) {
 *     registry.addInterceptor(modeRestrictionInterceptor);
 * }
 * }</pre>
 */
@Component
public class ModeRestrictionInterceptor extends HandlerInterceptorAdapter {

    /** Packages of the non-vulnerability controllers to disable. */
    private static final Set<String> BLOCKED_PACKAGES = new HashSet<String>(Arrays.asList(
            "org.t246osslab.easybuggy4sb.troubles",
            "org.t246osslab.easybuggy4sb.performance",
            "org.t246osslab.easybuggy4sb.errors",
            "org.t246osslab.easybuggy4sb.exceptions"));

    private static boolean isOnlyVulnerabilitiesMode() {
        return StringUtils.equalsIgnoreCase(
                System.getProperty("easybuggy.mode"), "only-vulnerabilities");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (isOnlyVulnerabilitiesMode() && handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Package beanPackage = handlerMethod.getBeanType().getPackage();
            if (beanPackage != null && BLOCKED_PACKAGES.contains(beanPackage.getName())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
        }
        return true;
    }
}
