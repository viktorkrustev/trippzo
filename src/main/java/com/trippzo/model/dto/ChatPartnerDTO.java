package com.trippzo.model.dto;

import com.trippzo.model.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatPartnerDTO {
    private User user;
    private int unreadMessagesCount;

    private String lastMessageTime;
    private String lastMessage;
    private String tripRoute;
    private String avatarUrl;

    public ChatPartnerDTO(User user, int unreadMessagesCount) {
        this.user = user;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public String getUsername() {
        return user.getUsername();
    }
}
