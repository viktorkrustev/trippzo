package com.trippzo.model.enums;

import lombok.Getter;

@Getter
public enum NotificationStatus {
    PENDING("Изчакващо"),
    ACCEPTED("Прието"),
    REJECTED("Отхвърлено"),
    READ("Прочетено");

    private final String displayName;

    NotificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isAccepted() {
        return this == ACCEPTED;
    }

    public boolean isRejected() {
        return this == REJECTED;
    }

    public boolean isRead() {
        return this == READ;
    }
}

