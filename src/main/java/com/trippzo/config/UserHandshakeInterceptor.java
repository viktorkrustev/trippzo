package com.trippzo.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

                SecurityContext context = (SecurityContext) httpServletRequest.getSession(false)
                        .getAttribute("SPRING_SECURITY_CONTEXT");
                if (context != null) {
                    Authentication auth = context.getAuthentication();
                    if (auth != null && auth.isAuthenticated()) {
                        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
                            String email = oauth2User.getAttribute("email");
                            attributes.put("user_email", email);
                        }
                        attributes.put("user", auth);
                        return true;
                    }
                }

                return true;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception ex) {
    }
}
