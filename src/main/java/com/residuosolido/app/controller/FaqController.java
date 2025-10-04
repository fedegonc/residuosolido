package com.residuosolido.app.controller;

import com.residuosolido.app.service.GeminiService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/faq")
public class FaqController {

    @Autowired
    private GeminiService geminiService;

    /**
     * Endpoint para procesar mensajes del chat con Gemini
     */
    @PostMapping("/chat/message")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processChatMessage(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        String userMessage = request.get("message");
        String sessionId = session.getId();
        
        // Llamar a Gemini con el sessionId para mantener contexto
        String geminiResponse = geminiService.chat(userMessage, sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", geminiResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para limpiar el historial de conversación
     */
    @PostMapping("/chat/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearChatHistory(HttpSession session) {
        String sessionId = session.getId();
        geminiService.clearHistory(sessionId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Historial limpiado");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para obtener el estado del servicio de chat
     */
    @GetMapping("/chat/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChatStatus(HttpSession session) {
        String sessionId = session.getId();
        int historySize = geminiService.getHistorySize(sessionId);
        
        Map<String, Object> status = new HashMap<>();
        status.put("available", true);
        status.put("message", "Chat service is running");
        status.put("historySize", historySize);
        status.put("sessionId", sessionId);
        
        return ResponseEntity.ok(status);
    }
    @GetMapping("/questions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getFrequentQuestions() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Endpoint de preguntas frecuentes - En desarrollo");
        
        return ResponseEntity.ok(response);
    }
}
