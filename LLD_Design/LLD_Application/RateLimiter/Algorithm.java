package LLD_Application.RateLimiter;

public interface Algorithm {
    boolean isRequestAllowed(String identifier);


}
