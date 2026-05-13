package com.trippzo.controller;

import com.trippzo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.mockito.Mockito.*;

class OAuth2ControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private OAuth2Controller oAuth2Controller;

    private OAuth2User oAuth2User;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        oAuth2User = mock(OAuth2User.class);
    }

    @Test
    void testSuccess_CallsUserService() {
        String email = "user@example.com";
        String name = "John Doe";
        String picture = "https://example.com/pic.jpg";

        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("picture")).thenReturn(picture);

        String result = oAuth2Controller.success(oAuth2User);

        assert result.equals("redirect:/");
        verify(userService, times(1)).findOrCreateOAuth2User(email, name, picture);
    }

    @Test
    void testSuccess_AlwaysRedirectsToHome() {
        when(oAuth2User.getAttribute("email")).thenReturn("test@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        String result = oAuth2Controller.success(oAuth2User);

        assert result.equals("redirect:/");
    }

    @Test
    void testSuccess_HandlesNullPicture() {
        when(oAuth2User.getAttribute("email")).thenReturn("test@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getAttribute("picture")).thenReturn(null);

        String result = oAuth2Controller.success(oAuth2User);

        assert result.equals("redirect:/");
        verify(userService).findOrCreateOAuth2User("test@test.com", "Test User", null);
    }
}
