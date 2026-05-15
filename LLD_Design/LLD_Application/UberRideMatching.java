package LLD_Application;


import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

// --- 1. STATE MACHINES (Enums) ---
enum DriverStatus { AVAILABLE, ON_TRIP, OFFLINE }
enum TripStatus { REQUESTED, EN_ROUTE, COMPLETED, CANCELED }

// --- 2. ENTITIES (Data & Thread-Safe State) ---
class Location {
    int x, y;
    public Location(int x, int y) { this.x = x; this.y = y; }
}

class Rider {
    String id;
    Location location;
    public Rider(String id, int x, int y) {
        this.id = id;
        this.location = new Location(x, y);
    }
}

class Driver {
    String id;
    Location location;

    // ATOMIC CONCURRENCY: Guarantees thread-safe state transitions without locking
    private AtomicReference<DriverStatus> status;

    public Driver(String id, int x, int y) {
        this.id = id;
        this.location = new Location(x, y);
        this.status = new AtomicReference<>(DriverStatus.AVAILABLE);
    }

    public DriverStatus getStatus() { return status.get(); }
    public void setLocation(int x, int y) { this.location = new Location(x, y); }

    // THE MAGIC: Hardware-level atomic check-and-swap
    public boolean acceptRide() {
        return status.compareAndSet(DriverStatus.AVAILABLE, DriverStatus.ON_TRIP);
    }

    public void completeRide() {
        status.set(DriverStatus.AVAILABLE); // Safe to overwrite
    }
}

class Trip {
    String id;
    String riderId;
    String driverId; // Null until matched
    Location start, dest;

    // Also atomic, preventing two threads from matching the same trip twice
    private AtomicReference<TripStatus> status;

    public Trip(String id, String riderId, Location start, Location dest) {
        this.id = id;
        this.riderId = riderId;
        this.start = start;
        this.dest = dest;
        this.status = new AtomicReference<>(TripStatus.REQUESTED);
    }

    public TripStatus getStatus() { return status.get(); }

    public boolean startTrip(String driverId) {
        if (status.compareAndSet(TripStatus.REQUESTED, TripStatus.EN_ROUTE)) {
            this.driverId = driverId;
            return true;
        }
        return false;
    }

    public void completeTrip() {
        status.set(TripStatus.COMPLETED);
    }
}

// --- 3. THE STRATEGY PATTERN (Decoupled Logic) ---
interface MatchingStrategy {
    Driver match(Trip trip, List<Driver> availableDrivers);
}

interface PricingStrategy {
    double calculatePrice(Trip trip);
}

// Concrete Strategy: Nearest Driver (Euclidean Distance)
class NearestDriverStrategy implements MatchingStrategy {
    @Override
    public Driver match(Trip trip, List<Driver> availableDrivers) {
        if (availableDrivers.isEmpty()) return null;

        Driver bestDriver = null;
        double minDistance = Double.MAX_VALUE;

        for (Driver d : availableDrivers) {
            double dist = Math.pow(d.location.x - trip.start.x, 2) +
                    Math.pow(d.location.y - trip.start.y, 2);
            if (dist < minDistance) {
                minDistance = dist;
                bestDriver = d;
            }
        }
        return bestDriver;
    }
}

// Concrete Strategy: Flat Rate Pricing
class FlatRatePricingStrategy implements PricingStrategy {
    @Override
    public double calculatePrice(Trip trip) {
        double dist = Math.sqrt(Math.pow(trip.dest.x - trip.start.x, 2) +
                Math.pow(trip.dest.y - trip.start.y, 2));
        return Math.round(dist * 2.5 * 100.0) / 100.0; // $2.50 per unit distance
    }
}

// --- 4. THE CORE ENGINE (The Orchestrator) ---
class RideSharingEngine {
    private ConcurrentMap<String, Driver> drivers = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Rider> riders = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Trip> trips = new ConcurrentHashMap<>();

    private MatchingStrategy matchingStrategy;
    private PricingStrategy pricingStrategy;

    public RideSharingEngine(MatchingStrategy matchingStrategy, PricingStrategy pricingStrategy) {
        this.matchingStrategy = matchingStrategy;
        this.pricingStrategy = pricingStrategy;
    }

