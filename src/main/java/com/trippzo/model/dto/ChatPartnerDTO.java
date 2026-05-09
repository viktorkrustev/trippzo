package com.trippzo.model.dto;

import com.trippzo.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatPartnerDTO {
    private User user;
    private int unreadMessagesCount;
    private String lastMessageTime;
    private String lastMessage;
    private String avatarUrl;
    private LocalDateTime rawTimestamp;

    public ChatPartnerDTO(User user, int unreadMessagesCount) {
        this.user = user;
        this.unreadMessagesCount = unreadMessagesCount;
        this.avatarUrl = user.getAvatarUrl();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public String getDisplayName() {
        return (user.getFullName() != null && !user.getFullName().isEmpty()) ? user.getFullName() : user.getUsername();
    }
}
