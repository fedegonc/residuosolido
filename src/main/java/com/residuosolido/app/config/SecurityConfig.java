package com.residuosolido.app.config;

import com.residuosolido.app.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.config.Customizer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    
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
                .requestMatchers("/", "/index", "/invitados", "/guest/**").permitAll()
                .requestMatchers("/auth/**", "/login", "/register").permitAll()
                .requestMatchers("/posts/**", "/categories/**").permitAll()
                .requestMatchers("/sistema-visual", "/grid-test").permitAll()
                .requestMatchers("/change-language").permitAll() // Cambio de idioma público
                // Recursos especiales de navegador (evitar guardarlos como destino de login)
                .requestMatchers("/.well-known/**").permitAll()
                // Páginas de error deben ser públicas para evitar AccessDenied en flujos de error
                .requestMatchers("/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/static/**", "/favicon.ico", "/favicon.*", "/webjars/**").permitAll()
                // Rutas de administrador
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Rutas de utilidades de desarrollo deshabilitadas
                // Rutas de gestión de usuarios para admin
                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                // Rutas de navegación personal (cualquier usuario autenticado)
                .requestMatchers("/user/**").authenticated()
                .requestMatchers("/requests/**").hasRole("USER")
                // Rutas de organización
                .requestMatchers("/org/**").hasRole("ORGANIZATION")
                // Feedback requiere autenticación
                .requestMatchers("/feedback/**").authenticated()
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
                    "img-src 'self' data: https:; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                    "font-src 'self' data: https://fonts.gstatic.com; " +
                    "script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com https://unpkg.com; " +
                    "connect-src 'self'"
                ))
                .frameOptions(frame -> frame.sameOrigin())
                .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
            )
            // Desactivamos la protección de sesión para depuración
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
            );
        
        return http.build();
    }

    // Sin AuthenticationEntryPoint específico para API: app HTML simple

    // Sin CORS: aplicación strictly same-origin

    // Exponer AuthenticationManager sin depender de AuthenticationConfiguration
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .map(user -> {
                // Logging de diagnóstico
                log.info("[AUTH][LOAD] username='{}' | id={} | role={} | active={} | passHashLen={}",
                        user.getUsername(), user.getId(), user.getRole(), user.isActive(),
                        (user.getPassword() != null ? user.getPassword().length() : -1));
                return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                );
            })
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
