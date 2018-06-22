package com.test.ruleeditor.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Created by Srilakshmi on 22/06/17.
 */
public class WebConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("*").
                allowedMethods("POST, GET, PUT, OPTIONS, DELETE").
                allowedHeaders("Origin","Accept","Content-Type").
                maxAge(3600).
                allowCredentials(false);
    }
}
