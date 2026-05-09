package com.trippzo.service;

import com.trippzo.model.Message;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.dto.ChatPartnerDTO;
import com.trippzo.repository.MessageRepository;
import com.trippzo.repository.TripRepository;
import com.trippzo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Message> getMessagesForTrip(Long tripId) {
        return messageRepository.findByTripIdOrderByTimestampAsc(tripId);
    }

    public Message saveMessage(Long tripId, String senderIdentifier, String content, String receiverUsername) {
        Trip trip = null;
        if (tripId != null) {
            trip = tripRepository.findById(tripId).orElse(null);
        }

        User sender = userRepository.findByUsername(senderIdentifier)
                .orElseGet(() -> userRepository.findByEmail(senderIdentifier).orElse(null));

        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseGet(() -> userRepository.findByEmail(receiverUsername).orElse(null));

        if (sender == null || receiver == null) {
            return null;
        }

        Message msg = new Message();
        msg.setTrip(trip);
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setMessageText(content);
        msg.setTimestamp(LocalDateTime.now());
        msg.setRead(false);

        return messageRepository.save(msg);
    }

    public Set<User> findChatPartners(String username) {
        Set<User> partners = new HashSet<>();
        partners.addAll(messageRepository.findReceiversBySender(username));
        partners.addAll(messageRepository.findSendersByReceiver(username));
        return partners;
    }

    public List<Message> getChatBetween(String userA, String userB) {
        return messageRepository.findChatBetweenUsers(userA, userB);
    }

    public int countUnreadMessages(String currentUsername, String partnerUsername) {
        return messageRepository.countBySenderUsernameAndReceiverUsernameAndReadFalse(partnerUsername, currentUsername);
    }

    public int countAllUnreadMessages(String currentUsername) {
        return messageRepository.countByReceiverUsernameAndReadFalse(currentUsername);
    }

    public String getLastMessageTime(String userA, String userB) {
        List<Message> messages = getChatBetween(userA, userB);
        if (messages.isEmpty()) {
            return "";
        }
        Message last = messages.get(messages.size() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm");
        return last.getTimestamp().format(formatter);
    }

    public String getLastMessageContent(String userA, String userB) {
        List<Message> messages = getChatBetween(userA, userB);
        if (messages.isEmpty()) {
            return "";
        }
        return messages.get(messages.size() - 1).getMessageText();
    }

    public void markMessagesAsRead(String senderUsername, String receiverUsername) {
        List<Message> unreadMessages = messageRepository
                .findBySenderUsernameAndReceiverUsernameAndReadFalse(senderUsername, receiverUsername);

        for (Message msg : unreadMessages) {
            msg.setRead(true);
        }

        messageRepository.saveAll(unreadMessages);
    }

    public List<ChatPartnerDTO> getSortedChatPartners(String currentUsername, Locale locale) {
        Set<User> partners = findChatPartners(currentUsername);

        return partners.stream().map(partner -> {
            int unreadCount = countUnreadMessages(currentUsername, partner.getUsername());
            ChatPartnerDTO dto = new ChatPartnerDTO(partner, unreadCount);

            List<Message> chat = getChatBetween(currentUsername, partner.getUsername());
            if (!chat.isEmpty()) {
                Message last = chat.get(chat.size() - 1);
                dto.setLastMessage(last.getMessageText());
                dto.setRawTimestamp(last.getTimestamp());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", locale);
                dto.setLastMessageTime(last.getTimestamp().format(formatter));
            }
            return dto;
        }).filter(dto -> dto.getRawTimestamp() != null)
                .sorted(Comparator.comparing(ChatPartnerDTO::getRawTimestamp).reversed()).collect(Collectors.toList());
    }

}
