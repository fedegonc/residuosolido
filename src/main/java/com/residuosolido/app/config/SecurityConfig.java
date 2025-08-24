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
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                // Rutas públicas (PRIMERO) - Acceso sin autenticación
                .requestMatchers("/", "/index", "/invitados", "/guest/**").permitAll()
                .requestMatchers("/auth/**", "/login", "/register").permitAll()
                .requestMatchers("/posts/**", "/categories/**").permitAll()
                .requestMatchers("/change-language").permitAll() // Cambio de idioma público
                // Páginas de error deben ser públicas para evitar AccessDenied en flujos de error
                .requestMatchers("/error").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/fonts/**", "/static/**", "/favicon.ico", "/favicon.*").permitAll()
                // Rutas de administrador
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Rutas de utilidades de desarrollo deshabilitadas
                // Rutas de gestión de usuarios para admin
                .requestMatchers("/admin/users/**").hasRole("ADMIN")
                // Rutas de usuario (solo usuarios)
                .requestMatchers("/users/**").hasRole("USER")
                .requestMatchers("/requests/**").hasRole("USER")
                // Rutas de organización
                .requestMatchers("/org/**").hasRole("ORGANIZATION")
                // Feedback requiere autenticación
                .requestMatchers("/feedback/**").authenticated()
                // Otras rutas requieren autenticación (ÚLTIMO)
                .anyRequest().authenticated()
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
            // Desactivamos la protección de sesión para depuración
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
            );
        
        return http.build();
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
