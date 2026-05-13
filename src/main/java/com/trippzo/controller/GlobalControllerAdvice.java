package com.trippzo.controller;

import com.trippzo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final ChatService chatService;

    @ModelAttribute("unreadMessages")
    public int populateUnreadMessages(Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            return chatService.countAllUnreadMessages(username);
        }
        return 0;
    }
}
