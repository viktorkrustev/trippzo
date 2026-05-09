package com.trippzo.service;

import com.trippzo.model.Trip;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TripSpecifications {

    public static Specification<Trip> searchTrips(String origin, String destination, String dateString) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(origin)) {
                predicates.add(cb.like(cb.lower(root.get("origin")), "%" + origin.toLowerCase().trim() + "%"));
            }

            if (StringUtils.hasText(destination)) {
                predicates
                        .add(cb.like(cb.lower(root.get("destination")), "%" + destination.toLowerCase().trim() + "%"));
            }

            if (StringUtils.hasText(dateString)) {
                try {
                    LocalDate date = LocalDate.parse(dateString);
                    LocalDateTime startOfDay = date.atStartOfDay();
                    LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

                    predicates.add(cb.between(root.get("departureDateTime"), startOfDay, endOfDay));
                } catch (Exception ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
