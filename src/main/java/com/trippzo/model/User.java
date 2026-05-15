package com.trippzo.model;

import com.trippzo.model.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String fullName;
    private String avatarUrl;
    private int driverLevel = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
