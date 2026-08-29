package com.psp.cbt.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// Runs before every /admin/** request (except /admin/login).
// If there is no "adminId" in the session, the visitor is not
// logged in as an admin, so we send them to the login page instead.
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("/admin/login");
            return false; // stop here, do not run the controller method
        }
        return true; // logged in, continue as normal
    }
}
