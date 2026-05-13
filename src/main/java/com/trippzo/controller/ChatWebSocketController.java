package com.trippzo.controller;

import com.trippzo.model.Message;
import com.trippzo.model.User;
import com.trippzo.model.dto.ChatMessage;
import com.trippzo.service.ChatService;
import com.trippzo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

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
