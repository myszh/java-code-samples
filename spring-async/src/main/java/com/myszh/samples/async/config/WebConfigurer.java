/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.async.config;

import com.myszh.samples.async.core.User;
import com.myszh.samples.async.core.UserContext;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.Random;

/**
 * Web相关配置
 *
 * @author LuoQuan
 * @since 2022/6/12
 */
@Configuration
public class WebConfigurer implements WebMvcConfigurer {
    /**
     * 登录用户过滤器
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<Filter> loginFilter() {
        FilterRegistrationBean<Filter> filterRegistration = new FilterRegistrationBean<>();
        // 拦截所有请求
        filterRegistration.addUrlPatterns("/*");

        Random random = new Random();
        filterRegistration.setFilter(
            (request, response, chain) -> {
                int value = random.nextInt(100);
                User user = new User();
                user.setAge(value);
                user.setName("zhang " + value);
                UserContext.setUser(user);
                chain.doFilter(request, response);
            });
        return filterRegistration;
    }
}
