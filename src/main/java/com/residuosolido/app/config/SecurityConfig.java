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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final LoginSuccessHandler successHandler;

    public SecurityConfig(LoginSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (PRIMERO) - Acceso sin autenticación
                .requestMatchers("/", "/index", "/invitados", "/guest/**").permitAll()
                .requestMatchers("/auth/**", "/login", "/register").permitAll()
                // API pública: login API
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/posts/**", "/categories/**").permitAll()
                .requestMatchers("/sistema-visual", "/grid-test").permitAll()
                .requestMatchers("/change-language").permitAll() // Cambio de idioma público
                // Páginas de error deben ser públicas para evitar AccessDenied en flujos de error
                .requestMatchers("/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/static/**", "/favicon.ico", "/favicon.*").permitAll()
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
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(apiAwareAuthenticationEntryPoint())
            )
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
            // Cabeceras de seguridad razonables sin romper Tailwind CDN ni Cloudinary
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(
                    "default-src 'self'; " +
                    "img-src 'self' data: https:; " +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                    "font-src 'self' data: https://fonts.gstatic.com; " +
                    "script-src 'self' 'unsafe-inline' https://cdn.tailwindcss.com; " +
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

    // AuthenticationEntryPoint que devuelve 401 JSON para rutas /api/** y redirige a login para el resto
    private AuthenticationEntryPoint apiAwareAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            String uri = request.getRequestURI();
            if (uri != null && uri.startsWith("/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
            } else {
                response.sendRedirect("/auth/login");
            }
        };
    }

    // CORS centralizado para toda la app
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(java.util.List.of(
            "http://localhost:3000",
            "https://residuosolido.onrender.com"
        ));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
            .map(user -> new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            ))
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
