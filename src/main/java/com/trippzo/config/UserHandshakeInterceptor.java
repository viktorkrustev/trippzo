package com.trippzo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class UserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("WebSocket handshake отхвърлен: заявката не е от тип ServletServerHttpRequest");
            return false;
        }

        try {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();

            HttpSession session = httpRequest.getSession(false);
            if (session == null) {
                log.debug("WebSocket handshake отхвърлен: няма активна сесия");
                return false;
            }

            SecurityContext context = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
            if (context == null) {
                log.debug("WebSocket handshake отхвърлен: няма SecurityContext в сесията");
                return false;
            }

            Authentication auth = context.getAuthentication();

            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                log.debug("WebSocket handshake отхвърлен: потребителят не е автентициран ({})",
                        auth != null ? auth.getClass().getSimpleName() : "null");
                return false;
            }

            if (auth.getPrincipal() instanceof OAuth2User oauth2User) {
                String email = oauth2User.getAttribute("email");
                attributes.put("user_email", email);
            }

            attributes.put("user", auth);
            log.debug("WebSocket handshake одобрен за: {}", auth.getName());
            return true;

        } catch (Exception e) {
            log.error("WebSocket handshake отхвърлен заради грешка: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception ex) {
        if (ex != null) {
            log.error("Грешка след WebSocket handshake: {}", ex.getMessage(), ex);
        }
    }
}
