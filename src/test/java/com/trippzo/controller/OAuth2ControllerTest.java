package com.trippzo.controller;

import com.trippzo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.mockito.Mockito.*;

/**
 * Unit тестове за OAuth2Controller
 * 
 * Демонстрира как DIP прави контролера лесен за тестване.
 * Тестваме САМО контролера, без да зависим от UserRepository имплементация.
 */
@DisplayName("OAuth2Controller Tests - DIP спазено")
class OAuth2ControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private OAuth2Controller oAuth2Controller;

    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Мокираме OAuth2User
        oAuth2User = mock(OAuth2User.class);
    }

    @Test
    @DisplayName("success() викне userService.findOrCreateOAuth2User с правилни параметри")
    void testSuccess_CallsUserService() {
        // Arrange
        String email = "user@example.com";
        String name = "John Doe";
        String picture = "https://example.com/pic.jpg";

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("picture")).thenReturn(picture);

        // Act
        String result = oAuth2Controller.success(oAuth2User);

        // Assert
        assert result.equals("redirect:/");
        // КЛЮЧЕВО: Проверяваме че е викнат сервизът
        verify(userService, times(1)).findOrCreateOAuth2User(email, name, picture);
    }

    @Test
    @DisplayName("success() всегда редиректи к домашней")
    void testSuccess_AlwaysRedirectsToHome() {
        // Arrange
        when(oAuth2User.getAttribute("email")).thenReturn("test@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        // Act
        String result = oAuth2Controller.success(oAuth2User);

        // Assert
        assert result.equals("redirect:/");
    }

    @Test
    @DisplayName("success() обработва null картинку")
    void testSuccess_HandlesNullPicture() {
        // Arrange
        when(oAuth2User.getAttribute("email")).thenReturn("test@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        // Act
        String result = oAuth2Controller.success(oAuth2User);

        // Assert
        assert result.equals("redirect:/");
        verify(userService).findOrCreateOAuth2User("test@test.com", "Test User", null);
    }
}

