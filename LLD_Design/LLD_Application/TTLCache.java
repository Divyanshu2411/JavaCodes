package LLD_Application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

class EvictionList {
    private List<Long> timeList;

    EvictionList() {
        this.timeList = new ArrayList<>();
    }

    // Locks THIS specific EvictionList instance (Key-Level Locking)
    public synchronized void putAndClean(long currentTimestamp, int ttlSeconds) {
        long newExpiry = currentTimestamp + ttlSeconds;
        evictExpiredInternal(currentTimestamp);
        int insertInd = findUpperBoundary(newExpiry);
        timeList.add(insertInd, newExpiry);
    }

    // Public method for the GC Thread
    public synchronized void evictExpired(long currentTimestamp) {
        evictExpiredInternal(currentTimestamp);
    }

    // Private internal method to prevent deadlocks when called by putAndClean
    private void evictExpiredInternal(long currentTimestamp) {
        if(isEmptyInternal()) return;
        int activeBoundary = findUpperBoundary(currentTimestamp);

        if(activeBoundary > 0) {
            timeList.subList(0, activeBoundary).clear();
        }
    }

    public synchronized int getActiveCount(long currentTimestamp) {
        if(isEmptyInternal()) return 0;
        int activeBoundary = findUpperBoundary(currentTimestamp);
        return timeList.size() - activeBoundary;
    }

    public synchronized Boolean isEmpty() {
        return isEmptyInternal();
    }

    private boolean isEmptyInternal() {
        return timeList.isEmpty();
    }

    // Doesn't need synchronized if it's only called from synchronized methods
    private int findUpperBoundary(Long timestamp) {
        int lo = 0;
        int hi = timeList.size() - 1;
        while(lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            if(timestamp >= timeList.get(mid)) lo = mid + 1;
            else hi = mid - 1;
        }
        return lo;
    }
}

class TTLCacheHelper {
    // Upgraded to ConcurrentMap
    private final ConcurrentMap<String, EvictionList> ttlCache;

    TTLCacheHelper() {
        this.ttlCache = new ConcurrentHashMap<>();
    }

    public void put_element(String element, long timestamp, int ttlSeconds) {
        // Safe, atomic initialization without locking the whole map
        ttlCache.putIfAbsent(element, new EvictionList());

        // Delegate to the synchronized object
        ttlCache.get(element).putAndClean(timestamp, ttlSeconds);
    }

    public int get_element_count(String element, long currentTimestamp) {
        EvictionList list = ttlCache.get(element);
        if(list == null) return 0;

        return list.getActiveCount(currentTimestamp);
    }

    public int get_total_elements(long currentTimestamp) {
        int totalActive = 0;
        // ConcurrentHashMap allows safe iteration even if other threads are writing
        for(var entry : ttlCache.values()) {
            totalActive += entry.getActiveCount(currentTimestamp);
        }
        return totalActive;
    }

    public void customCleanup() {
        long currentTime = System.currentTimeMillis();

        for(var entry : ttlCache.entrySet()) {
            EvictionList list = entry.getValue();
            list.evictExpired(currentTime);

            if(list.isEmpty()) {
                // ATOMIC REMOVAL: Only removes if 'list' is still the exact same object
                // Prevents a race condition where a user writes just as GC deletes it
                ttlCache.remove(entry.getKey(), list);
            }
        }
    }
}

