package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.User;
import com.trippzo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public abstract class BaseController {

    protected final UserService userService;

    protected User resolveUser(Object principal) {
        if (principal instanceof CustomUserDetails(User user)) {
            return user;
        }
        if (principal instanceof OAuth2User oauth) {
            String email = oauth.getAttribute("email");
            if (email == null) {
                throw new IllegalStateException("OAuth2 user has no email attribute");
            }
            return userService.findByEmail(email);
        }
        return null;
    }
}
