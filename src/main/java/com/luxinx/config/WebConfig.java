package com.luxinx.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /files/** 映射到文件系统路径
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:/static/")
                .setCachePeriod(3600); // 缓存时间（可选）
    }
}