package com.trippzo.service;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.exception.PasswordMismatchException;
import com.trippzo.exception.UserAlreadyExistsException;
import com.trippzo.model.User;
import com.trippzo.model.dto.UserRegisterDTO;
import com.trippzo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Потребител с този имейл не е намерен"));
        return new CustomUserDetails(user);
    }

    @Transactional
    public void registerUser(UserRegisterDTO dto) {

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        if (userRepository.existsByUsername(dto.getUsername().toLowerCase().trim())) {
            throw new UserAlreadyExistsException("username", "Потребителското име вече е заето!");
        }
        if (userRepository.existsByEmail(dto.getEmail().toLowerCase().trim())) {
            throw new UserAlreadyExistsException("email", "Имейлът вече е регистриран!");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername().toLowerCase().trim());
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username.trim().toLowerCase()).orElse(null);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
    }

    @Transactional
    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        return userRepository.existsByUsername(username.toLowerCase().trim());
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    public User getAuthenticatedUserFromPrincipal(Principal principal) {
        if (principal == null) return null;

        String name = principal.getName();

        User user = userRepository.findByUsername(name).orElse(null);
        if (user != null) return user;

        user = userRepository.findByEmail(name).orElse(null);
        if (user != null) return user;

        if (principal instanceof OAuth2AuthenticationToken oauthToken) {
            String email = oauthToken.getPrincipal().getAttribute("email");
            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
        }

        return null;
    }

    @Transactional
    public void findOrCreateOAuth2User(String email, String fullName, String avatarUrl) {
        userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(fullName);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setUsername(email.split("@")[0] + "_"
                    + UUID.randomUUID().toString().substring(0, 4));
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            return userRepository.save(newUser);
        });
    }
}