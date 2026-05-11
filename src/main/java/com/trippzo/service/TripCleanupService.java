package com.trippzo.service;

import com.trippzo.repository.TripRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class TripCleanupService {

    private final TripRepository tripRepository;

    public TripCleanupService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    @Transactional
    @Scheduled(cron = "${trip.cleanup.cron:0 * * * * ?}")
    public void deletePastTrips() {
        LocalDateTime cutoff = LocalDateTime.now();
        try {
            int deletedCount = tripRepository.deleteByDepartureDateTimeBefore(cutoff);
            if (deletedCount > 0) {
                log.info("Successfully deleted {} expired trips.", deletedCount);
            }
        } catch (Exception e) {
            log.error("Failed to cleanup trips: {}", e.getMessage());
        }
    }
}
