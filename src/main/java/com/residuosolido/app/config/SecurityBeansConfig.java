package com.residuosolido.app.config;

import com.residuosolido.app.enums.Role;
import com.residuosolido.app.model.Username;
import com.residuosolido.app.repository.OrganizationRepository;
import com.residuosolido.app.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Beans de seguridad. Decisión deliberada (fase 2): la entidad {@code User}
 * NO implementa {@code UserDetails} — el principal es el {@code User} lean de
 * Spring construido abajo (username + hash + una authority + flag active),
 * que actúa como el DTO del boundary de autenticación. Implementar UserDetails
 * en la entidad metería el objeto completo en el SecurityContext/sesión y
 * reacoplaría el dominio al framework. Los controllers nunca leen el principal
 * directamente: {@code @CurrentUser} refetchea la entidad fresca por request,
 * evitando datos stale de la sesión. Guardado por HttpBoundaryContractTest.
 */
@Configuration
public class SecurityBeansConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityBeansConfig.class);

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, RateLimiter rateLimiter,
                                                 OrganizationRepository organizationRepository) {
        return rawUsername -> {
            String username = Username.canonical(rawUsername);
            HttpServletRequest request = getRequest();
            if (request != null && rateLimiter.isBlocked(request)) {
                throw new LockedException("IP bloqueada temporalmente por múltiples intentos fallidos");
            }
            return userRepository.findByUsername(username)
                .map(user -> {
                    // El rol no es un campo de User — se deriva: existe doc en
                    // organizations con su _id → ORGANIZATION, si no → USER.
                    Role role = organizationRepository.existsById(user.getId())
                            ? Role.ORGANIZATION : Role.USER;
                    log.debug("[AUTH][LOAD] username='{}' | role={} | active={}",
                            user.getUsername(), role, user.isActive());
                    return new org.springframework.security.core.userdetails.User(
                        user.getUsername(),
                        user.getPassword(),
                        user.isActive(),
                        true,
                        true,
                        true,
                        java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role.name()))
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Session usa "SESSION" como cookie por defecto; el logout y cualquier
     * cliente asumen JSESSIONID — se preserva el nombre para no cambiar contrato.
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("JSESSIONID");
        return serializer;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
