package org.t246osslab.easybuggy4sb.core.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.stereotype.Component;

/**
 * Servlet Filter for security
 */
@Component
public class SecurityFilter implements Filter {

    /**
     * Default constructor.
     */
    public SecurityFilter() {
        // Do nothing
    }

    /**
     * Call {@link #doFilter(HttpServletRequest, HttpServletResponse, FilterChain)}.
     * 
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String target = request.getRequestURI();

        /* Prevent to upload large files if target start w/ /ureupload ... */
        if (target.startsWith("/ureupload") && request.getMethod().equalsIgnoreCase("POST")) {
            ServletFileUpload upload = new ServletFileUpload();
            upload.setFileItemFactory(new DiskFileItemFactory());
            upload.setFileSizeMax(1024 * 1024 * 10); // 10MB
            upload.setSizeMax(1024 * 1024 * 50); // 50MB
            try {
                upload.parseRequest(new ServletRequestContext(request));
            } catch (FileUploadException e) {
                throw new ServletException(e);
            }
        }

        /* Prevent clickjacking if target is not /admins/clickjacking ... */
        if (!target.startsWith("/admins/clickjacking")) {
            response.addHeader("X-FRAME-OPTIONS", "DENY");
        }
        /* Prevent Content-Type sniffing */
        response.addHeader("X-Content-Type-Options", "nosniff");

        /* Prevent XSS if target is not /xss ... */
        if (!target.startsWith("/xss")) {
            response.addHeader("X-XSS-Protection", "1; mode=block");
        }
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // Do nothing
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // Do nothing
    }
}
