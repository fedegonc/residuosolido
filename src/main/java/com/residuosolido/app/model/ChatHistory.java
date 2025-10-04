package com.residuosolido.app.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChatHistory {
    private static final int MAX_MESSAGES = 20;
    private LinkedList<Map<String, Object>> messages;
    
    public ChatHistory() {
        this.messages = new LinkedList<>();
    }
    
    public void addMessage(String role, String text) {
        Map<String, Object> message = Map.of(
            "role", role,
            "parts", List.of(Map.of("text", text))
        );
        
        messages.add(message);
        
        // Mantener solo los últimos MAX_MESSAGES mensajes
        // Pero siempre mantener el system prompt (primeros 2 mensajes si existen)
        while (messages.size() > MAX_MESSAGES + 2) {
            // Eliminar el tercer mensaje (después del system prompt)
            if (messages.size() > 2) {
                messages.remove(2);
            }
        }
    }
    
    public List<Map<String, Object>> getMessages() {
        return new ArrayList<>(messages);
    }
    
    public void clear() {
        messages.clear();
    }
    
    public int size() {
        return messages.size();
    }
}
