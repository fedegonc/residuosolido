package com.residuosolido.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/tracking")
public class LogTrackingController {
    
    private static final Logger logger = LoggerFactory.getLogger(LogTrackingController.class);

    @PostMapping("/console-log")
    public ResponseEntity<String> trackConsoleLog(@RequestBody Map<String, Object> logData, 
                                                 HttpServletRequest request) {
        
        // logs deshabilitados
        
        // Manejar batch de logs
        if (logData.containsKey("logs")) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> logs = (java.util.List<Map<String, Object>>) logData.get("logs");
            String sessionId = request.getSession().getId();
            
            // logs deshabilitados
            
            for (Map<String, Object> log : logs) {
                String level = (String) log.get("level");
                String message = (String) log.get("message");
                String url = (String) log.get("url");
                // logs deshabilitados
                // logs de clicks de idioma deshabilitados
            }
            
            return ResponseEntity.ok("batch_logged");
        }
        
        // Manejar log individual (fallback)
        String level = (String) logData.get("level");
        String message = (String) logData.get("message");
        String url = (String) logData.get("url");
        String sessionId = request.getSession().getId();
        
        // logs deshabilitados
        
        // logs de clicks de idioma deshabilitados
        
        return ResponseEntity.ok("logged");
    }
    
    @PostMapping("/performance")
    public ResponseEntity<String> trackPerformance(@RequestBody Map<String, Object> perfData,
                                                  HttpServletRequest request) {
        
        Long loadTime = ((Number) perfData.get("loadTime")).longValue();
        String url = (String) perfData.get("url");
        String sessionId = request.getSession().getId();
        
        // logs de performance deshabilitados (se mantiene warn en caso de lentitud)
        
        if (loadTime > 2000) {
            logger.warn("🐌 SLOW_FRONTEND: {}ms exceeds 2s threshold | URL: {}", loadTime, url);
        }
        
        return ResponseEntity.ok("tracked");
    }
}