    public void addDriver(String driverId, int x, int y) {
        drivers.put(driverId, new Driver(driverId, x, y));
    }

    public void addRider(String riderId, int x, int y) {
        riders.put(riderId, new Rider(riderId, x, y));
    }

    public String requestRide(String riderId, int destX, int destY) {
        Rider rider = riders.get(riderId);
        if (rider == null) return null;

        String tripId = UUID.randomUUID().toString();
        Trip trip = new Trip(tripId, riderId, rider.location, new Location(destX, destY));
        trips.put(tripId, trip);
        return tripId;
    }

    public void updateDriverLocation(String driverId, int x, int y) {
        Driver driver = drivers.get(driverId);
        if (driver != null) {
            driver.setLocation(x, y);
        }
    }

    // THE SDE II CONCURRENCY MASTERCLASS
    public boolean matchDriver(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null || trip.getStatus() != TripStatus.REQUESTED) return false;

        // 1. Get a snapshot of currently available drivers
        List<Driver> availableDrivers = new ArrayList<>();
        for (Driver d : drivers.values()) {
            if (d.getStatus() == DriverStatus.AVAILABLE) availableDrivers.add(d);
        }

        // 2. The Retry Loop (Optimistic Locking)
        while (!availableDrivers.isEmpty()) {
            Driver bestDriver = matchingStrategy.match(trip, availableDrivers);
            if (bestDriver == null) break;

            // 3. Attempt the Atomic Lock
            if (bestDriver.acceptRide()) {
                // We won the race! Lock the trip.
                if (trip.startTrip(bestDriver.id)) {
                    System.out.println("✅ Trip " + tripId.substring(0,4) + " matched with Driver " + bestDriver.id);
                    return true;
                } else {
                    // Edge case: Trip was canceled while matching. Free the driver.
                    bestDriver.completeRide();
                    return false;
                }
            } else {
                // 4. We lost the race. Another rider grabbed this driver milliseconds ago.
                // Remove from our candidate list and try the next best driver!
                System.out.println("⚠️ Race condition averted! Driver " + bestDriver.id + " was snagged. Retrying...");
                availableDrivers.remove(bestDriver);
            }
        }

        System.out.println("❌ No available drivers for Trip " + tripId.substring(0,4));
        return false;
    }

    public void completeRide(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip != null && trip.getStatus() == TripStatus.EN_ROUTE) {
            trip.completeTrip();
            Driver driver = drivers.get(trip.driverId);
            if (driver != null) driver.completeRide();

            double price = pricingStrategy.calculatePrice(trip);
            System.out.println("🏁 Trip " + tripId.substring(0,4) + " completed. Price: $" + price);
        }
    }
}

// --- 5. THE THUNDERING HERD STRESS TEST ---
public class UberRideMatching {
    public static void main(String[] args) throws InterruptedException {
        RideSharingEngine engine = new RideSharingEngine(new NearestDriverStrategy(), new FlatRatePricingStrategy());

        // Setup: 1 Driver, 3 Riders all in the exact same spot
        engine.addDriver("Driver_John", 0, 0);

        engine.addRider("Rider_A", 0, 0);
        engine.addRider("Rider_B", 0, 0);
        engine.addRider("Rider_C", 0, 0);

        String tripA = engine.requestRide("Rider_A", 10, 10);
        String tripB = engine.requestRide("Rider_B", 10, 10);
        String tripC = engine.requestRide("Rider_C", 10, 10);

        System.out.println("--- Starting Concurrency Test ---");

        CountDownLatch startGun = new CountDownLatch(1);
        CountDownLatch finishLine = new CountDownLatch(3);

        List<String> activeTrips = Arrays.asList(tripA, tripB, tripC);

        for (String tripId : activeTrips) {
            new Thread(() -> {
                try {
                    startGun.await(); // Wait for the bang
                    engine.matchDriver(tripId);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    finishLine.countDown();
                }
            }).start();
        }

        startGun.countDown(); // BANG!
        finishLine.await();   // Wait for all 3 threads to finish
    }
}

