package com.residuosolido.app.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Configuración para la internacionalización de la aplicación.
 * Permite cambiar el idioma usando el parámetro 'lang' en la URL.
 */
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * Define el MessageSource para cargar los mensajes de los archivos de propiedades.
     * @return MessageSource configurado
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    
    /**
     * Define el resolver de locale que utiliza la sesión para almacenar la preferencia de idioma.
     * @return LocaleResolver configurado
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        // Configurar portugués como idioma por defecto
        localeResolver.setDefaultLocale(new Locale("pt"));
        return localeResolver;
    }

    /**
     * Define el interceptor que cambia el locale basado en el parámetro 'lang' en la URL.
     * @return LocaleChangeInterceptor configurado
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Usar ?lang=es o ?lang=pt en la URL
        return interceptor;
    }

    /**
     * Registra el interceptor de cambio de locale en la aplicación.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
