package com.trippzo.config;

import com.trippzo.model.*;
import com.trippzo.model.enums.NotificationStatus;
import com.trippzo.model.enums.NotificationType;
import com.trippzo.model.enums.Role;
import com.trippzo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final TripPassengerRepository tripPassengerRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    private Random random = new Random();
    private static final String PASSWORD = "123456";

    private static final String[] BULGARIAN_FIRST_NAMES = { "Александър", "Борис", "Виктор", "Георги", "Данаил", "Емил",
            "Филип", "Григорий", "Йордан", "Камен", "Любчо", "Людмил", "Мартин", "Николай", "Орест", "Павел", "Ради",
            "Симеон", "Тими", "Петър", "Теодор", "Халил", "Цветан", "Цветозар", "Явор", "Захари" };

    private static final String[] BULGARIAN_LAST_NAMES = { "Петров", "Иванов", "Сидорова", "Соколов", "Волков",
            "Морозов", "Павлов", "Федоров", "Михайлов", "Орлов", "Попов", "Ефимов", "Кузнецов", "Новиков", "Тихомиров",
            "Ширяев", "Щербаков", "Якушев", "Ямилев", "Зубков", "Новак", "Живков", "Калчев", "Мангалов", "Никифоров",
            "Раев", "Чалиев", "Чолаков" };

    private static final String[] BULGARIAN_CITIES = { "София", "Пловдив", "Варна", "Бургас", "Русе", "Плевен",
            "Благоевград", "Силистра", "Габрово", "Стара Загора", "Ямбол", "Перник", "Ловеч", "Велико Търново",
            "Кюстендил", "Монтана", "Сливен", "Шумен", "Разград", "Сандански" };

    private static final String[] CAR_MODELS = { "Volkswagen Golf", "Mercedes-Benz C-Class", "BMW 3 Series", "Audi A4",
            "Skoda Octavia", "Renault Clio", "Peugeot 308", "Ford Focus", "Hyundai i30", "Mazda3", "Toyota Corolla",
            "Honda Civic", "Seat Leon", "Citroen C5", "Opel Astra", "Nissan", "Kia Sportage", "Dacia Duster",
            "Fiat 500", "Volkswagen Passat" };

    private static final String[] TRIP_DESCRIPTIONS = { "Пътуване в компания с безопасна кола",
            "Спокойна и комфортна кола, музика по време на пътуване", "Супер кола, достъпна цена, добър шофьор",
            "Всички добре дошли", "Тръгваме навреме, моля бъдете точни",
            "Колата е нова и чиста, всички сте добре дошли", "Идеално за бързо пътуване, директно по маршрута",
            "Лична кола, моля бъдете точни", "Удобна за пътници с багаж", "Приветливо пътуване, добра музика" };

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (shouldSeedData()) {
            log.info("Starting data seeding...");
            long startTime = System.currentTimeMillis();

            List<User> users = seedUsers();
            log.info("Created {} users", users.size());

            List<Trip> trips = seedTrips(users);
            log.info("Created {} trips", trips.size());

            seedTripPassengers(trips, users);
            log.info("Added trip passengers");

            seedReviews(trips, users);
            log.info("Created reviews");

            seedNotifications(trips, users);
            log.info("Created notifications");

            seedChatMessages(trips, users);
            log.info("Created chat messages");

            long endTime = System.currentTimeMillis();
            log.info("Data seeding completed in {} ms", endTime - startTime);
        } else {
            log.info("Database already has data, skipping seeding");
        }
    }

    private boolean shouldSeedData() {
        return userRepository.count() <= 1;
    }

    private List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        Set<String> usedEmails = new HashSet<>();
        Set<String> usedUsernames = new HashSet<>();

        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setFullName(generateBulgarianName());
            user.setUsername(generateUniqueUsername(usedUsernames));
            user.setEmail(generateUniqueEmail(usedEmails));
            user.setPasswordHash(passwordEncoder.encode(PASSWORD));
            user.setRole(Role.ROLE_USER);
            user.setDriverLevel(random.nextInt(1, 6));

            users.add(user);
        }

        return userRepository.saveAll(users);
    }

    private List<Trip> seedTrips(List<User> users) {
        List<Trip> trips = new ArrayList<>();
        LocalDateTime baseDateTime = LocalDateTime.now().plus(1, ChronoUnit.DAYS);

        for (int i = 0; i < 5; i++) {
            Trip trip = new Trip();
            trip.setOrigin(getRandomElement(BULGARIAN_CITIES));
            trip.setDestination(getRandomElement(BULGARIAN_CITIES, trip.getOrigin()));
            trip.setDepartureDateTime(baseDateTime.plus(random.nextInt(1, 90), ChronoUnit.DAYS)
                    .plus(random.nextInt(0, 24), ChronoUnit.HOURS).plus(random.nextInt(0, 60), ChronoUnit.MINUTES));
            trip.setSeatsTotal(random.nextInt(2, 6));
            trip.setPricePerSeat(new BigDecimal(random.nextInt(5, 30)));
            trip.setCar(getRandomElement(CAR_MODELS));
            trip.setDescription(getRandomElement(TRIP_DESCRIPTIONS));
            trip.setDriver(getRandomElement(users));
            trip.setActive(true);

            trips.add(trip);
        }

        return tripRepository.saveAll(trips);
    }

    private void seedTripPassengers(List<Trip> trips, List<User> users) {
        for (Trip trip : trips) {
            int passengerCount = random.nextInt(0, 4);
            Set<Long> addedPassengers = new HashSet<>();

            for (int i = 0; i < passengerCount; i++) {
                User passenger = getRandomElement(users);

                if (!passenger.getId().equals(trip.getDriver().getId())
                        && !addedPassengers.contains(passenger.getId())) {

                    TripPassenger tripPassenger = new TripPassenger();
                    tripPassenger.setTrip(trip);
                    tripPassenger.setUser(passenger);
                    tripPassenger.setJoinedAt(LocalDateTime.now().minus(random.nextInt(1, 30), ChronoUnit.DAYS));

                    tripPassengerRepository.save(tripPassenger);
                    addedPassengers.add(passenger.getId());
                }
            }
        }
    }

    private void seedReviews(List<Trip> trips, List<User> users) {
        for (Trip trip : trips) {
            List<TripPassenger> passengers = tripPassengerRepository.findAll().stream()
                    .filter(tp -> tp.getTrip().getId().equals(trip.getId())).toList();

            for (TripPassenger passenger : passengers) {
                if (random.nextDouble() < 0.6) {
                    Review review = new Review();
                    review.setTrip(trip);
                    review.setReviewer(passenger.getUser());
                    review.setReviewee(trip.getDriver());
                    review.setRating(random.nextInt(1, 6));
                    review.setComment(getRandomReviewComment());
                    review.setCreatedAt(LocalDateTime.now().minus(random.nextInt(1, 15), ChronoUnit.DAYS));

                    reviewRepository.save(review);
                }
            }

            for (TripPassenger passenger : passengers) {
                if (random.nextDouble() < 0.3) {
                    Review review = new Review();
                    review.setTrip(trip);
                    review.setReviewer(trip.getDriver());
                    review.setReviewee(passenger.getUser());
                    review.setRating(random.nextInt(1, 6));
                    review.setComment(getRandomReviewComment());
                    review.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(1, 15)));

                    reviewRepository.save(review);
                }
            }
        }
    }

    private void seedNotifications(List<Trip> trips, List<User> users) {
        for (Trip trip : trips) {
            List<TripPassenger> passengers = tripPassengerRepository.findAll().stream()
                    .filter(tp -> tp.getTrip().getId().equals(trip.getId())).toList();

            if (!passengers.isEmpty()) {
                for (TripPassenger passenger : passengers) {
                    if (random.nextDouble() < 0.5) {
                        Notification notification = new Notification();
                        notification.setRecipient(trip.getDriver());
                        notification.setSender(passenger.getUser());
                        notification.setTrip(trip);
                        notification.setType(NotificationType.SEAT_REQUEST);
                        notification.setMessage(passenger.getUser().getFullName() + " wants to join your trip");
                        notification.setStatus(getRandomElement(NotificationStatus.values()));
                        notification.setCreatedAt(LocalDateTime.now().minus(random.nextInt(1, 30), ChronoUnit.DAYS));

                        notificationRepository.save(notification);
                    }
                }
            }
        }
    }

    private void seedChatMessages(List<Trip> trips, List<User> users) {
        for (Trip trip : trips) {
            List<TripPassenger> passengers = tripPassengerRepository.findAll().stream()
                    .filter(tp -> tp.getTrip().getId().equals(trip.getId())).toList();

            if (!passengers.isEmpty()) {
                for (TripPassenger passenger : passengers) {
                    int messageCount = random.nextInt(0, 6);

                    for (int i = 0; i < messageCount; i++) {
                        Message message = new Message();
                        message.setTrip(trip);

                        if (i % 2 == 0) {
                            message.setSender(trip.getDriver());
                            message.setReceiver(passenger.getUser());
                        } else {
                            message.setSender(passenger.getUser());
                            message.setReceiver(trip.getDriver());
                        }

                        message.setMessageText(getRandomChatMessage());
                        message.setTimestamp(LocalDateTime.now().minus(random.nextInt(1, 30), ChronoUnit.DAYS).plus(i,
                                ChronoUnit.HOURS));
                        message.setRead(random.nextBoolean());

                        messageRepository.save(message);
                    }
                }
            }
        }
    }

    private String generateBulgarianName() {
        String firstName = getRandomElement(BULGARIAN_FIRST_NAMES);
        String lastName = getRandomElement(BULGARIAN_LAST_NAMES);
        return firstName + " " + lastName;
    }

    private String generateUniqueUsername(Set<String> usedUsernames) {
        String username;
        do {
            username = "user" + "_" + (random.nextInt(100, 999));
        } while (usedUsernames.contains(username));

        usedUsernames.add(username);
        return username;
    }

    private String generateUniqueEmail(Set<String> usedEmails) {
        String email;
        do {
            String domain = getRandomElement(new String[] { "gmail.com", "yahoo.com", "outlook.com", "abv.bg" });
            email = "user" + "_" + random.nextInt(1000, 9999) + "@" + domain;
        } while (usedEmails.contains(email));

        usedEmails.add(email);
        return email;
    }

    private String getRandomReviewComment() {
        String[] comments = { "Отличен шофьор, препоръчвам!", "Удобна кола, безопасно пътуване",
                "Много добър, благодаря!", "Препоръчвам на всички приятели", "Отговорен и любезен",
                "Чиста кола, спокойно пътуване", "Точен, надежден и приветлив", "Добра кола, моля отново!",
                "5 звезди, страхотно изживяване", "Безопасност на първо място", "Харесва ми музиката, хубав избор",
                "Супер опит начало до край" };
        return getRandomElement(comments);
    }

    private String getRandomChatMessage() {
        String[] messages = { "Кога приблизително ще пристигнем?", "Всичко ли е в ред?", "Благодаря за пътуването!",
                "Можеш ли да направиш спирка тук?", "Музиката е отлична!", "Пътуване е толкова комфортно",
                "Очаквам се да се видим на тази точка", "Благодаря че дойде", "Следваща спирка след 10 минути",
                "Всичко е организирано добре", "Супер предложение за цена", "Сигурен съм, че е хубав опит" };
        return getRandomElement(messages);
    }

    private <T> T getRandomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

    private <T> T getRandomElement(T[] array, T exclude) {
        T element;
        do {
            element = getRandomElement(array);
        } while (element.equals(exclude));
        return element;
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
}
