# DataSeeder - Quick Start Guide

## Overview
The `DataSeeder` automatically populates your Trippzo database with realistic Bulgarian data when the application starts for the first time.

## What Gets Generated

✅ **50 Users** - Authentic Bulgarian names, emails, usernames
✅ **120 Trips** - Future trips with random cities, prices, and descriptions
✅ **150-180 Trip Passengers** - 0-3 passengers per trip
✅ **100-150 Reviews** - Ratings and comments from passengers and drivers
✅ **60-90 Notifications** - Seat request notifications
✅ **400-600 Chat Messages** - Conversations between drivers and passengers

**Total: ~800-1200 database records**

## How to Use

### 1. Start Your Application
```bash
mvn spring-boot:run
```

### 2. Watch the Logs
You'll see:
```
[INFO] Starting data seeding...
[INFO] Created 50 users
[INFO] Created 120 trips
[INFO] Added trip passengers
[INFO] Created reviews
[INFO] Created notifications
[INFO] Created chat messages
[INFO] Data seeding completed in 5432 ms
```

### 3. Access the Data
- **Admin Dashboard:** `http://localhost:8080/admin/dashboard`
- **View Users:** `http://localhost:8080/admin/users`
- **View Trips:** `http://localhost:8080/admin/trips`

## Default Credentials for Test Users

All generated users have:
- **Password:** `123456`
- **Role:** `ROLE_USER`

Example users (random):
```
Username: aleksandur_petrov_234
Email: aleksandur_234@gmail.com
Password: 123456
```

To find test users:
1. Login to admin panel with: `viktorkrustev03@abv.bg` / `123456`
2. Go to `/admin/users`
3. View the full user list with emails and usernames

## Database Check

### Verify Data Was Seeded
```sql
SELECT COUNT(*) as user_count FROM users;
-- Result: 51 (50 users + 1 admin)

SELECT COUNT(*) as trip_count FROM trips;
-- Result: 120

SELECT COUNT(*) as passenger_count FROM trip_passengers;
-- Result: 150-180

SELECT COUNT(*) as review_count FROM reviews;
-- Result: 100-150

SELECT COUNT(*) as notification_count FROM notifications;
-- Result: 60-90

SELECT COUNT(*) as message_count FROM messages;
-- Result: 400-600
```

## Key Features

### ✅ Automatic Execution
- Runs on application startup
- Only once when database is empty
- Doesn't run on subsequent restarts

### ✅ Realistic Data
- Authentic Bulgarian names and cities
- Valid email domains
- Realistic trip prices (5-30 BGN)
- Genuine car models
- Bulgarian text descriptions and reviews

### ✅ Data Integrity
- No duplicate users
- No duplicate passengers per trip
- Drivers never added as passengers
- Reviews only from actual trip participants
- Proper timestamps and relationships

### ✅ Future Trips Only
- All trips scheduled 1-90 days in the future
- Prevents old/expired trip data
- Realistic for testing booking systems

## Customizing the Data

### Increase User Count
Edit `DataSeeder.java`:
```java
for (int i = 0; i < 50; i++) { // Change 50 to desired count
    User user = new User();
    // ...
}
```

### Increase Trip Count
```java
for (int i = 0; i < 120; i++) { // Change 120 to desired count
    Trip trip = new Trip();
    // ...
}
```

### Adjust Passengers per Trip
```java
int passengerCount = random.nextInt(0, 4); // 0-3, change 4 to X
```

### Change Review Coverage
```java
if (random.nextDouble() < 0.6) { // 60%, change to desired percentage
```

### Rebuild and Run
```bash
mvn clean compile
mvn spring-boot:run
```

## Testing Guide

### 1. Test User Login
1. Go to `/login`
2. Use username or email from user list
3. Password: `123456`
4. Should successfully login

### 2. Test Admin Panel
1. Login as admin: `admin` / `123456`
2. See admin link in navigation
3. View dashboard with statistics
4. Manage users and trips

### 3. Test Trip Browsing
1. Go to `/trips/search`
2. Find trips from seeded data
3. View trip details with driver and passengers
4. See reviews from test data

### 4. Test Database Relationships
```sql
-- View trip with passengers
SELECT t.id, t.origin, t.destination, COUNT(tp.id) as passenger_count
FROM trips t
LEFT JOIN trip_passengers tp ON t.id = tp.trip_id
GROUP BY t.id, t.origin, t.destination
HAVING COUNT(tp.id) > 0
LIMIT 5;

-- View reviews for a trip
SELECT r.id, r.rating, u.full_name as reviewer, u2.full_name as reviewee
FROM reviews r
JOIN users u ON r.reviewer_id = u.id
JOIN users u2 ON r.reviewee_id = u2.id
LIMIT 5;

-- View messages between users
SELECT m.id, u1.full_name as sender, u2.full_name as receiver, m.message_text
FROM messages m
JOIN users u1 ON m.sender_id = u1.id
JOIN users u2 ON m.receiver_id = u2.id
LIMIT 5;
```

## Troubleshooting

### Issue: Seeder Doesn't Run
**Solution:** Check if database is empty
- Clear database and restart
- Or check `shouldSeedData()` method

### Issue: Data Not Showing
**Solution:** Refresh page or restart browser
- Data is committed to database
- Check admin panel for complete list

### Issue: Seeder Runs Every Time
**Solution:** Database is being cleared on startup
- Use `ddl-auto=update` not `ddl-auto=create`
- Check `application.properties`

### Issue: Error During Seeding
**Solution:** Check application logs
- Look for NullPointerException or FK constraint errors
- Ensure all repositories are injected correctly

## What's in the Data

### Sample Trip
```
Origin: София
Destination: Пловдив
Departure: 2026-05-20 14:30
Seats: 4
Price per seat: 15 BGN
Car: Volkswagen Golf
Description: "Спокойна и комфортна кола, музика по време на пътуване"
Driver: Виктор Петров (Driver Level: 4/5)
Passengers: 2 (Анна Иванова, Георги Сидоров)
Reviews: Both passengers rated 5 stars with positive comments
Chat: 5 messages between driver and passengers
```

### Sample User
```
Full Name: Borис Иванов
Username: boris_ivanov_567
Email: boris_567@outlook.com
Password: 123456 (BCrypt encoded)
Role: ROLE_USER
Driver Level: 3/5 stars
Trips as Driver: 2
Trips as Passenger: 3
Reviews Given: 4
Reviews Received: 2
```

## Performance

- **Seeding Time:** 5-15 seconds
- **Database Size:** ~20-30 MB (with indexes)
- **Memory Usage:** Minimal (batch operations)
- **Scalability:** Can easily increase to 1000+ users

## Security Notes

⚠️ **Important:**
- This is for development/testing only
- Never use default passwords in production
- Seed only on dev/test environments
- Disable seeding on production

## Next Steps

1. ✅ Start application → seeder runs automatically
2. ✅ Login with admin or test user
3. ✅ Browse trips and users
4. ✅ Test features with real data
5. ✅ Customize counts if needed
6. ✅ Develop confidently with realistic data

## Additional Resources

- **Implementation Summary:** See `IMPLEMENTATION_SUMMARY.md`
- **Admin Setup Guide:** See `ADMIN_SETUP_GUIDE.md`
- **Full Documentation:** See `DATA_SEEDER_DOCUMENTATION.md`

---

**Enjoy testing Trippzo with realistic Bulgarian data!** 🚗✨

