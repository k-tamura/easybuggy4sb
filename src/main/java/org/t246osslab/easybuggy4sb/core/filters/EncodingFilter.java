package org.t246osslab.easybuggy4sb.core.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.filter.OrderedCharacterEncodingFilter;
import org.springframework.stereotype.Component;

/**
 * Servlet Filter for encoding
 */
@Component
public class EncodingFilter extends OrderedCharacterEncodingFilter {

    /**
     * Set the encoding to use for requests.
     * "Shift_JIS" is intentionally set to the request to /mojibake.
     *
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        super.setOrder(HIGHEST_PRECEDENCE);
        if ("/mojibake".equals(request.getRequestURI())) {
            super.setEncoding("Shift_JIS");
        } else {
            super.setEncoding("UTF-8");
        }
        super.setForceEncoding(true);
        super.doFilterInternal(request, response, filterChain);
    }
}
