package com.trippzo.model.dto;

import java.time.LocalDateTime;

public record MessageDTO(Long id, String senderUsername, String senderFullName, String senderAvatar, String text,
        LocalDateTime timestamp, boolean read) {
    public String getDisplayName() {
        return (senderFullName != null && !senderFullName.isEmpty()) ? senderFullName : senderUsername;
    }
}
