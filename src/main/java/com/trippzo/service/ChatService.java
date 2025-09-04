package com.trippzo.service;

import com.trippzo.model.Message;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.repository.MessageRepository;
import com.trippzo.repository.TripRepository;
import com.trippzo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ChatService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    // Връща всички съобщения за дадено пътуване, сортирани по време
    public List<Message> getMessagesForTrip(Long tripId) {
        return messageRepository.findByTripIdOrderByTimestampAsc(tripId);
    }

    // Записва ново съобщение (за пътуване или директно)
    public Message saveMessage(Long tripId, String senderUsername, String content, String receiverUsername) {
        Trip trip = null;
        if (tripId != null) {
            trip = tripRepository.findById(tripId).orElse(null);
        }

        Optional<User> senderOpt = userRepository.findByUsername(senderUsername);
        Optional<User> receiverOpt = userRepository.findByUsername(receiverUsername);

        if (senderOpt.isEmpty() || receiverOpt.isEmpty()) {
            return null;
        }

        User sender = senderOpt.get();
        User receiver = receiverOpt.get();

        Message msg = new Message();
        msg.setTrip(trip);
        msg.setSender(sender);
        msg.setReceiver(receiver);
        msg.setMessageText(content);
        msg.setTimestamp(java.time.LocalDateTime.now());
        msg.setRead(false);  // маркираме новото съобщение като непрочетено

        return messageRepository.save(msg);
    }

    // Намира всички чат партньори на потребител (като изпращач и получател)
    public Set<User> findChatPartners(String username) {
        Set<User> partners = new HashSet<>();
        partners.addAll(messageRepository.findReceiversBySender(username));
        partners.addAll(messageRepository.findSendersByReceiver(username));
        return partners;
    }

    // Връща списък със съобщения между два потребителя (цял чат)
    public List<Message> getChatBetween(String userA, String userB) {
        return messageRepository.findChatBetweenUsers(userA, userB);
    }

    // Брои непрочетените съобщения от партньор към текущия потребител
    public int countUnreadMessages(String currentUsername, String partnerUsername) {
        return messageRepository.countBySenderUsernameAndReceiverUsernameAndReadFalse(
                partnerUsername, currentUsername);
    }

    // Връща време на последното съобщение между двама потребители, форматирано като "HH:mm"
    public String getLastMessageTime(String userA, String userB) {
        List<Message> messages = getChatBetween(userA, userB);
        if (messages.isEmpty()) {
            return "";
        }
        // Взимаме последното съобщение
        Message last = messages.get(messages.size() - 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return last.getTimestamp().format(formatter);
    }

    // Връща текста на последното съобщение между двама потребители
    public String getLastMessageContent(String userA, String userB) {
        List<Message> messages = getChatBetween(userA, userB);
        if (messages.isEmpty()) {
            return "";
        }
        return messages.get(messages.size() - 1).getMessageText();
    }



    public void markMessagesAsRead(String senderUsername, String receiverUsername) {
        List<Message> unreadMessages = messageRepository.findBySenderUsernameAndReceiverUsernameAndReadFalse(
                senderUsername, receiverUsername);

        for (Message msg : unreadMessages) {
            msg.setRead(true);
        }

        messageRepository.saveAll(unreadMessages);
    }

}
