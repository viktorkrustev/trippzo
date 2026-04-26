package com.trippzo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Transient
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 20)
    private String role = "USER";

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 5)
    private String languagePref = "bg";

    @Column(length = 255)
    private String avatarUrl;
}
