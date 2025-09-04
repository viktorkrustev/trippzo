package com.trippzo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Когато браузърът поиска /uploads/avatars/...
        // Spring ще върне файла от физическата папка на диска
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:///D:/trippzo/uploads/avatars/");
    }
}
