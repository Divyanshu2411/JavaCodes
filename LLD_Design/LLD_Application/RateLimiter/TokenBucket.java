package LLD_Application.RateLimiter;

import java.util.concurrent.ConcurrentHashMap;


public class TokenBucket implements  Algorithm{
    int maxCapacity;
    int refillRatePerSecond;

    ConcurrentHashMap<String, Bucket> indiBucket;
    public TokenBucket(int maxCapacity, int refillRatePerSecond){
        this.maxCapacity= maxCapacity;
        this.refillRatePerSecond = refillRatePerSecond;
        indiBucket = new ConcurrentHashMap<>();
    }


    @Override
    public boolean isRequestAllowed(String identifier) {
        Bucket userBucket = indiBucket.get(identifier);
        if(userBucket==null){
            Bucket newBucket = new Bucket(maxCapacity,System.currentTimeMillis());
            Bucket existingBucket = indiBucket.putIfAbsent(identifier,newBucket);
            if(existingBucket==null)
                userBucket= newBucket;
            else
                userBucket= existingBucket;
        }

        return userBucket.isRequestAccepted(maxCapacity,refillRatePerSecond);
    }

    class Bucket {
        double capacity; // This MUST be a double to track partial tokens
        long lastRefillTime;

        Bucket(int capacity, long lastRefillTime) {
            this.capacity = (double) capacity;
            this.lastRefillTime = lastRefillTime;
        }

        private void refillBucket(int maxCapacity, int refillRate) {
            long currTime = System.currentTimeMillis();
            long elapsedTime = currTime - lastRefillTime;

            if (elapsedTime > 0) {
                // refillRate is tokens per second.
                // So tokens per ms is (refillRate / 1000.0)
                double tokensToAdd = elapsedTime * (refillRate / 1000.0);

                // Add the fractional tokens to the double capacity
                this.capacity = Math.min((double) maxCapacity, this.capacity + tokensToAdd);
                this.lastRefillTime = currTime;
            }
        }

        public synchronized boolean isRequestAccepted(int maxCapacity, int refillRate) {
            refillBucket(maxCapacity, refillRate);

            // We only accept the request if we have at least 1 FULL token
            if (this.capacity >= 1.0) {
                this.capacity -= 1.0;
                return true;
            }
            return false;
        }
    }
}
