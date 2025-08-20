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
        
        logger.info("🔥 BACKEND: Recibido request en /api/tracking/console-log");
        logger.info("🔥 BACKEND: Datos recibidos: {}", logData);
        
        // Manejar batch de logs
        if (logData.containsKey("logs")) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> logs = (java.util.List<Map<String, Object>>) logData.get("logs");
            String sessionId = request.getSession().getId();
            
            logger.info("🔥 BACKEND: Procesando batch de {} logs", logs.size());
            
            for (Map<String, Object> log : logs) {
                String level = (String) log.get("level");
                String message = (String) log.get("message");
                String url = (String) log.get("url");
                
                logger.info("🖥️ FRONTEND_LOG [{}]: {} | Session: {}", 
                           level.toUpperCase(), message, sessionId);
                
                // Log especial para clicks de idioma
                if (message != null && message.contains("LANG_BTN_CLICK")) {
                    logger.info("🌍 LANGUAGE_CLICK_DETECTED: {} | Session: {} | IP: {}", 
                               message, sessionId, request.getRemoteAddr());
                }
            }
            
            logger.info("🔥 BACKEND: Batch procesado exitosamente");
            return ResponseEntity.ok("batch_logged");
        }
        
        // Manejar log individual (fallback)
        String level = (String) logData.get("level");
        String message = (String) logData.get("message");
        String url = (String) logData.get("url");
        String sessionId = request.getSession().getId();
        
        logger.info("🔥 BACKEND: Procesando log individual");
        logger.info("🖥️ FRONTEND_LOG [{}]: {} | Session: {}", 
                   level.toUpperCase(), message, sessionId);
        
        // Log especial para clicks de idioma
        if (message != null && message.contains("LANG_BTN_CLICK")) {
            logger.info("🌍 LANGUAGE_CLICK_DETECTED: {} | Session: {} | IP: {}", 
                       message, sessionId, request.getRemoteAddr());
        }
        
        return ResponseEntity.ok("logged");
    }
    
    @PostMapping("/performance")
    public ResponseEntity<String> trackPerformance(@RequestBody Map<String, Object> perfData,
                                                  HttpServletRequest request) {
        
        Long loadTime = ((Number) perfData.get("loadTime")).longValue();
        String url = (String) perfData.get("url");
        String sessionId = request.getSession().getId();
        
        logger.info("⚡ FRONTEND_PERF: {}ms | URL: {} | Session: {}", loadTime, url, sessionId);
        
        if (loadTime > 2000) {
            logger.warn("🐌 SLOW_FRONTEND: {}ms exceeds 2s threshold | URL: {}", loadTime, url);
        }
        
        return ResponseEntity.ok("tracked");
    }
}
