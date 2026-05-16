package com.trippzo.service;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.exception.PasswordMismatchException;
import com.trippzo.exception.UserAlreadyExistsException;
import com.trippzo.model.User;
import com.trippzo.model.dto.UserRegisterDTO;
import com.trippzo.model.enums.Role;
import com.trippzo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegisterDTO registerDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(Role.ROLE_USER);

        registerDTO = new UserRegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setEmail("new@example.com");
        registerDTO.setFullName("New User");
        registerDTO.setPassword("password123");
        registerDTO.setConfirmPassword("password123");
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var result = userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertInstanceOf(CustomUserDetails.class, result);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testLoadUserByEmailSuccess() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var result = userService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertInstanceOf(CustomUserDetails.class, result);
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
            userService.loadUserByUsername("nonexistent")
        );
    }

    @Test
    void testRegisterUserSuccess() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        userService.registerUser(registerDTO);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserPasswordMismatch() {
        registerDTO.setConfirmPassword("different");

        assertThrows(PasswordMismatchException.class, () -> userService.registerUser(registerDTO));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUserUsernameAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
            userService.registerUser(registerDTO)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegisterUserEmailAlreadyExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () ->
            userService.registerUser(registerDTO)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var result = userService.findByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        var result = userService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testPromoteToAdmin() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.promoteToAdmin(1L);

        assertEquals(Role.ROLE_ADMIN, testUser.getRole());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testDemoteToUser() {
        testUser.setRole(Role.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.demoteToUser(1L);

        assertEquals(Role.ROLE_USER, testUser.getRole());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testGetUserCount() {
        when(userRepository.count()).thenReturn(5L);

        long count = userService.getUserCount();

        assertEquals(5L, count);
    }

    @Test
    void testSaveUser() {
        userService.saveUser(testUser);

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testDeleteUser() {
        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}
