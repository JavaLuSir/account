package com.luxinx.config;

import com.alibaba.fastjson.JSONObject;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

@Component
@Configuration
public class UserFilter implements Filter {

    private String username;
    private String password;
    private String MD5key;
    @Override
    public void init(FilterConfig filterConfig) {
        Properties configProperties = new Properties();
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            configProperties.load(ins);
        } catch (IOException e) {
            e.printStackTrace();
        }
        username = configProperties.getProperty("account.username");
        password = configProperties.getProperty("account.password");
        MD5key = configProperties.getProperty("account.md5");

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String token = request.getHeader("token");
        String uri = request.getRequestURI();

        if (uri.endsWith("login.html")) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("token") && MD5key.equals(cookie.getValue())) {
                        response.sendRedirect("/account/index.html");
                        return;
                    }
                }
            }
        }

        if (uri.endsWith("login") || uri.endsWith("html") || uri.endsWith("js") || uri.endsWith("css") || uri.endsWith("ttf")) {
            chain.doFilter(request, response);
        } else {

            Cookie[] cookies = ((HttpServletRequest) servletRequest).getCookies();
            if (cookies == null) {
                try {
                    response.getWriter().write(JSONObject.toJSONString(new HashMap<String, Object>() {{
                        put("code", 302);
                        put("url", "/account/login.html");
                    }}));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            for(Cookie cookie:cookies){
                if(cookie.getName().equals("token")){
                    if (!MD5key.equals(cookie.getValue())) {
                        try {
                            response.getWriter().write(JSONObject.toJSONString(new HashMap<String, Object>() {{
                                put("code", 302);
                                put("url", "/account/login.html");
                            }}));
                            return;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            chain.doFilter(request, response);
        }

    }

    public static void main(String[] args) {
        System.out.println(MD5Encoder.encode(("admin123456").getBytes()));
    }
}
