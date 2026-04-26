package com.trippzo.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {

    private Long tripId;
    private String senderUsername;
    private String content;
    private String to;
    private String receiverUsername;

}
