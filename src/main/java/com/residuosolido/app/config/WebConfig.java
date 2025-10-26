package com.residuosolido.app.config;

import com.residuosolido.app.interceptor.BreadcrumbInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Configuración web para registrar interceptors y otros componentes MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private BreadcrumbInterceptor breadcrumbInterceptor;

    @Autowired
    private LocaleChangeInterceptor localeChangeInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
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
