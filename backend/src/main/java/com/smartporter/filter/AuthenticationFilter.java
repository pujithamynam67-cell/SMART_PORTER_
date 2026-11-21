package com.smartporter.filter;

import com.smartporter.util.ResponseUtil;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter("/api/secure/*")
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("USER_TYPE") == null) {
            ResponseUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access. Please login first.");
            return;
        }

        chain.doFilter(req, res);
    }
}
