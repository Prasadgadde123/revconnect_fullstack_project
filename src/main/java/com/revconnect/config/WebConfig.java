// src/main/java/com/revconnect/config/WebConfig.java
package com.revconnect.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * ✅ Serves files from the local uploads/ folder as static resources.
     * So /uploads/profile-pictures/abc.jpg works in the browser.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}