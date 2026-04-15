package LLD_Application.RateLimiter;

public class RateLimiterService {
    private  static volatile RateLimiterService rateLimiterServiceInstance;
    private Algorithm algorithmStrategy;
    private  KeyExtractionStrategy keyExtractionStrategy;

    private RateLimiterService(){};

    public static RateLimiterService getInstance(){
        if(rateLimiterServiceInstance ==null){
            synchronized (RateLimiterService.class){
                if(rateLimiterServiceInstance ==null)
                    rateLimiterServiceInstance = new RateLimiterService();
            }
        }
        return rateLimiterServiceInstance;
    }

    //setter for algorithm strategy
    //setter for keyExtractionStrategy
    // builder pattern if more info needed
    public void setAlgorithmStrategy(Algorithm algorithmStrategy) {
        rateLimiterServiceInstance.algorithmStrategy = algorithmStrategy;
    }

    public void setKeyExtractionStrategy(KeyExtractionStrategy keyStrategy) {
        rateLimiterServiceInstance.keyExtractionStrategy = keyStrategy;
    }

    public Boolean allowAccess(Object request){
        if(algorithmStrategy==null || keyExtractionStrategy ==null)
            throw new IllegalStateException("algroithm or key is not defined");

// no detailed logging will be there for this to keep the system fast as logging delays a lot.
        Boolean allowed= algorithmStrategy.isRequestAllowed(keyExtractionStrategy.extactKey(request));
        if(!allowed) System.out.println("Request is throtlled, 429 too many request");
        else System.out.println("Request Accepted: 2XX");
        return allowed;
    }


}
