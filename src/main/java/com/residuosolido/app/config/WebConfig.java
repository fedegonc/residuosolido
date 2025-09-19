package com.residuosolido.app.config;

import com.residuosolido.app.interceptor.BreadcrumbInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración web para registrar interceptors y otros componentes MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BreadcrumbInterceptor breadcrumbInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registrar el interceptor de breadcrumbs para todas las rutas
        registry.addInterceptor(breadcrumbInterceptor)
                .addPathPatterns("/**")  // Aplicar a todas las rutas
                .excludePathPatterns(
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/webjars/**",
                    "/api/**",
                    "/error"
                ); // Excluir recursos estáticos y APIs
    }
}
