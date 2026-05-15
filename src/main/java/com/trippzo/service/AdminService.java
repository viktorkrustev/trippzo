package com.trippzo.service;

import com.trippzo.model.Trip;
import com.trippzo.model.User;
import com.trippzo.repository.ReviewRepository;
import com.trippzo.repository.TripRepository;
import com.trippzo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }

    public Optional<Trip> getTripById(Long tripId) {
        return tripRepository.findById(tripId);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.deleteById(userId);
    }

    @Transactional
    public void deleteTrip(Long tripId) {
        Trip trip = tripRepository.findById(tripId).orElseThrow(() -> new IllegalArgumentException("Trip not found"));
        tripRepository.deleteById(tripId);
    }

    @Transactional
    public void promoteUserToAdmin(Long userId) {
        userService.promoteToAdmin(userId);
    }

    @Transactional
    public void demoteUserToNormal(Long userId) {
        userService.demoteToUser(userId);
    }

    public AdminStats getAdminStats() {
        long totalUsers = userRepository.count();
        long totalTrips = tripRepository.count();
        long totalReviews = reviewRepository.count();

        return new AdminStats(totalUsers, totalTrips, totalReviews);
    }

    public record AdminStats(long totalUsers, long totalTrips, long totalReviews) {
    }
}
