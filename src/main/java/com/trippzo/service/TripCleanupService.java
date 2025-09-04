package com.trippzo.service;

import com.trippzo.repository.TripRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TripCleanupService {

    private final TripRepository tripRepository;

    public TripCleanupService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    // Изпълнява се всеки ден в 00:00
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deletePastTrips() {
        LocalDateTime now = LocalDateTime.now();
        tripRepository.deleteByDepartureDateTimeBefore(now);
        System.out.println("Oстарели пътувания изтрити: " + now);
    }
}
