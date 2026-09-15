package com.residuosolido.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final LoginSuccessHandler successHandler;
    private final LoginFailureHandler failureHandler;
 
    public SecurityConfig(LoginSuccessHandler successHandler, LoginFailureHandler failureHandler) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (PRIMERO) - Acceso sin autenticación
                .requestMatchers(Routes.HOME, Routes.INDEX).permitAll()
                .requestMatchers(Routes.LOGIN, "/login", Routes.REGISTER, "/register").permitAll()
                .requestMatchers(Routes.LANGUAGE).permitAll()
                // Recursos especiales de navegador
                .requestMatchers(Routes.WELL_KNOWN).permitAll()
                // Páginas de error deben ser públicas
                .requestMatchers(Routes.ERROR).permitAll()
                .requestMatchers("/css/**", "/js/**", "/i18n/**", "/images/**", "/fonts/**", "/static/**", "/favicon.ico", "/favicon.*", "/webjars/**", "/uploads/**", "/manifest.json", "/sw.js", "/icon-*.png", "/icon-*.svg").permitAll()
                // Formulario público de nueva solicitud
                .requestMatchers(HttpMethod.GET, Routes.REQUESTS_NEW).permitAll()
                .requestMatchers(HttpMethod.POST, Routes.REQUESTS).permitAll()
                .requestMatchers(HttpMethod.GET, Routes.REQUESTS_SUCCESS).permitAll()
                .requestMatchers(Routes.TRACK).permitAll()
                .requestMatchers(HttpMethod.GET, Routes.API_ORGANIZATIONS_BY_CITY).permitAll()
                .requestMatchers(Routes.METRICAS).permitAll()
                .requestMatchers(Routes.BLOG).permitAll()
                .requestMatchers(Routes.DOCUMENTOS, Routes.DIAGRAMAS).permitAll()
                .requestMatchers(Routes.DOCS_ANY).permitAll()
                .requestMatchers(Routes.ACTUATOR_HEALTH).permitAll()
                .requestMatchers(Routes.SWAGGER_V3, Routes.SWAGGER_UI, Routes.SWAGGER_HTML).permitAll()
                // API endpoints para usuarios autenticados
                .requestMatchers(Routes.API_ANY).authenticated()
                // Rutas de usuarios regulares
                .requestMatchers("/usuarios/**").hasRole("USER")
                // Rutas de organización
                .requestMatchers("/acopio/**").hasRole("ORGANIZATION")
                // Otras rutas requieren autenticación (ÚLTIMO)
                .anyRequest().authenticated()
            )
            // Manejo por defecto: redirige a /auth/login para recursos HTML
            .formLogin(form -> form
                .loginPage(Routes.LOGIN)
                .loginProcessingUrl(Routes.LOGIN)
                .successHandler(successHandler)
                .failureHandler(failureHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl(Routes.LOGOUT)
                // Usar un flag simple para evitar problemas de codificación en la URL
                .logoutSuccessUrl(Routes.HOME)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // No usar SavedRequest para decidir redirecciones tras login
            .requestCache(rc -> rc.disable())
            // Cabeceras de seguridad
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "img-src 'self' data: https: https://tile.openstreetmap.org; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com https://www.draw.io; " +
                    "font-src 'self' data: https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                    "script-src 'self' 'unsafe-inline' https://www.draw.io; " +
                    "connect-src 'self'; " +
                    "frame-src 'self' https://www.openstreetmap.org https://www.draw.io"
                ))
                .frameOptions(frame -> frame.sameOrigin())
                .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            // Protección de sesión: cambiar ID en autenticación
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.changeSessionId())
            );
        
        return http.build();
    }
}
