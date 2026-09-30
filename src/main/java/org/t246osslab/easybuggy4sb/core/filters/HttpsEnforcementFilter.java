package org.t246osslab.easybuggy4sb.core.filters;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * Filter to enforce HTTPS communication
 */
@Component
public class HttpsEnforcementFilter implements Filter {

    @Value("${metrics.allowed.hosts}")
    protected String METRICS_ALLOWED_HOSTS;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // Allow HTTP access for /stat
        if (uri.startsWith("/stat")) {
            chain.doFilter(request, response);
            return;
        }
        // Check allowed hosts for /metrics and /health
        if (uri.startsWith("/metrics") || uri.startsWith("/health")) {
            if (!isAllowedHost(req.getRemoteHost())) {
                res.sendRedirect("https://" + req.getServerName());
                return;
            }
            chain.doFilter(request, response);
            return;
        }
        // Check if accessing via HTTPS
        boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getScheme())
                || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));

        if (!secure) {
            // Redirect to HTTPS URL
            String redirectUrl = "https://" + req.getServerName() + req.getRequestURI();
            if (req.getQueryString() != null) {
                redirectUrl += "?" + req.getQueryString();
            }
            res.sendRedirect(redirectUrl);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Do nothing
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    private boolean isAllowedHost(String remoteHost) {
        if (!StringUtils.hasText(METRICS_ALLOWED_HOSTS) || !StringUtils.hasText(remoteHost)) {
            return false;
        }
        return Arrays.stream(METRICS_ALLOWED_HOSTS.split(","))
                .map(String::trim)
                .anyMatch(remoteHost::equalsIgnoreCase);
    }
}
