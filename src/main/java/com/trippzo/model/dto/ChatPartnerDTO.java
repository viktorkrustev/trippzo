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

    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private int unreadMessagesCount;
    private String lastMessageTime;
    private String lastMessage;
    private LocalDateTime rawTimestamp;

    public ChatPartnerDTO(User user, int unreadMessagesCount) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.avatarUrl = user.getAvatarUrl();
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public String getDisplayName() {
        return (fullName != null && !fullName.isEmpty()) ? fullName : username;
    }
}
