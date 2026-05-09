package com.trippzo.service;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.model.User;
import com.trippzo.model.dto.UserRegisterDTO;
import com.trippzo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;

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
        String trimmedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(trimmedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Потребител с този имейл не е намерен"));

        return new CustomUserDetails(user);
    }

    @Transactional
    public void registerUser(UserRegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Потребителското име вече е заето!");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Имейлът вече е регистриран!");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setUsername(dto.getUsername().toLowerCase().trim());
        user.setEmail(dto.getEmail().toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);
    }

    public User findByUsername(String username) {

        String normalizedUsername = username.trim().toLowerCase();

        return userRepository.findByUsername(normalizedUsername).orElse(null);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        if (username == null)
            return false;
        return userRepository.existsByUsername(username.toLowerCase().trim());
    }

    public boolean existsByEmail(String email) {
        if (email == null)
            return false;
        return userRepository.existsByEmail(email.toLowerCase().trim());
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getAuthenticatedUserFromPrincipal(Principal principal) {
        if (principal == null)
            return null;

        String name = principal.getName();

        User user = userRepository.findByUsername(name).orElse(null);
        if (user != null)
            return user;

        user = userRepository.findByEmail(name).orElse(null);
        if (user != null)
            return user;

        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            String email = oauthToken.getPrincipal().getAttribute("email");

            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
        }

        return null;
    }
}
