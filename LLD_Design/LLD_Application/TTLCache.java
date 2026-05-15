package LLD_Application;
import java.util.*;
/**
 * Problem 1: The Expiry Counter (Time-To-Live Cache)Context:You need to design an in-memory data structure that tracks elements with a specified Time-To-Live (TTL). Elements expire dynamically based on the system clock.
 * Functional Requirements (API Contract):
 * put_element(String element, long timestamp, int ttlSeconds): Inserts an element into the system with its current timestamp and a TTL.
 * get_element_count(String element, long currentTimestamp): Returns the number of unexpired instances of this specific element currently in the system.
 * get_total_elements(long currentTimestamp): Returns the total count of all unexpired elements across the entire system.
 *
 * SDE II Constraints & Evaluation Criteria:
 *
 * Read Optimization: Read latencies must be strictly bounded to $O(\log n)$ or $O(1)$. You cannot execute an $O(N)$ sequential eviction of a priority queue or list blocking the read path.
 *
 * Binary Search Expectation: You are expected to use contiguous memory blocks (lists/arrays) and binary search variants to quickly find the threshold of expired vs. active timestamps.
 *
 * Memory Management: A system that just skips expired data during reads will cause an Out-of-Memory (OOM) exception in a long-running JVM. You must implement (or clearly define the architecture for) an active cleanup mechanism—either a lazy eviction policy triggered on writes or a simulated background garbage-collection daemon.
 */

/**
 * Requriement:
 *  a TTL based cache that puts element with ttl
 *  duplicates are allowed, each one are unique, when get an elementCount then return unexpired
 *  should support getTotalElement that return entire stuff
 */

/**
 * for TTL, my idea is instead of putting timestamp, put TTL +timestamp
 * a map with string and expiryList custom class that  keeps array sorted and drops the expired array
 * bs on element on timer to find upper_limit
 * a custom GC that cleans expired key as separate function
 */

/**
 * Map me dalne ke liye alag se Eviction list class containing array
 * Usme humesha sorted fashion me insert
 *
 *
 * Entity:
 *
 * Eviciton List: Class
 * - List<Long> timelist;
 * +EvictionList()
 * +putAndClean (currentTIm, tllSecond) => call evict expired before putting
 * +evictExpired() => find active boundary ind using upperBoundary, then list.sublist(0,ind).clear() i.e. remove inactive
 * + getActiveCount(timestamp) => find active boundary using upperBoundry, then timelist.size()-activeBoundary
 * getUpperBoundary(timestamp) => binary searach (lo<=mid) {if([mid]<= timestamp) lo=mid+1, else hi = mid-1; } return lo;
 *
 * TTLCache: Class
 * -Map <String, EvictionList> cache;
 * +put_element(string key, timestamp, ttl) -> if key doesn't exist add key => add to eviction list
 * +getElementCout => key doesn't exist -> 0, else cache.get(key).getActiveCount();
 * +getTotalCount => iterate through the whole map and count active // keep a total key and process that everytime;
 * customGarbageCollector(timestamp) ->
 * empties deactivate for each value, if value becomes empty add it to a array
 * next loop, remove keys from hashmap
 */



class EvictionList{
    List<Long> timeList ;
    EvictionList(){
        this.timeList = new ArrayList<>();
    }

    public void putAndClean(long currentTimestamp, int ttlSeconds){
        long newExpiry = currentTimestamp + ttlSeconds;
        evictExpired(currentTimestamp);
        int insertInd = findUpperBoundary(newExpiry);
        timeList.add(insertInd,newExpiry);
    }
    public void evictExpired(long currentTimestamp){
        if(isEmpty()) return;
        int activeBoundary = findUpperBoundary(currentTimestamp);

        if(activeBoundary >0){
            timeList.subList(0,activeBoundary).clear();
        }
    }

    public  int getActiveCount(long currentTimestamp){
        if(isEmpty()) return  0;
        int activeBoundary = findUpperBoundary(currentTimestamp);
        return timeList.size()-activeBoundary;
    }

    public Boolean isEmpty(){
        return  timeList.isEmpty();
    }

    private int findUpperBoundary(Long timestamp){
        int lo =0;
        int hi = timeList.size()-1;
        int mid = lo;

        while(lo<=hi){
            mid = lo+ (hi-lo)/2;
            if(timestamp >= timeList.get(mid)){
                lo= mid+1;
            }
            else hi= mid-1;
        }

        return  lo;
    }
}

class TTLCacheHelper{
    private final Map<String, EvictionList> ttlCache;
    TTLCacheHelper(){
        this.ttlCache = new HashMap<>();
    }

    public void put_element(String element, long timestamp, int ttlSeconds){
        if(!ttlCache.containsKey(element)){
            ttlCache.put(element, new EvictionList());
        }

        ttlCache.get(element).putAndClean(timestamp,ttlSeconds);
    }

    public int get_element_count(String element, long currentTimestamp){
        if(!ttlCache.containsKey(element)){
            return 0;
        }
        return  ttlCache.get(element).getActiveCount(currentTimestamp);
    }
    public  int get_total_elements(long currentTimestamp){
        int totalActive=0;
        for(var entry: ttlCache.values()){
            totalActive+=entry.getActiveCount(currentTimestamp);
        }

        return  totalActive;
    }

    public void customCleanup(){
        long currentTime = System.currentTimeMillis();
        List<String> keysToRemove = new ArrayList<>();

        for(var entry : ttlCache.entrySet()) {
            EvictionList list = entry.getValue();
            list.evictExpired(currentTime);

            if(list.isEmpty()) {
                keysToRemove.add(entry.getKey());
            }
        }

        for(String key : keysToRemove) {
            ttlCache.remove(key);
            System.out.println("GC: Removed dormant key -> " + key);
        }
    }
}

public class TTLCache {
    public static void main(String[] args){
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

    }


}
