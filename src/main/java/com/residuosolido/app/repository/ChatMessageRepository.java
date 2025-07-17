package com.residuosolido.app.repository;

import com.residuosolido.app.model.ChatMessage;
import com.residuosolido.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    /**
     * Encuentra todos los mensajes de una solicitud específica ordenados por fecha de envío
     */
    List<ChatMessage> findByRequestIdOrderBySentAtAsc(Long requestId);
    
    /**
     * Encuentra todos los mensajes enviados por un usuario específico
     */
    List<ChatMessage> findBySenderId(Long senderId);
    
    /**
     * Encuentra todos los mensajes recibidos por un usuario específico
     */
    List<ChatMessage> findByReceiverId(Long receiverId);
    
    /**
     * Encuentra todos los mensajes no leídos para un usuario específico
     */
    List<ChatMessage> findByReceiverIdAndStatusNot(Long receiverId, ChatMessage.MessageStatus status);
    
    /**
     * Cuenta el número de mensajes no leídos para un usuario específico
     */
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.receiver.id = :userId AND cm.status != 'READ'")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);
    
    /**
     * Encuentra la conversación entre dos usuarios en una solicitud específica
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.request.id = :requestId AND " +
           "((cm.sender.id = :user1Id AND cm.receiver.id = :user2Id) OR " +
           "(cm.sender.id = :user2Id AND cm.receiver.id = :user1Id)) " +
           "ORDER BY cm.sentAt ASC")
    List<ChatMessage> findConversationBetweenUsers(
            @Param("requestId") Long requestId,
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id);
}
