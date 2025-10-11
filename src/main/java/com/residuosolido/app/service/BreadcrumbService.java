package com.residuosolido.app.service;

import com.residuosolido.app.model.User;
import com.residuosolido.app.view.BreadcrumbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para generar breadcrumbs automáticamente basado en la URL.
 * Analiza la URI y construye la jerarquía de navegación dinámicamente.
 */
@Service
public class BreadcrumbService {

    @Autowired
    private UserService userService;

    /**
     * Genera breadcrumbs automáticamente basado en la URI de la request.
     * 
     * @param requestURI URI de la request (ej: /admin/users/view?id=123)
     * @param model ModelMap de Spring para obtener datos adicionales
     * @return Lista de BreadcrumbItems para usar en el template
     */
    public List<BreadcrumbItem> generarBreadcrumbs(String requestURI, ModelMap model) {
        List<BreadcrumbItem> breadcrumbs = new ArrayList<>();
        
        // Limpiar la URI de parámetros de query
        String cleanURI = requestURI.split("\\?")[0];
        String[] segmentos = cleanURI.split("/");
        
        // Procesar segmentos de la URL
        for (int i = 0; i < segmentos.length; i++) {
            String segmento = segmentos[i];
            
            if (segmento.isEmpty()) continue;
            
            switch (segmento.toLowerCase()) {
                case "admin":
                    // No agregamos "Admin" al breadcrumb, es implícito
                    break;
                    
                case "dashboard":
                case "inicio":
                    // Determinar contexto
                    if (cleanURI.startsWith("/admin/")) {
                        breadcrumbs.add(new BreadcrumbItem("Inicio", "/admin/dashboard"));
                    } else if (cleanURI.startsWith("/usuarios/")) {
                        breadcrumbs.add(new BreadcrumbItem("Inicio", "/usuarios/inicio"));
                    } else if (cleanURI.startsWith("/acopio/")) {
                        breadcrumbs.add(new BreadcrumbItem("Inicio", "/acopio/inicio"));
                    }
                    break;
                    
                case "users":
                case "usuarios":
                    // Determinar si es admin o usuario según el contexto
                    if (cleanURI.startsWith("/admin/")) {
                        breadcrumbs.add(new BreadcrumbItem("Usuarios", "/admin/users"));
                    } else if (cleanURI.startsWith("/usuarios/")) {
                        breadcrumbs.add(new BreadcrumbItem("Inicio", "/usuarios/inicio"));
                    }
                    break;
                    
                case "organizations":
                case "organizaciones":
                    breadcrumbs.add(new BreadcrumbItem("Organizaciones", "/admin/organizations"));
                    break;
                    
                case "posts":
                    // Determinar si es admin o público según el contexto
                    if (cleanURI.startsWith("/admin/")) {
                        breadcrumbs.add(new BreadcrumbItem("Notas", "/admin/posts"));
                    } else {
                        // Para rutas públicas /posts, no agregar breadcrumb intermedio
                    }
                    break;
                    
                case "materials":
                case "materiales":
                    breadcrumbs.add(new BreadcrumbItem("Materiales", "/admin/materials"));
                    break;
                    
                case "categories":
                case "categorias":
                    breadcrumbs.add(new BreadcrumbItem("Categorías", "/admin/categories"));
                    break;
                    
                case "requests":
                case "solicitudes":
                    // Determinar si es admin o usuario según el contexto
                    if (cleanURI.startsWith("/admin/")) {
                        breadcrumbs.add(new BreadcrumbItem("Solicitudes", "/admin/requests"));
                    } else if (cleanURI.startsWith("/usuarios/")) {
                        breadcrumbs.add(new BreadcrumbItem("Solicitudes", "/usuarios/solicitudes"));
                    }
                    break;
                    
                case "feedback":
                    // Determinar si es admin o usuario según el contexto
                    if (cleanURI.startsWith("/admin/")) {
                        breadcrumbs.add(new BreadcrumbItem("Feedback", "/admin/feedback"));
                    } else {
                        // Para rutas públicas /feedback, no agregar breadcrumb intermedio
                        // El título se manejará en determinarTituloPorSegmentos
                    }
                    break;
                    
                case "my":
                    // Para /feedback/my
                    if (i > 0 && segmentos[i-1].equalsIgnoreCase("feedback")) {
                        breadcrumbs.add(new BreadcrumbItem("Reportar Problema", "/feedback"));
                    }
                    break;
                    
                case "statistics":
                case "estadisticas":
                    // Determinar contexto
                    if (cleanURI.startsWith("/admin/")) {
                        breadcrumbs.add(new BreadcrumbItem("Estadísticas", "/admin/statistics"));
                    } else if (cleanURI.startsWith("/acopio/")) {
                        breadcrumbs.add(new BreadcrumbItem("Estadísticas", "/acopio/estadisticas"));
                    }
                    break;
                    
                case "acopio":
                    // Ruta base para centros de acopio
                    breadcrumbs.add(new BreadcrumbItem("Inicio", "/acopio/inicio"));
                    break;
                    
                case "perfil":
                case "profile":
                    // No agregamos al breadcrumb, será el título final
                    break;
                    
                case "nueva":
                case "nuevo":
                case "new":
                    // No agregamos al breadcrumb, será el título final
                    break;
                    
                case "calendario":
                case "calendar":
                    if (cleanURI.startsWith("/acopio/")) {
                        breadcrumbs.add(new BreadcrumbItem("Calendario", "/acopio/calendario"));
                    }
                    break;
                    
                default:
                    // Manejar casos especiales como IDs o acciones
                    if (i > 0) {
                        String segmentoAnterior = segmentos[i - 1].toLowerCase();
                        procesarSegmentoEspecial(segmento, segmentoAnterior, breadcrumbs, model);
                    }
                    break;
            }
        }
        
        return breadcrumbs;
    }
    
