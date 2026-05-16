package com.trippzo.service;

import com.trippzo.config.CustomUserDetails;
import com.trippzo.exception.PasswordMismatchException;
import com.trippzo.exception.UserAlreadyExistsException;
import com.trippzo.model.User;
import com.trippzo.model.dto.UserRegisterDTO;
import com.trippzo.model.enums.Role;
import com.trippzo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
        String normalized = emailOrUsername.trim().toLowerCase();

        User user = userRepository.findByEmail(normalized).or(() -> userRepository.findByUsername(normalized))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Потребител с този имейл или потребителско име не е намерен"));

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
        user.setRole(Role.ROLE_USER);
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

    public User getAuthenticatedUserFromPrincipal(Principal principal) {
        if (principal == null)
            return null;

        String name = principal.getName();

        User user = userRepository.findByUsername(name).or(() -> userRepository.findByEmail(name)).orElse(null);

        if (user != null)
            return user;

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
            newUser.setUsername(email.split("@")[0] + "_" + UUID.randomUUID().toString().substring(0, 4));
            newUser.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole(Role.ROLE_USER);
            return userRepository.save(newUser);
        });
    }

    @Transactional
    public void updateProfile(User user, String fullName, String email) {
        user.setFullName(fullName);
        user.setEmail(email);
        saveUser(user);
    }

    @Transactional
    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.setRole(Role.ROLE_ADMIN);
        userRepository.save(user);
    }

    @Transactional
    public void demoteToUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        user.setRole(Role.ROLE_USER);
        userRepository.save(user);
    }

    public long getUserCount() {
        return userRepository.count();
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public String initiatePasswordReset(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("Потребител с този имейл не е намерен");
        }

        String token = generateResetToken();
        user.setPasswordResetToken(token);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);
        return token;
    }

    public User getUserByResetToken(String token) {
        return userRepository.findByPasswordResetToken(token).orElse(null);
    }

    public boolean isResetTokenValid(User user) {
        return user != null && user.getPasswordResetToken() != null && user.getPasswordResetTokenExpiry() != null
                && LocalDateTime.now().isBefore(user.getPasswordResetTokenExpiry());
    }

    @Transactional
    public void resetPassword(User user, String newPassword) {
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        userRepository.save(user);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
