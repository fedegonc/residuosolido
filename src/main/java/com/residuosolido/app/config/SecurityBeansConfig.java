package com.residuosolido.app.config;

import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityBeansConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityBeansConfig.class);

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, LoginAttemptService loginAttemptService) {
        return username -> {
            if (loginAttemptService.isBlocked(username)) {
                throw new LockedException("Cuenta bloqueada temporalmente por múltiples intentos fallidos");
            }
            return userRepository.findByUsername(username)
                .map(user -> {
                    log.debug("[AUTH][LOAD] username='{}' | role={} | active={}",
                            user.getUsername(), user.getRole(), user.isActive());
                    return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
