/*
 * Copyright 2022 the original author or authors.
 */

package com.myszh.samples.tomcat.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.servlet.http.HttpSession;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.TomcatWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.server.WebServerFactoryCustomizerBeanPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * info
 *
 * @author LuoQuan
 * @see ServerProperties
 * @see EmbeddedWebServerFactoryCustomizerAutoConfiguration
 * @see TomcatWebServerFactoryCustomizer
 * @see ReactiveWebServerFactoryAutoConfiguration
 * @see WebServerFactoryCustomizerBeanPostProcessor
 * @see org.springframework.boot.autoconfigure.web.reactive .ReactiveWebServerFactoryConfiguration.EmbeddedTomcat
 * @since 2022/6/17
 */
@RestController
@RequestMapping("/info")
public class InfoApplication {

    @GetMapping
    public Object info(HttpSession session) {
        // info
        return LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
