package com.example.examenes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Solo añade manejadores adicionales si es necesario
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Este manejador permite servir recursos desde /images/**
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}