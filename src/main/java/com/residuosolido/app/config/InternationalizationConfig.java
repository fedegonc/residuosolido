package com.residuosolido.app.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 

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
}
