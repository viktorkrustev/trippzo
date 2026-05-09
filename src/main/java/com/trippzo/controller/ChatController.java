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

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/chat")
public class ChatController extends BaseController {

    private final ChatService chatService;
    private final UserService userService;

    @Autowired
    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    @GetMapping
    public String showInbox(Model model, @AuthenticationPrincipal Object principal, Locale locale) {
        User currentUser = resolveUser(principal);
        if (currentUser == null)
            return "redirect:/login";

        String currentUsername = currentUser.getUsername();

        List<ChatPartnerDTO> partnerDtos = chatService.getSortedChatPartners(currentUsername, locale);

        model.addAttribute("chatPartners", partnerDtos);
        model.addAttribute("currentUsername", currentUsername);
        return "chat-inbox";
    }

    @GetMapping("/{username}")
    public String showChat(@PathVariable String username, Model model, @AuthenticationPrincipal Object principal) {
        User currentUser = resolveUser(principal);
        if (currentUser == null)
            return "redirect:/login";

        chatService.markMessagesAsRead(username, currentUser.getUsername());

        List<Message> messages = chatService.getChatBetween(currentUser.getUsername(), username);
        User chatPartner = userService.findByUsername(username);

        model.addAttribute("messages", messages);
        model.addAttribute("chatPartner", chatPartner);
        model.addAttribute("currentUsername", currentUser.getUsername()); // Подаваме реалния username
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
