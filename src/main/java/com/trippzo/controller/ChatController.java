package com.trippzo.controller;

import com.trippzo.model.Message;
import com.trippzo.model.User;
import com.trippzo.model.dto.ChatPartnerDTO;
import com.trippzo.service.ChatService;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @Autowired
    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping
    public String showInbox(Model model, Principal principal) {
        String currentUsername = principal.getName();

        Set<User> chatPartners = chatService.findChatPartners(currentUsername);

        List<ChatPartnerDTO> partnerDtos = chatPartners.stream().map(partner -> {
            int unreadCount = chatService.countUnreadMessages(currentUsername, partner.getUsername());

            String lastMessageTime = chatService.getLastMessageTime(currentUsername, partner.getUsername());
            String lastMessage = chatService.getLastMessageContent(currentUsername, partner.getUsername());

            ChatPartnerDTO dto = new ChatPartnerDTO(partner, unreadCount);
            dto.setLastMessageTime(lastMessageTime);
            dto.setLastMessage(lastMessage);

            dto.setAvatarUrl(partner.getAvatarUrl());

            return dto;
        }).toList();

        model.addAttribute("chatPartners", partnerDtos);
        return "chat-inbox";
    }

    @GetMapping("/{username}")
    public String showChat(@PathVariable String username, Model model, Principal principal) {
        String currentUsername = principal.getName();

        chatService.markMessagesAsRead(username, currentUsername);

        List<Message> messages = chatService.getChatBetween(currentUsername, username);
        User chatPartner = userService.findByUsername(username);

        model.addAttribute("messages", messages);
        model.addAttribute("chatPartner", chatPartner);

        return "chat-window";
    }

    @PostMapping("/{username}/send")
    public String sendMessage(@PathVariable String username, @RequestParam("message") String message,
            Principal principal) {
        String senderUsername = principal.getName();

        chatService.saveMessage(null, senderUsername, message, username);

        return "redirect:/chat/" + username;
    }

}
