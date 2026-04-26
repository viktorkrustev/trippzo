package com.trippzo.controller;

import com.trippzo.model.Message;
import com.trippzo.model.dto.ChatMessage;
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

    @MessageMapping("/chat.sendMessage")
    public void processMessage(ChatMessage chatMessage, Principal principal) {
        Message savedMsg = chatService.saveMessage(chatMessage.getTripId(), principal.getName(),
                chatMessage.getContent(), chatMessage.getReceiverUsername());

        if (savedMsg != null) {
            messagingTemplate.convertAndSendToUser(chatMessage.getReceiverUsername(), "/queue/messages", savedMsg);

            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/messages", savedMsg);

            int totalUnread = chatService.countAllUnreadMessages(chatMessage.getReceiverUsername());
            messagingTemplate.convertAndSendToUser(chatMessage.getReceiverUsername(), "/queue/unread-count",
                    totalUnread);
        }
    }

    @MessageMapping("/chat.readMessages")
    public void markAsRead(ChatMessage chatMessage, Principal principal) {
        chatService.markMessagesAsRead(chatMessage.getReceiverUsername(), principal.getName());

        int totalUnread = chatService.countAllUnreadMessages(chatMessage.getReceiverUsername());
        messagingTemplate.convertAndSendToUser(chatMessage.getReceiverUsername(), "/queue/unread-count", totalUnread);
    }
}
