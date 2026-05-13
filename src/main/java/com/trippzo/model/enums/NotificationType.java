package com.trippzo.model.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    SEAT_REQUEST("Заявка за място"), SEAT_ACCEPTED("Мястото е потвърдено"), SEAT_REJECTED("Заявката е отхвърлена");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

}
