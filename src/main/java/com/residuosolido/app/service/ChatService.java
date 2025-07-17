package com.residuosolido.app.service;

import com.residuosolido.app.model.ChatMessage;
import com.residuosolido.app.model.Request;
import com.residuosolido.app.model.User;
import com.residuosolido.app.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar los mensajes de chat entre usuarios
 */
@Service
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Autowired
    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Crea un nuevo mensaje en el chat de una solicitud
     * @param request Solicitud a la que pertenece el mensaje
     * @param sender Usuario que envía el mensaje
     * @param receiver Usuario que recibe el mensaje
     * @param content Contenido del mensaje
     * @return El mensaje creado
     */
    @Transactional
    public ChatMessage createMessage(Request request, User sender, User receiver, String content) {
        ChatMessage message = new ChatMessage();
        message.setRequest(request);
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        
        // Añadir el mensaje a la lista de mensajes de la solicitud
        request.getChatMessages().add(message);
        
        return chatMessageRepository.save(message);
    }

    /**
     * Obtiene todos los mensajes de una solicitud ordenados por fecha de envío
     * @param requestId ID de la solicitud
     * @return Lista de mensajes ordenados
     */
    public List<ChatMessage> getMessagesByRequestSorted(Long requestId) {
        return chatMessageRepository.findByRequestIdOrderBySentAtAsc(requestId);
    }

    /**
     * Obtiene los mensajes no leídos para un usuario específico en una solicitud
     * @param requestId ID de la solicitud
     * @param userId ID del usuario
     * @return Lista de mensajes no leídos
     */
    public List<ChatMessage> getUnreadMessagesForUser(Long requestId, Long userId) {
        return chatMessageRepository.findByRequestIdOrderBySentAtAsc(requestId).stream()
                .filter(m -> m.getReceiver().getId().equals(userId))
                .filter(m -> m.getStatus() != ChatMessage.MessageStatus.READ)
                .collect(Collectors.toList());
    }

    /**
     * Marca todos los mensajes como leídos para un usuario específico en una solicitud
     * @param requestId ID de la solicitud
     * @param userId ID del usuario
     */
    @Transactional
    public void markAllMessagesAsReadForUser(Long requestId, Long userId) {
        List<ChatMessage> unreadMessages = getUnreadMessagesForUser(requestId, userId);
        unreadMessages.forEach(ChatMessage::markAsRead);
        chatMessageRepository.saveAll(unreadMessages);
    }

    /**
     * Obtiene la conversación entre dos usuarios en una solicitud específica
     * @param requestId ID de la solicitud
     * @param user1Id ID del primer usuario
     * @param user2Id ID del segundo usuario
     * @return Lista de mensajes ordenados por fecha
     */
    public List<ChatMessage> getConversationBetweenUsers(Long requestId, Long user1Id, Long user2Id) {
        return chatMessageRepository.findConversationBetweenUsers(requestId, user1Id, user2Id);
    }

    /**
     * Cuenta el número de mensajes no leídos para un usuario
     * @param userId ID del usuario
     * @return Número de mensajes no leídos
     */
    public Long countUnreadMessagesForUser(Long userId) {
        return chatMessageRepository.countUnreadMessagesForUser(userId);
    }
}
