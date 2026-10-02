package com.residuosolido.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.config.Customizer;
import java.util.concurrent.Executor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableAsync
public class SecurityConfig {

    private final AuthenticationEventHandler authHandler;

    public SecurityConfig(AuthenticationEventHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (PRIMERO) - Acceso sin autenticación
                .requestMatchers(Routes.HOME, Routes.INDEX).permitAll()
                .requestMatchers(Routes.LOGIN, Routes.REGISTER, Routes.REGISTER_ORG).permitAll()
                .requestMatchers(Routes.PAGE_BY_SLUG).permitAll()
                .requestMatchers(Routes.QR).permitAll()
                // Recursos especiales de navegador
                .requestMatchers(Routes.WELL_KNOWN).permitAll()
                // Páginas de error deben ser públicas
                .requestMatchers(Routes.ERROR).permitAll()
                .requestMatchers(Routes.PUBLIC_STATIC).permitAll()
                // Opciones de orga (sin auth)
                .requestMatchers(HttpMethod.GET, Routes.ORG_OPTIONS).permitAll()
                .requestMatchers(Routes.DOCS_ANY).permitAll()
                .requestMatchers(Routes.ACTUATOR_HEALTH, Routes.ACTUATOR_INFO).permitAll()
                // Admin endpoints (dev only, protected at controller level via @Profile)
                .requestMatchers(Routes.ADMIN_ANY).permitAll()
                // Rutas de usuarios regulares
                .requestMatchers(Routes.REQUESTS_NEW, Routes.REQUESTS, Routes.REQUESTS_ANY, Routes.NOTIFICATIONS).hasRole("USER")
                // Rutas de organización
                .requestMatchers(Routes.ORG_ANY, Routes.ORG_PROFILE).hasRole("ORGANIZATION")
                // Otras rutas requieren autenticación (ÚLTIMO)
                .anyRequest().authenticated()
            )
            // Manejo por defecto: redirige a /entrar para recursos HTML
            .formLogin(form -> form
                .loginPage(Routes.LOGIN)
                .loginProcessingUrl(Routes.LOGIN)
                .successHandler(authHandler)
                .failureHandler(authHandler)
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
                    "img-src 'self' data: https://tile.openstreetmap.org https://images.pexels.com; " +
                    "style-src 'self' 'unsafe-inline' https://viewer.diagrams.net; " +
                    "font-src 'self' data: https://viewer.diagrams.net; " +
                    "script-src 'self' https://viewer.diagrams.net; " +
                    "connect-src 'self' https://images.pexels.com https://viewer.diagrams.net; " +
                    "frame-src 'self' https://www.openstreetmap.org https://www.draw.io"
                ))
                .frameOptions(frame -> frame.sameOrigin())
                .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            // Protección de sesión: cambiar ID en autenticación
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(fixation -> fixation.changeSessionId())
            );

        return http.build();
    }

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("notif-");
        executor.initialize();
        return executor;
    }
}
