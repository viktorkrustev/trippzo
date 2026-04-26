package com.trippzo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false)
    private String destination;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    @Transient
    private String departureDate;

    @Transient
    private String departureTime;

    @Column(nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime departureDateTime;

    @Column(nullable = false)
    private Integer seatsTotal;

    @Column(length = 100)
    private String car;

    @Column
    private String stops;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TripPassenger> passengers;

}
