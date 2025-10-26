package com.residuosolido.app.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.residuosolido.app.model.AssistantConfig;
import com.residuosolido.app.model.ChatHistory;
import com.residuosolido.app.repository.AssistantConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GeminiService {

    @Value("${apikey_gemini}")
    private String apiKey;

    @Autowired
    private AssistantConfigRepository assistantConfigRepository;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<>() {};
    private static final TypeReference<List<Map<String, Object>>> LIST_MAP_TYPE_REF = new TypeReference<>() {};
    
    // Memoria de conversaciones por sessionId (en RAM)
    private final Map<String, ChatHistory> conversationHistory = new ConcurrentHashMap<>();

    public String chat(String userMessage, String sessionId) {
        
        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: API Key de Gemini no configurada. Verifica tu archivo .env";
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Obtener o crear historial para esta sesión
            ChatHistory history = conversationHistory.computeIfAbsent(sessionId, k -> {
                ChatHistory newHistory = new ChatHistory();
                
                // Obtener el system prompt configurado
                AssistantConfig config = assistantConfigRepository
                    .findFirstByIsActiveTrueOrderByUpdatedAtDesc()
                    .orElse(null);
                
                // Si hay system prompt, agregarlo como primer mensaje
                if (config != null && config.getSystemPrompt() != null && !config.getSystemPrompt().isEmpty()) {
                    newHistory.addMessage("user", config.getSystemPrompt());
                    newHistory.addMessage("model", "Entendido. Responderé según el contexto proporcionado.");
                }
                
                return newHistory;
            });
            
            // Agregar el mensaje del usuario al historial
            history.addMessage("user", userMessage);
            
            // Construir el request body con todo el historial
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", history.getMessages());

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Llamada a la API
            String url = GEMINI_API_URL + "?key=" + apiKey;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            // Extraer la respuesta
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                List<Map<String, Object>> candidates = extractMapList(responseBody.get("candidates"));
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> contentResponse = extractMap(firstCandidate.get("content"));
                    List<Map<String, Object>> parts = contentResponse != null ? extractMapList(contentResponse.get("parts")) : List.of();
                    if (!parts.isEmpty()) {
                        String geminiText = (String) parts.get(0).get("text");
                        
                        // Agregar la respuesta del modelo al historial
                        history.addMessage("model", geminiText);
                        
                        return geminiText;
                    }
                }
            }

            return "No se pudo obtener respuesta de Gemini - Body vacío o sin candidates";

        } catch (Exception e) {
            return "Lo siento, hubo un error al procesar tu mensaje. Por favor, intenta de nuevo.";
        }
    }

    private List<Map<String, Object>> extractMapList(Object value) {
        if (value == null) {
            return List.of();
        }
        try {
            return OBJECT_MAPPER.convertValue(value, LIST_MAP_TYPE_REF);
        } catch (IllegalArgumentException ex) {
            return List.of();
        }
    }

    private Map<String, Object> extractMap(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.convertValue(value, MAP_TYPE_REF);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    /**
     * Limpia el historial de conversación para una sesión específica
     */
    public void clearHistory(String sessionId) {
        conversationHistory.remove(sessionId);
    }
    
    /**
     * Obtiene el número de mensajes en el historial de una sesión
     */
    public int getHistorySize(String sessionId) {
        ChatHistory history = conversationHistory.get(sessionId);
        return history != null ? history.size() : 0;
    }
}
