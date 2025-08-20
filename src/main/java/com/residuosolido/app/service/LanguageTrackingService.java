package com.residuosolido.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LanguageTrackingService {
    
    private static final Logger logger = LoggerFactory.getLogger(LanguageTrackingService.class);
    
    // Contadores de cambios de idioma
    private final AtomicLong totalChanges = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> languageStats = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> sessionStats = new ConcurrentHashMap<>();
    
    public void trackLanguageChange(String fromLang, String toLang, String sessionId, String clientIP) {
        long changeNumber = totalChanges.incrementAndGet();
        
        // Incrementar estadísticas por idioma
        languageStats.computeIfAbsent(toLang, k -> new AtomicLong(0)).incrementAndGet();
        
        // Incrementar estadísticas por sesión
        sessionStats.computeIfAbsent(sessionId, k -> new AtomicLong(0)).incrementAndGet();
        
        logger.info("📊 LANGUAGE_STATS: Change #{} | {} -> {} | Session: {} changes | IP: {}", 
                   changeNumber, fromLang, toLang, 
                   sessionStats.get(sessionId).get(), clientIP);
        
        // Log estadísticas cada 10 cambios
        if (changeNumber % 10 == 0) {
            logStatistics();
        }
    }
    
    public void logStatistics() {
        logger.info("📈 LANGUAGE_USAGE_STATS:");
        logger.info("   Total changes: {}", totalChanges.get());
        languageStats.forEach((lang, count) -> 
            logger.info("   {} usage: {} times", lang.toUpperCase(), count.get()));
        logger.info("   Active sessions: {}", sessionStats.size());
    }
    
    public long getTotalChanges() {
        return totalChanges.get();
    }
    
    public ConcurrentHashMap<String, AtomicLong> getLanguageStats() {
        return languageStats;
    }
}