    /**
     * Procesa segmentos especiales como acciones (nuevo, editar, view) o IDs.
     */
    private void procesarSegmentoEspecial(String segmento, String segmentoAnterior, 
                                        List<BreadcrumbItem> breadcrumbs, ModelMap model) {
        
        switch (segmento.toLowerCase()) {
            case "new":
            case "nuevo":
                // No agregamos al breadcrumb, será el título final
                break;
                
            case "edit":
            case "editar":
                // No agregamos al breadcrumb, será el título final
                break;
                
            case "view":
            case "detalle":
            case "details":
                // No agregamos al breadcrumb, será el título final
                break;
                
            default:
                // Verificar si es un ID numérico
                if (esNumerico(segmento)) {
                    procesarID(segmento, segmentoAnterior, breadcrumbs, model);
                }
                break;
        }
    }
    
    /**
     * Procesa IDs para obtener nombres de entidades.
     */
    private void procesarID(String id, String modulo, List<BreadcrumbItem> breadcrumbs, ModelMap model) {
        try {
            Long entityId = Long.parseLong(id);
            
            switch (modulo.toLowerCase()) {
                case "users":
                case "usuarios":
                    Optional<User> user = userService.findById(entityId);
                    if (user.isPresent()) {
                        String nombre = user.get().getFirstName() + " " + user.get().getLastName();
                        if (nombre.trim().isEmpty()) {
                            nombre = user.get().getUsername();
                        }
                        // No agregamos como breadcrumb intermedio, será el título final
                    }
                    break;
                    
                // Agregar otros casos según necesidades:
                // case "organizations": ...
                // case "posts": ...
            }
        } catch (NumberFormatException e) {
            // No es un ID válido, ignorar
        }
    }
    
    /**
     * Genera el título final basado en la URL y parámetros.
     */
    public String generarTituloFinal(String requestURI, ModelMap model) {
        String cleanURI = requestURI.split("\\?")[0];
        String[] segmentos = cleanURI.split("/");
        
        // Obtener parámetros de query si existen
        String action = obtenerParametroQuery(requestURI, "action");
        String id = obtenerParametroQuery(requestURI, "id");
        
        // Determinar el título basado en la acción
        if (action != null) {
            switch (action.toLowerCase()) {
                case "new":
                    return "Nuevo";
                case "edit":
                    return "Editar";
                case "view":
                    return obtenerNombreEntidad(id, segmentos, model);
                default:
                    return determinarTituloPorSegmentos(segmentos, model);
            }
        }
        
        return determinarTituloPorSegmentos(segmentos, model);
    }
    
    /**
     * Obtiene el nombre de una entidad por ID para usar como título.
     */
    private String obtenerNombreEntidad(String id, String[] segmentos, ModelMap model) {
        if (id == null || !esNumerico(id)) {
            return "Detalles";
        }
        
        try {
            Long entityId = Long.parseLong(id);
            
            // Buscar el módulo en los segmentos
            for (String segmento : segmentos) {
                switch (segmento.toLowerCase()) {
                    case "users":
                    case "usuarios":
                        Optional<User> user = userService.findById(entityId);
                        if (user.isPresent()) {
                            String nombre = user.get().getFirstName() + " " + user.get().getLastName();
                            return nombre.trim().isEmpty() ? user.get().getUsername() : nombre.trim();
                        }
                        break;
                        
                    // Agregar otros casos según necesidades
                }
            }
        } catch (NumberFormatException e) {
            // ID inválido
        }
        
        return "Detalles";
    }
    
    /**
     * Determina el título final basado en los segmentos de la URL.
     */
    private String determinarTituloPorSegmentos(String[] segmentos, ModelMap model) {
        if (segmentos.length == 0) return "Inicio";
        
        String ultimoSegmento = segmentos[segmentos.length - 1].toLowerCase();
        
        switch (ultimoSegmento) {
            case "users":
            case "usuarios":
                return "Usuarios";
            case "organizations":
            case "organizaciones":
                return "Organizaciones";
            case "dashboard":
            case "inicio":
                return "Inicio";
            case "posts":
                return "Notas";
            case "materials":
            case "materiales":
                return "Materiales";
            case "categories":
            case "categorias":
                return "Categorías";
            case "requests":
            case "solicitudes":
                return "Solicitudes";
            case "feedback":
                return "Reportar Problema";
            case "my":
                // Si el segmento anterior es feedback
                if (segmentos.length > 1 && segmentos[segmentos.length - 2].equalsIgnoreCase("feedback")) {
                    return "Mis Reportes";
                }
                return "Mis Datos";
            case "statistics":
            case "estadisticas":
                return "Estadísticas";
            case "perfil":
            case "profile":
                return "Perfil";
            case "nueva":
            case "nuevo":
            case "new":
                return "Nueva Solicitud";
            case "acopio":
                return "Centro de Acopio";
            case "calendario":
            case "calendar":
                return "Calendario";
            default:
                // Si es un ID, buscar en el segmento anterior
                if (esNumerico(ultimoSegmento) && segmentos.length > 1) {
                    return obtenerNombreEntidad(ultimoSegmento, segmentos, model);
                }
                return "Página";
        }
    }
    
    // Métodos auxiliares
    private boolean esNumerico(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private String obtenerParametroQuery(String uri, String parametro) {
        if (!uri.contains("?")) return null;
        
        String queryString = uri.split("\\?")[1];
        String[] params = queryString.split("&");
        
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(parametro)) {
                return keyValue[1];
            }
        }
        
        return null;
    }
}
