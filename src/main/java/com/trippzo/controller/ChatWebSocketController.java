package com.trippzo.controller;

import com.trippzo.model.Message;
import com.trippzo.model.User;
import com.trippzo.model.dto.ChatMessage;
import com.trippzo.service.ChatService;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Autowired
    public ChatWebSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate,
            UserService userService) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @MessageMapping("/chat.sendMessage")
    public void processMessage(ChatMessage chatMessage, Principal principal) {
        User sender = userService.getAuthenticatedUserFromPrincipal(principal);
        if (sender == null)
            return;

        Message savedMsg = chatService.saveMessage(chatMessage.getTripId(), sender.getUsername(),
                chatMessage.getContent(), chatMessage.getReceiverUsername());

        if (savedMsg != null) {
            messagingTemplate.convertAndSendToUser(chatMessage.getReceiverUsername(), "/queue/messages", savedMsg);

            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/messages", savedMsg);
        }
    }
}
