package com.residuosolido.app.interceptor;

import com.residuosolido.app.service.BreadcrumbService;
import com.residuosolido.app.view.BreadcrumbItem;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Interceptor que genera automáticamente los breadcrumbs para todas las páginas.
 * Se ejecuta después de cada request y agrega los breadcrumbs al modelo.
 */
@Component
public class BreadcrumbInterceptor implements HandlerInterceptor {

    @Autowired
    private BreadcrumbService breadcrumbService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                          Object handler, ModelAndView modelAndView) throws Exception {
        
        // Solo procesar si hay un ModelAndView y no es una redirección
        if (modelAndView == null || modelAndView.getViewName() == null || 
            modelAndView.getViewName().startsWith("redirect:")) {
            return;
        }
        
        // Solo procesar páginas de admin y usuarios (no APIs ni recursos estáticos)
        String requestURI = request.getRequestURI();
        if (!debeGenerarBreadcrumbs(requestURI)) {
            return;
        }
        
        try {
            ModelMap model = modelAndView.getModelMap();
            
            // Generar breadcrumbs automáticamente
            List<BreadcrumbItem> breadcrumbs = breadcrumbService.generarBreadcrumbs(requestURI, model);
            String tituloFinal = breadcrumbService.generarTituloFinal(requestURI, model);
            
            // Agregar al modelo para que el template los use
            model.addAttribute("breadcrumbItems", breadcrumbs);
            model.addAttribute("breadcrumbTitle", tituloFinal);
            
        } catch (Exception e) {
            // En caso de error, no fallar la request
        }
    }
    
    /**
     * Determina si se deben generar breadcrumbs para esta URL.
     */
    private boolean debeGenerarBreadcrumbs(String requestURI) {
        // Excluir APIs REST, recursos estáticos, etc.
        if (requestURI.startsWith("/api/") || 
            requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") ||
            requestURI.startsWith("/webjars/") ||
            requestURI.contains(".css") ||
            requestURI.contains(".js") ||
            requestURI.contains(".png") ||
            requestURI.contains(".jpg") ||
            requestURI.contains(".ico")) {
            return false;
        }
        
        // Incluir páginas de admin y usuarios
        return requestURI.startsWith("/admin/") || 
               requestURI.startsWith("/users/") ||
               requestURI.equals("/") ||
               requestURI.startsWith("/public/");
    }
}
