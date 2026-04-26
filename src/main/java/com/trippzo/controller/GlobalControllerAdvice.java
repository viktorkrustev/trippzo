package com.trippzo.controller;

import com.trippzo.model.User;
import com.trippzo.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final ChatService chatService;

    @Autowired
    public GlobalControllerAdvice(ChatService chatService) {
        this.chatService = chatService;
    }

    @ModelAttribute("unreadMessages")
    public int populateUnreadMessages(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            return chatService.countAllUnreadMessages(username);
        }
        return 0;
    }
}
