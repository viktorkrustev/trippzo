package com.trippzo.controller;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.User;
import com.trippzo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;

public abstract class BaseController {

    @Autowired
    protected UserService userService;

    protected User resolveUser(Object principal) {
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        } else if (principal instanceof OAuth2User) {
            String email = ((OAuth2User) principal).getAttribute("email");
            return userService.findByEmail(email);
        }
        return null;
    }

    protected User extractUserFromCustomDetails(CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser() : null;
    }
}
