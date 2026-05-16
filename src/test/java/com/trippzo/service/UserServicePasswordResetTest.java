package com.trippzo.service;

import com.trippzo.model.User;
import com.trippzo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServicePasswordResetTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
    }

    @Test
    void testInitiatePasswordReset() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.initiatePasswordReset("test@example.com");

        verify(userRepository).save(any(User.class));
        assertNotNull(testUser.getPasswordResetToken());
        assertNotNull(testUser.getPasswordResetTokenExpiry());
    }

    @Test
    void testInitiatePasswordReset_UserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            userService.initiatePasswordReset("unknown@example.com")
        );
    }

    @Test
    void testGetUserByResetToken() {
        String token = "test-token-123";
        testUser.setPasswordResetToken(token);

        when(userRepository.findByPasswordResetToken(token)).thenReturn(Optional.of(testUser));

        User result = userService.getUserByResetToken(token);

        assertNotNull(result);
        assertEquals(token, result.getPasswordResetToken());
    }

    @Test
    void testGetUserByResetToken_InvalidToken() {
        when(userRepository.findByPasswordResetToken("invalid-token")).thenReturn(Optional.empty());

        User result = userService.getUserByResetToken("invalid-token");

        assertNull(result);
    }

    @Test
    void testIsResetTokenValid() {
        testUser.setPasswordResetToken("valid-token");
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        boolean result = userService.isResetTokenValid(testUser);

        assertTrue(result);
    }

    @Test
    void testIsResetTokenValid_Expired() {
        testUser.setPasswordResetToken("expired-token");
        testUser.setPasswordResetTokenExpiry(LocalDateTime.now().minusHours(1));

        boolean result = userService.isResetTokenValid(testUser);

        assertFalse(result);
    }

    @Test
    void testResetPassword() {
        String newPassword = "newPassword123";
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.resetPassword(testUser, newPassword);

        assertEquals("encodedNewPassword", testUser.getPasswordHash());
        assertNull(testUser.getPasswordResetToken());
        assertNull(testUser.getPasswordResetTokenExpiry());
        verify(userRepository).save(testUser);
    }
}
