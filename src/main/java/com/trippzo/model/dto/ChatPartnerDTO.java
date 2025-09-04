package com.trippzo.model.dto;

import com.trippzo.model.User;

public class ChatPartnerDTO {
    private User user;
    private int unreadMessagesCount;

    // Допълнителни полета за показване в чата
    private String lastMessageTime;
    private String lastMessage;
    private String tripRoute;
    private String avatarUrl;


    public ChatPartnerDTO(User user, int unreadMessagesCount) {
        this.user = user;
        this.unreadMessagesCount = unreadMessagesCount;
    }

    // --- Getters и Setters ---

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getTripRoute() {
        return tripRoute;
    }

    public void setTripRoute(String tripRoute) {
        this.tripRoute = tripRoute;
    }

    // Помощен метод за лесен достъп до username в Thymeleaf
    public String getUsername() {
        return user != null ? user.getUsername() : "";
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
