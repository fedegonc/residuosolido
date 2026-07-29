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

    public SecurityConfig(LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (PRIMERO) - Acceso sin autenticación
                .requestMatchers("/", "/index").permitAll()
                .requestMatchers("/auth/**", "/login", "/register").permitAll()
                .requestMatchers("/change-language").permitAll()
                // Recursos especiales de navegador (evitar guardarlos como destino de login)
                .requestMatchers("/.well-known/**").permitAll()
                // Páginas de error deben ser públicas para evitar AccessDenied en flujos de error
                .requestMatchers("/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/static/**", "/favicon.ico", "/favicon.*", "/webjars/**", "/uploads/**").permitAll()
                // Formulario público de nueva solicitud (invitado o autenticado)
                .requestMatchers(HttpMethod.GET, "/solicitudes/nueva").permitAll()
                .requestMatchers(HttpMethod.POST, "/solicitudes").permitAll()
                .requestMatchers("/rastrear").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/organizations/by-city").permitAll()
                .requestMatchers("/metricas").permitAll()
                // API endpoints para usuarios autenticados
                .requestMatchers("/api/**").authenticated()
                // Rutas de usuarios regulares
                .requestMatchers("/usuarios/**").hasRole("USER")
                // Rutas de organización
                .requestMatchers("/acopio/**").hasRole("ORGANIZATION")
                // Otras rutas requieren autenticación (ÚLTIMO)
                .anyRequest().authenticated()
            )
            // Manejo por defecto: redirige a /auth/login para recursos HTML
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                // Usar un flag simple para evitar problemas de codificación en la URL
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // No usar SavedRequest para decidir redirecciones tras login
            .requestCache(rc -> rc.disable())
            // Cabeceras de seguridad razonables sin romper Tailwind CDN ni Cloudinary
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "img-src 'self' data: https: https://tile.openstreetmap.org; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com https://unpkg.com; " +
                    "font-src 'self' data: https://fonts.gstatic.com; " +
                    "script-src 'self' 'nonce-{cspNonce}' https://cdn.tailwindcss.com https://unpkg.com https://cdn.jsdelivr.net; " +
                    "connect-src 'self' https://unpkg.com https://cdn.jsdelivr.net; " +
                    "frame-src 'self' https://www.openstreetmap.org https://www.google.com https://maps.google.com"
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
