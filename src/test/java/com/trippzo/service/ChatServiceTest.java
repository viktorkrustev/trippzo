package com.trippzo.service;

import com.trippzo.model.Message;
import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.model.dto.MessageDTO;
import com.trippzo.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private TripService tripService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChatService chatService;

    private User sender;
    private User receiver;
    private Trip testTrip;
    private Message testMessage;

    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setEmail("sender@example.com");
        sender.setFullName("Sender User");
        sender.setAvatarUrl("avatar1.jpg");

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");
        receiver.setEmail("receiver@example.com");
        receiver.setFullName("Receiver User");
        receiver.setAvatarUrl("avatar2.jpg");

        testTrip = new Trip();
        testTrip.setId(1L);
        testTrip.setOrigin("Sofia");
        testTrip.setDestination("Plovdiv");
        testTrip.setSeatsTotal(4);
        testTrip.setPricePerSeat(BigDecimal.valueOf(15.00));

        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setTrip(testTrip);
        testMessage.setSender(sender);
        testMessage.setReceiver(receiver);
        testMessage.setMessageText("Hello!");
        testMessage.setTimestamp(LocalDateTime.now());
        testMessage.setRead(false);
    }

    @Test
    void testGetMessagesForTrip() {
        List<Message> messages = List.of(testMessage);
        when(messageRepository.findByTripIdOrderByTimestampAsc(1L)).thenReturn(messages);

        List<MessageDTO> result = chatService.getMessagesForTrip(1L);

        assertEquals(1, result.size());
        assertEquals("Hello!", result.getFirst().text());
    }

    @Test
    void testGetMessagesForTripEmpty() {
        when(messageRepository.findByTripIdOrderByTimestampAsc(1L)).thenReturn(Collections.emptyList());

        List<MessageDTO> result = chatService.getMessagesForTrip(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void testSaveMessage_Success() {
        when(tripService.getTripById(1L)).thenReturn(testTrip);
        when(userService.findByUsername("sender")).thenReturn(sender);
        when(userService.findByEmail("receiver")).thenReturn(receiver);
        when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

        Message result = chatService.saveMessage(1L, "sender", "Hello!", "receiver");

        assertNotNull(result);
        assertEquals("Hello!", result.getMessageText());
        verify(messageRepository, times(1)).save(any(Message.class));
    }

    @Test
    void testSaveMessage_WithoutTrip() {
        Message messageWithoutTrip = new Message();
        messageWithoutTrip.setId(1L);
        messageWithoutTrip.setTrip(null);
        messageWithoutTrip.setSender(sender);
        messageWithoutTrip.setReceiver(receiver);
        messageWithoutTrip.setMessageText("Hello!");
        messageWithoutTrip.setTimestamp(LocalDateTime.now());
        messageWithoutTrip.setRead(false);

        when(userService.findByUsername("sender")).thenReturn(sender);
        when(userService.findByUsername("receiver")).thenReturn(receiver);
        when(messageRepository.save(any(Message.class))).thenReturn(messageWithoutTrip);

        Message result = chatService.saveMessage(null, "sender", "Hello!", "receiver");

        assertNotNull(result);
        assertNull(result.getTrip());
    }

    @Test
    void testSaveMessage_SenderNotFound() {
        when(tripService.getTripById(1L)).thenReturn(testTrip);
        when(userService.findByUsername("unknown")).thenReturn(null);
        when(userService.findByEmail("unknown")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            chatService.saveMessage(1L, "unknown", "Hello!", "receiver")
        );
    }

    @Test
    void testSaveMessage_ReceiverNotFound() {
        when(tripService.getTripById(1L)).thenReturn(testTrip);
        when(userService.findByUsername("sender")).thenReturn(sender);
        when(userService.findByUsername("unknown")).thenReturn(null);
        when(userService.findByEmail("unknown")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () ->
            chatService.saveMessage(1L, "sender", "Hello!", "unknown")
        );
    }

    @Test
    void testFindChatPartners() {
        when(userService.findByUsername("sender")).thenReturn(sender);
        when(messageRepository.findReceiversBySender("sender")).thenReturn(Set.of(receiver));
        when(messageRepository.findSendersByReceiver("sender")).thenReturn(Collections.emptySet());

        Set<User> result = chatService.findChatPartners("sender");

        assertEquals(1, result.size());
        assertTrue(result.contains(receiver));
    }

    @Test
    void testFindChatPartnersEmpty() {
        when(userService.findByUsername("sender")).thenReturn(sender);
        when(messageRepository.findReceiversBySender("sender")).thenReturn(Collections.emptySet());
        when(messageRepository.findSendersByReceiver("sender")).thenReturn(Collections.emptySet());

        Set<User> result = chatService.findChatPartners("sender");

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindChatPartners_UserNotFound() {
        when(userService.findByUsername("unknown")).thenReturn(null);

        Set<User> result = chatService.findChatPartners("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetChatBetween() {
        List<Message> messages = List.of(testMessage);
        when(messageRepository.findChatBetweenUsers("sender", "receiver")).thenReturn(messages);

        List<MessageDTO> result = chatService.getChatBetween("sender", "receiver");

        assertEquals(1, result.size());
    }

    @Test
    void testGetChatBetweenEmpty() {
        when(messageRepository.findChatBetweenUsers("sender", "receiver")).thenReturn(Collections.emptyList());

        List<MessageDTO> result = chatService.getChatBetween("sender", "receiver");

        assertTrue(result.isEmpty());
    }

    @Test
    void testCountUnreadMessages() {
        when(messageRepository.countBySenderUsernameAndReceiverUsernameAndReadFalse("sender", "receiver"))
            .thenReturn(3);

        int count = chatService.countUnreadMessages("receiver", "sender");

        assertEquals(3, count);
    }

    @Test
    void testCountUnreadMessagesZero() {
        when(messageRepository.countBySenderUsernameAndReceiverUsernameAndReadFalse("sender", "receiver"))
            .thenReturn(0);

        int count = chatService.countUnreadMessages("receiver", "sender");

        assertEquals(0, count);
    }

    @Test
    void testCountAllUnreadMessages() {
        when(messageRepository.countByReceiverUsernameAndReadFalse("receiver"))
            .thenReturn(5);

        int count = chatService.countAllUnreadMessages("receiver");

        assertEquals(5, count);
    }

    @Test
    void testMarkMessagesAsRead() {
        Message msg1 = new Message();
        msg1.setId(1L);
        msg1.setRead(false);
        Message msg2 = new Message();
        msg2.setId(2L);
        msg2.setRead(false);

        when(messageRepository.findBySenderUsernameAndReceiverUsernameAndReadFalse("sender", "receiver"))
                .thenReturn(List.of(msg1, msg2));

        chatService.markMessagesAsRead("sender", "receiver");

        assertTrue(msg1.isRead());
        assertTrue(msg2.isRead());
        verify(messageRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testMarkMessagesAsReadNoMessages() {
        when(messageRepository.findBySenderUsernameAndReceiverUsernameAndReadFalse("sender", "receiver"))
            .thenReturn(Collections.emptyList());

        chatService.markMessagesAsRead("sender", "receiver");

        verify(messageRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testToDTO() {
        MessageDTO dto = chatService.toDTO(testMessage);

        assertEquals("sender", dto.senderUsername());
        assertEquals("Sender User", dto.senderFullName());
        assertEquals("avatar1.jpg", dto.senderAvatar());
        assertEquals("Hello!", dto.text());
        assertFalse(dto.read());
    }
}
