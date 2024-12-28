package com.luxinx.config;

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
import java.util.Properties;

@Component
@Configuration
public class UserFilter implements Filter {

    private String username;
    private String password;

    @Override
    public void init(FilterConfig filterConfig){
        Properties configProperties = new Properties();
        InputStream ins = this.getClass().getClassLoader().getResourceAsStream("application.properties");
        try {
            configProperties.load(ins);
        } catch (IOException e) {
            e.printStackTrace();
        }
        username = configProperties.getProperty("account.username");
        password = configProperties.getProperty("account.password");

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpServletResponse response = (HttpServletResponse)servletResponse;
        String token = request.getHeader("token");
        String md5key = MD5Encoder.encode((username + password).getBytes());
        String uri = request.getRequestURI();
        if(uri.endsWith("login")||uri.endsWith("html")||uri.endsWith("js")||uri.endsWith("css")||uri.endsWith("ttf")){
            chain.doFilter(request,response);
        }else{
            Cookie[] cookies = ((HttpServletRequest) servletRequest).getCookies();
            Arrays.stream(cookies).filter(cookie -> cookie.getName().equals("token")).forEach(cookie -> {
                if(!token.equals(cookie.getValue())){
                    try {
                        response.getWriter().print("invilid request");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            chain.doFilter(request,response);
        }

    }
}
