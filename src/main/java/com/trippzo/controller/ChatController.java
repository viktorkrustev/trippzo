package com.trippzo.controller;

import com.trippzo.model.Message;
import com.trippzo.model.User;
import com.trippzo.model.dto.ChatPartnerDTO;
import com.trippzo.service.ChatService;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
        }).sorted((a, b) -> {
            // null-safe sort (най-новите първи)
            if (a.getLastMessageTime() == null)
                return 1;
            if (b.getLastMessageTime() == null)
                return -1;

            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
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

    @GetMapping("/unread/count")
    @ResponseBody
    public int getUnreadCount(@AuthenticationPrincipal UserDetails user) {

        if (user == null) {
            return 0;
        }

        return chatService.countAllUnreadMessages(user.getUsername());
    }
}
