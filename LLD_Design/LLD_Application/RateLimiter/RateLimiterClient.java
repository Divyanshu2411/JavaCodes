package LLD_Application.RateLimiter;

public class RateLimiterClient {

    public static void rateLimiterTokenBucketClientRunner() throws InterruptedException {
        RateLimiterService rateLimiterService = RateLimiterService.getInstance();
        Algorithm algorithm = new TokenBucket(5,2);
        rateLimiterService.setAlgorithmStrategy(algorithm);
        rateLimiterService.setKeyExtractionStrategy(new SingleBucketKeyStrategy());

        long currTime = System.currentTimeMillis();
        while(System.currentTimeMillis()<=currTime+5000){

            rateLimiterService.allowAccess(124);
            Thread.sleep(100);
        }
    }


}
