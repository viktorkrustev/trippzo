package com.trippzo.model.enums;

public enum Role {
    ROLE_USER("User"), ROLE_ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
