package com.trippzo.controller;

import com.trippzo.model.User;
import com.trippzo.model.dto.ChatPartnerDTO;
import com.trippzo.service.ChatService;
import com.trippzo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/chat")
public class ChatController extends BaseController {

    private final ChatService chatService;

    public ChatController(UserService userService, ChatService chatService) {
        super(userService);
        this.chatService = chatService;
    }

    @GetMapping
    public String showInbox(Model model, @AuthenticationPrincipal Object principal, Locale locale) {
        User currentUser = resolveUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }

        List<ChatPartnerDTO> partnerDtos = chatService.getSortedChatPartners(currentUser.getUsername(), locale);
        model.addAttribute("chatPartners", partnerDtos);
        model.addAttribute("currentUsername", currentUser.getUsername());
        return "chat-inbox";
    }

    @GetMapping("/{username}")
    public String showChat(@PathVariable String username, Model model, @AuthenticationPrincipal Object principal) {
        User currentUser = resolveUser(principal);
        if (currentUser == null) {
            return "redirect:/login";
        }

        String currentUsername = currentUser.getUsername();
        chatService.markMessagesAsRead(username, currentUsername);

        User chatPartner = userService.findByUsername(username);
        if (chatPartner == null) {
            return "redirect:/chat";
        }

        model.addAttribute("messages", chatService.getChatBetween(currentUsername, username));
        model.addAttribute("chatPartnerUsername", chatPartner.getUsername());
        model.addAttribute("chatPartnerName",
                chatPartner.getFullName() != null ? chatPartner.getFullName() : chatPartner.getUsername());
        model.addAttribute("chatPartnerAvatar", chatPartner.getAvatarUrl());
        model.addAttribute("currentUsername", currentUsername);
        return "chat-window";
    }

    @GetMapping("/unread/count")
    @ResponseBody
    public int getUnreadCount(@AuthenticationPrincipal Object principal) {
        User user = resolveUser(principal);
        if (user == null) {
            return 0;
        }
        return chatService.countAllUnreadMessages(user.getUsername());
    }
}
