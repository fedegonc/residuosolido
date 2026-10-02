package com.residuosolido.app.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback si JavaScript no concatenó los 4 dígitos del PIN.
 * Permitir que el PIN se envíe como:
 * - password=1234 (JS funcionó, enviado por .pin-boxes__value)
 * - password-1=1, password-2=2, password-3=3, password-4=4 (JS falló)
 *
 * Este filtro concatena automáticamente si vienen los 4 campos separados.
 */
@Component
public class PinFallbackFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Solo procesar POST a /entrar (login)
        if ("POST".equals(request.getMethod()) && "/entrar".equals(request.getRequestURI())) {
            String password = request.getParameter("password");

            // Si password está vacío pero tenemos los 4 dígitos separados, concatenar
            if ((password == null || password.trim().isEmpty())) {
                String d1 = request.getParameter("password-1");
                String d2 = request.getParameter("password-2");
                String d3 = request.getParameter("password-3");
                String d4 = request.getParameter("password-4");

                if (d1 != null && d2 != null && d3 != null && d4 != null) {
                    String concatenated = d1 + d2 + d3 + d4;
                    if (!concatenated.trim().isEmpty()) {
                        request = new ParameterWrapper(request, "password", concatenated);
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Wrapper que sobrescribe getParameter para devolver el PIN concatenado.
     */
    private static class ParameterWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> params;

        ParameterWrapper(HttpServletRequest request, String overrideKey, String overrideValue) {
            super(request);
            params = new HashMap<>(request.getParameterMap());
            params.put(overrideKey, new String[]{overrideValue});
        }

        @Override
        public String getParameter(String name) {
            String[] values = params.get(name);
            return values != null && values.length > 0 ? values[0] : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.get(name);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(params.keySet());
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return Collections.unmodifiableMap(params);
        }
    }
}
