package org.t246osslab.easybuggy4sb.core.filters;

import org.springframework.stereotype.Component;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter for authentication
 */
@Component
public class AuthenticationFilter implements Filter {

    /**
     * Intercept unauthenticated requests for specific URLs and redirect to login page.
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String target = request.getRequestURI();
        
        if (target.startsWith("/admins") || "/serverinfo".equals(target)) {
            /* Login (authentication) is needed to access admin pages (under /admins). */
            
            String loginType = request.getParameter("logintype");
            String queryString = request.getQueryString();

            /* Remove "logintype" parameter from query string. */
            StringBuilder cleanQuery = new StringBuilder();
            if (queryString != null && !queryString.isEmpty()) {
                for (String param : queryString.split("&")) {
                    String paramName = param.indexOf('=') >= 0 ? param.substring(0, param.indexOf('=')) : param;
                    if (!"logintype".equals(paramName)) {
                        if (cleanQuery.length() > 0) cleanQuery.append('&');
                        cleanQuery.append(param);
                    }
                }
            }
            queryString = cleanQuery.length() > 0 ? "?" + cleanQuery.toString() : "";

            HttpSession session = request.getSession(false);
            String authNMsg = (session == null) ? null : (String) session.getAttribute("authNMsg");
			if (!"authenticated".equals(authNMsg)) {
                /* Not authenticated yet */
                session = request.getSession(true);
                session.setAttribute("target", target + queryString);
                if (loginType == null) {
                    response.sendRedirect("/login" + queryString);
                } else if ("sessionfixation".equals(loginType)) {
                    response.sendRedirect(response.encodeRedirectURL("/" + loginType + "/login" + queryString));
                } else {
                    response.sendRedirect("/" + loginType + "/login" + queryString);
                }
                return;
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void init(FilterConfig arg0) {
        // Do nothing
    }
}
