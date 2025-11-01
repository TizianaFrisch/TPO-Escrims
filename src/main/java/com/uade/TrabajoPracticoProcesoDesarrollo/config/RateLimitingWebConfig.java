package com.uade.TrabajoPracticoProcesoDesarrollo.config;

import com.uade.TrabajoPracticoProcesoDesarrollo.security.RateLimitingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitingWebConfig implements WebMvcConfigurer {

    private final RateLimitingInterceptor interceptor;

    @Autowired
    public RateLimitingWebConfig(RateLimitingInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Protect moderation endpoints
        registry.addInterceptor(interceptor).addPathPatterns("/api/mod/**");
        // Protect report moderation endpoints explicitly (safe to duplicate)
        registry.addInterceptor(interceptor).addPathPatterns("/api/mod/reportes/**");
        // Protect postular (apply) endpoint
        registry.addInterceptor(interceptor).addPathPatterns("/api/scrims/*/postulaciones");
    }
}
