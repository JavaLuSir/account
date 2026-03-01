package com.luxinx.config;

import org.apache.tomcat.util.security.MD5Encoder;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@Component  // 在 WebConfig 中手动注册
public class LoginFilter implements Filter {

    // 正确的 token (admin123456 的 MD5)
    private static final String VALID_TOKEN = MD5Encoder.encode(("admin" + "123456").getBytes());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // 只拦截 /rest/* 接口
        if (requestURI.contains("/rest/")) {
            // 检查是否是登录接口，登录接口放行
            if (requestURI.endsWith("/login")) {
                chain.doFilter(request, response);
                return;
            }
            
            // 检查 token
            Cookie[] cookies = httpRequest.getCookies();
            String token = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            
            // token 不匹配，返回 401
            if (token == null || !token.equals(VALID_TOKEN)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write("{\"code\":401,\"msg\":\"未登录，请先登录\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
