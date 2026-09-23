package com.residuosolido.app.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.controller.CurrentUserArgumentResolver;
import com.residuosolido.app.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Configuración web unificada: interceptors, i18n y componentes MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthNavigationInterceptor authNavigationInterceptor;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final UserRepository userRepository;

    public WebConfig(AuthNavigationInterceptor authNavigationInterceptor,
                     CurrentUserArgumentResolver currentUserArgumentResolver,
                     UserRepository userRepository) {
        this.authNavigationInterceptor = authNavigationInterceptor;
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.userRepository = userRepository;
    }

    // ========== INTERNACIONALIZACIÓN ==========

    @Bean
    public MessageSource messageSource() throws IOException {
        return new JsonMessageSource(new ObjectMapper());
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new CityAwareLocaleResolver(userRepository);
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    // ========== INTERCEPTORS ==========

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(authNavigationInterceptor)
                .addPathPatterns(Routes.GUEST_ONLY_PATHS.toArray(String[]::new));
    }

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
        registry.addResourceHandler("/docs/**")
                .addResourceLocations("classpath:/docs/", "file:docs/");
    }
}
