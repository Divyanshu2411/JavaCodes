package LLD_Application.RateLimiter;

public class SingleBucketKeyStrategy implements KeyExtractionStrategy {
    public String extactKey(Object Request){
        return "1";
    }
}
