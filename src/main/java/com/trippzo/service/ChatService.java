package com.trippzo.service;

import com.trippzo.model.Message;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.dto.ChatPartnerDTO;
import com.trippzo.model.dto.MessageDTO;
import com.trippzo.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final TripService tripService;
    private final UserService userService;

    public List<MessageDTO> getMessagesForTrip(Long tripId) {
        return messageRepository.findByTripIdOrderByTimestampAsc(tripId).stream().map(this::toDTO).toList();
    }

    public Message saveMessage(Long tripId, String senderIdentifier, String content, String receiverUsername) {
        Trip trip = null;
        if (tripId != null) {
            trip = tripService.getTripById(tripId);
        }

        User sender = resolveUser(senderIdentifier);
        User receiver = resolveUser(receiverUsername);

        if (sender == null) {
            throw new IllegalArgumentException("Sender not found: " + senderIdentifier);
        }
        if (receiver == null) {
            throw new IllegalArgumentException("Receiver not found: " + receiverUsername);
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

    public Set<User> findChatPartners(String identifier) {
        User user = resolveUser(identifier);
        if (user == null)
            return Collections.emptySet();

        String username = user.getUsername();
        Set<User> partners = new HashSet<>();
        partners.addAll(messageRepository.findReceiversBySender(username));
        partners.addAll(messageRepository.findSendersByReceiver(username));
        return partners;
    }

    public List<MessageDTO> getChatBetween(String userA, String userB) {
        return messageRepository.findChatBetweenUsers(userA, userB).stream().map(this::toDTO).toList();
    }

    public int countUnreadMessages(String currentUsername, String partnerUsername) {
        return messageRepository.countBySenderUsernameAndReceiverUsernameAndReadFalse(partnerUsername, currentUsername);
    }

    public int countAllUnreadMessages(String currentUsername) {
        return messageRepository.countByReceiverUsernameAndReadFalse(currentUsername);
    }

    public void markMessagesAsRead(String senderUsername, String receiverUsername) {
        List<Message> unread = messageRepository.findBySenderUsernameAndReceiverUsernameAndReadFalse(senderUsername,
                receiverUsername);
        unread.forEach(msg -> msg.setRead(true));
        messageRepository.saveAll(unread);
    }

    public List<ChatPartnerDTO> getSortedChatPartners(String currentUsername, Locale locale) {
        Set<User> partners = findChatPartners(currentUsername);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm", locale);

        return partners.stream().map(partner -> {
            int unreadCount = countUnreadMessages(currentUsername, partner.getUsername());
            ChatPartnerDTO dto = new ChatPartnerDTO(partner, unreadCount);

            List<Message> chat = messageRepository.findChatBetweenUsers(currentUsername, partner.getUsername());
            if (!chat.isEmpty()) {
                Message last = chat.getLast();
                dto.setLastMessage(last.getMessageText());
                dto.setRawTimestamp(last.getTimestamp());
                dto.setLastMessageTime(last.getTimestamp().format(formatter));
            }

            return dto;
        }).filter(dto -> dto.getRawTimestamp() != null)
                .sorted(Comparator.comparing(ChatPartnerDTO::getRawTimestamp).reversed()).toList();
    }

    private User resolveUser(String identifier) {
        if (identifier == null)
            return null;
        User user = userService.findByUsername(identifier);
        if (user == null) {
            user = userService.findByEmail(identifier);
        }
        return user;
    }

    public MessageDTO toDTO(Message msg) {
        User sender = msg.getSender();
        return new MessageDTO(msg.getId(), sender.getUsername(), sender.getFullName(), sender.getAvatarUrl(),
                msg.getMessageText(), msg.getTimestamp(), msg.isRead());
    }
}
