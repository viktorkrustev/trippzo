package com.trippzo.service;


import com.trippzo.model.User;
import com.trippzo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = encoder;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Имейлът вече е зает");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Потребителското име вече съществува");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }



    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }


    // Намира потребител по username
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

}
