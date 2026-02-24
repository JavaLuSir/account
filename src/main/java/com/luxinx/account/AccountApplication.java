package com.luxinx.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * AccountApplication 类是 Spring Boot 应用程序的入口点。
 * 它位于 com.luxinx.account 包中。
 * 该类使用了多个 Spring Boot 注解来配置应用程序。
 */
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan(basePackages = {"com.luxinx.**.service","com.luxinx.**.controller","com.luxinx.config","com.luxinx.cron"})
public class AccountApplication {

    /**
     * main 方法是应用程序的入口点。
     * 它使用 SpringApplication.run 启动应用程序。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AccountApplication.class, args);
    }

}