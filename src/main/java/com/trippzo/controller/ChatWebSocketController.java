package com.trippzo.controller;

import com.trippzo.model.dto.ChatMessage;
import com.trippzo.model.Message;
import com.trippzo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    // Получаване и обработка на изпратено съобщение
    @MessageMapping("/chat.sendMessage")
    public void processMessage(ChatMessage chatMessage, Principal principal) {
        // Записване на съобщението в базата
        Message savedMsg = chatService.saveMessage(
                chatMessage.getTripId(),
                principal.getName(),          // логнатият потребител - подател
                chatMessage.getContent(),
                chatMessage.getReceiverUsername()
        );

        if (savedMsg != null) {
            // Изпращане до получателя (user destination)
            messagingTemplate.convertAndSendToUser(
                    chatMessage.getReceiverUsername(),
                    "/queue/messages",
                    savedMsg
            );

            // Изпращане до подателя, за синхронизация
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/messages",
                    savedMsg
            );
        }
    }

    // Маркиране на съобщения като прочетени
    @MessageMapping("/chat.readMessages")
    public void markAsRead(ChatMessage chatMessage, Principal principal) {
        chatService.markMessagesAsRead(chatMessage.getReceiverUsername(), principal.getName());

        // Изпращане на статус към подателя
        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiverUsername(),
                "/queue/read-status",
                principal.getName() // или DTO с по-подробна информация
        );
    }

    // Изпращане на съобщение, че потребителят пише
    @MessageMapping("/typing")
    public void typingNotification(ChatMessage message, Principal principal) {
        messagingTemplate.convertAndSendToUser(
                message.getReceiverUsername(),
                "/queue/typing",
                principal.getName()
        );
    }
}