public class TTLCache {
    public static void main(String[] args) throws InterruptedException {
        TTLCacheHelper cache = new TTLCacheHelper();

        Thread gcThread = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                System.out.println("Garbage Collector Starting cleanup");
                cache.customCleanup();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    System.out.println("Garbage collector shutting down");
                }
            }
        });

        gcThread.setDaemon(true);
        gcThread.start();



        // ==========================================
        // SIMULATION TESTS (Using explicit time)
        // ==========================================

        System.out.println("\n--- Starting Tests ---");
        long baseTime = 1000; // Pretend current time is 1000 seconds

        // Test 1: Basic Insertion
        System.out.println("1. Inserting elements at T=1000");
        cache.put_element("UserA", baseTime, 10); // Expires at 1010
        cache.put_element("UserA", baseTime, 20); // Expires at 1020
        cache.put_element("UserB", baseTime, 5);  // Expires at 1005

        System.out.println("UserA Count (Expected 2): " + cache.get_element_count("UserA", baseTime));
        System.out.println("UserB Count (Expected 1): " + cache.get_element_count("UserB", baseTime));
        System.out.println("Total Count (Expected 3): " + cache.get_total_elements(baseTime));

        // Test 2: Partial Expiry
        System.out.println("\n2. Advancing time to T=1015 (UserB and one UserA should expire)");
        long time1015 = 1015;

        System.out.println("UserA Count (Expected 1): " + cache.get_element_count("UserA", time1015));
        System.out.println("UserB Count (Expected 0): " + cache.get_element_count("UserB", time1015));
        System.out.println("Total Count (Expected 1): " + cache.get_total_elements(time1015));

        // Test 3: Lazy Eviction Verification
        System.out.println("\n3. Testing Lazy Eviction on Write");
        // UserB's array still physically holds the expired '1005' timestamp until a write occurs.
        // Let's write to UserB to trigger lazy eviction.
        cache.put_element("UserB", time1015, 30); // Expires at 1045
        System.out.println("UserB Count after new write (Expected 1): " + cache.get_element_count("UserB", time1015));
        System.out.println("Total Count (Expected 2): " + cache.get_total_elements(time1015));

        // Test 4: Total Expiry
        System.out.println("\n4. Advancing time to T=1050 (Everything should be expired)");
        long time1050 = 1050;
        System.out.println("UserA Count (Expected 0): " + cache.get_element_count("UserA", time1050));
        System.out.println("UserB Count (Expected 0): " + cache.get_element_count("UserB", time1050));
        System.out.println("Total Count (Expected 0): " + cache.get_total_elements(time1050));

        System.out.println("\nTests complete. Leaving main thread alive briefly to let GC fire...");
        try {
            // Sleep the main thread for 6 seconds so the 5-second GC loop fires at least once
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // ==========================================
        // 1. FUNCTIONAL TESTS (Single Threaded)
        // ==========================================
        System.out.println("\n--- Starting Functional Tests ---");

        cache.put_element("UserA", baseTime, 10);
        cache.put_element("UserA", baseTime, 20);
        cache.put_element("UserB", baseTime, 5);

        System.out.println("UserA Count (Expected 2): " + cache.get_element_count("UserA", baseTime));
        System.out.println("UserB Count (Expected 1): " + cache.get_element_count("UserB", baseTime));


        // ==========================================
        // 2. CONCURRENCY STRESS TEST (The "Thundering Herd")
        // ==========================================
        System.out.println("\n--- Starting Multi-threaded Stress Test ---");

        int numThreads = 50;
        int insertsPerThread = 100;
        String stressKey = "StressUser";

        // Latch 1: Acts as a starting gun so all threads start at the exact same time
        CountDownLatch startGun = new CountDownLatch(1);

        // Latch 2: Lets the main thread know when all worker threads are done
        CountDownLatch finishLine = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            new Thread(() -> {
                try {
                    // Every thread pauses here, waiting for the startGun to fire
                    startGun.await();

                    for (int j = 0; j < insertsPerThread; j++) {
                        // All threads violently write to the exact same key at the same time
                        cache.put_element(stressKey, baseTime, 50);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // Tell the main thread this specific worker is done
                    finishLine.countDown();
                }
            }).start();
        }

        System.out.println("All threads created and waiting at the starting line...");

        // BANG! Release the threads
        startGun.countDown();

        // Main thread waits here until all 50 threads cross the finish line
        finishLine.await();

        System.out.println("All threads finished writing.");

        // ==========================================
        // 3. VERIFY THE RESULTS
        // ==========================================
        int expectedCount = numThreads * insertsPerThread; // 50 * 100 = 5,000
        int actualCount = cache.get_element_count(stressKey, baseTime);

        System.out.println("Expected " + stressKey + " Count: " + expectedCount);
        System.out.println("Actual " + stressKey + " Count:   " + actualCount);

        if (expectedCount == actualCount) {
            System.out.println("SUCCESS: No race conditions! The synchronized blocks are working perfectly.");
        } else {
            System.out.println("FAILED: Race condition detected. Data was lost or overwritten!");
        }

        System.out.println("\nTests complete. Exiting.");

    }


}
