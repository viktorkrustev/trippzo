package com.trippzo.controller;

import com.trippzo.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2Controller {

    private final UserService userService;

    public OAuth2Controller(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/oauth2/success")
    public String success(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");
        String picture = principal.getAttribute("picture");

        userService.findOrCreateOAuth2User(email, name, picture);

        return "redirect:/";
    }
}


