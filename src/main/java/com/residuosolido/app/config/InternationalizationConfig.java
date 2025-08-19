package com.residuosolido.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {
    
    private static final Logger log = LoggerFactory.getLogger(InternationalizationConfig.class);

    @Bean
    public MessageSource messageSource() {
        log.info("[I18N] Configurando MessageSource");
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache por 1 hora
        log.info("[I18N] MessageSource configurado con basename: classpath:messages");
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        log.info("[I18N] Configurando LocaleResolver");
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("pt")); // Portugués por defecto
        log.info("[I18N] LocaleResolver configurado con locale por defecto: pt");
        return slr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        log.info("[I18N] Configurando LocaleChangeInterceptor");
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // Parámetro para cambiar idioma: ?lang=es
        log.info("[I18N] LocaleChangeInterceptor configurado con param: lang");
        return lci;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("[I18N] Registrando LocaleChangeInterceptor");
        registry.addInterceptor(localeChangeInterceptor());
        log.info("[I18N] LocaleChangeInterceptor registrado correctamente");
    }
}
