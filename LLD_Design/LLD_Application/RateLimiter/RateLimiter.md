# Rate Limiter

## Components
    - RateLimiter Service
        - double locked singleton initializer and a public instance
        - accept(Object Request)
        - setter algorithm + key extraction + user type (gold/platinum) ENUM
    - Rate Limiting Algorithms (interface)
        - Token bucket
        - Sliding Window
        - Leaky Bucket
    - Key Extraction Strategy (to determine throttle on what)

### Rate Limiter Service
 - Double locked singleton with volatil class instance
 -  ```java 
    public static RateLimiterService getInstance(){
    if(rateLimiterServiceInstance ==null){
    synchronized (RateLimiterService.class){
    if(rateLimiterServiceInstance ==null)
    rateLimiterServiceInstance = new RateLimiterService();
    }
    }
    return LimiterServiceInstance;;
    }
    ```
 - Ensure that you mark the default constructor as empty

## Rate Limitng Algorithm Interface
    - boolean isRequestAllowed(String identifier);

## Key Extraction Strategy (user based/ip based)
    -     public String extactKey(Object Request);

## Token Bucket Algorithm
### Key points
    - a max capacity is there, token refills based on refill rate
    - new request consumes one token

### implementation
    - internal bucket class
        - double capacity (double is important to refill partially)
        - last filled timestamp
        - fn. refill -> takes maxCapacity as argument and rate of filling, calculate time elapsed  (fromm last filled to now) and fill the bucket.
        - refill is called every time a new request comes
        - fn. validRequest -> fills the capacity, if capacity is there decrement capacity and accept request(return true) else return false.
    - maxCapacity
    - refill rate per second
    - concurrentHashmap<identifier, bucket> to do identifier based throttling.
    - acceptRequest fn that checks if the identifier already has a bucket, if doesn't it create new bucket and then calls the bucket's valid request
    - valid request is a synchronized block locks on the Bucket instance, not the class. This means it only blocks concurrent requests coming from the exact same user/IP. Thread A (User 1) and Thread B (User 2) will execute in parallel without waiting for each other. and since it's pure mathematics, blocking is for nano seconds, which is trivial for same IP/ user. 
    
-   ```java 
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

        return return userBucket.isRequestAccepted(maxCapacity,refillRatePerSecond);```
- ```java     
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


  
    
   
