package com.residuosolido.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthNavigationInterceptor authNavigationInterceptor;

    @Autowired
    public WebMvcConfig(AuthNavigationInterceptor authNavigationInterceptor) {
        this.authNavigationInterceptor = authNavigationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authNavigationInterceptor)
                .addPathPatterns("/", "/index", "/auth/login", "/auth/register");
    }
}
