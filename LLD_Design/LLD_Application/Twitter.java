package LLD_Application;
/**
 *

 Problem 3: In-Memory Twitter & Newsfeed GenerationContext:Design the backend class structures for a microblogging platform where users can post tweets, follow others, and view a personalized newsfeed.

 Functional Requirements (API Contract):
 registerUser(String userId): Creates a new user in the system.postTweet(String userId, String tweetId, long timestamp):
 Publishes a new tweet from a user.follow(String followerId, String followeeId):
 Subscribes one user to another's tweets.
 unfollow(String followerId, String followeeId): Removes the subscription.
 getNewsFeed(String userId): Retrieves the 10 most recent tweets from the user and the specific network of individuals they follow.

 SDE II Constraints & Evaluation Criteria:Fan-Out on Write (Push Model): Naively fetching and sorting all tweets from everyone a user follows on every read is unacceptable for this scale.
 You must design a system that pushes tweets to follower feeds asynchronously (or simulates it) so that getNewsFeed is an $O(1)$ or $O(K)$ operation.
 Concurrency & Thread Safety: The underlying simulated database must utilize thread-safe singletons to prevent race conditions during high-throughput concurrent tweet storms.

 */

/**
 Entity (rough)

 System
 - List <User>>



 */
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class Tweet {
    String id;
    String content;
    long timestamp; // Use primitive 'long' instead of wrapper 'Long' for memory efficiency

    Tweet(String id, String content, long timestamp){
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public long getTime(){
        return timestamp;
    }
}

class UserTwitter {
    String id;

    // SDE II FIX: Use a truly concurrent Set, NOT a volatile HashSet
    Set<UserTwitter> followers;
    Set<UserTwitter> following;

    // PriorityQueue is not thread-safe by default, so we must synchronize access
    PriorityQueue<Tweet> pq;

    UserTwitter(String id){
        this.id = id;

        // Min-Heap: Oldest tweet is always at the root
        this.pq = new PriorityQueue<>(Comparator.comparingLong(Tweet::getTime));

        // Creates a thread-safe, lock-free concurrent set
        this.followers = ConcurrentHashMap.newKeySet();
        this.following = ConcurrentHashMap.newKeySet();
    }

    // SDE II FIX: Add first, then poll if size > 10
    public synchronized void addTweet(Tweet tweet){
        pq.add(tweet);
        if (pq.size() > 10) {
            pq.poll(); // Safely ejects the oldest tweet
        }
    }

    // SDE II FIX: Extract read logic into a synchronized, non-destructive method
    public synchronized List<Tweet> getNewsFeed() {
        // 1. Create a safe copy of the queue elements (Non-destructive)
        List<Tweet> tweets = new ArrayList<>(pq);

        // 2. Sort descending so the absolute NEWEST tweet is at the top of the feed
        tweets.sort((t1, t2) -> Long.compare(t2.getTime(), t1.getTime()));

        return tweets;
    }
}

class TwitterFeed {
    ConcurrentHashMap<String, UserTwitter> userList;
    ConcurrentHashMap<String, Tweet> tweetList;

    TwitterFeed(){
        userList = new ConcurrentHashMap<>();
        tweetList = new ConcurrentHashMap<>();
    }

    public void registerUser(String id){
        // Safe atomic initialization
        userList.putIfAbsent(id, new UserTwitter(id));
    }

    public void deregisterUser(String id){
        userList.remove(id);
    }

    public void follow(String followerId, String followingId){
        UserTwitter follower = userList.get(followerId);
        UserTwitter following = userList.get(followingId);

        if (follower != null && following != null && follower != following) {
            following.followers.add(follower);
            follower.following.add(following);
        }
    }

    public void unfollow(String followerId, String followingId){
        UserTwitter follower = userList.get(followerId);
        UserTwitter following = userList.get(followingId);

        if (follower != null && following != null && follower != following) {
            following.followers.remove(follower);
            follower.following.remove(following);
        }
    }

    public void postTweet(String userId, String tweetId, long timestamp){
        Tweet tweet = new Tweet(tweetId, UUID.randomUUID().toString(), timestamp);
        tweetList.put(tweetId, tweet);

        UserTwitter user = userList.get(userId);
        if (user == null) return;

        // SDE II FIX: Don't forget to push the tweet to the author's own feed!
        user.addTweet(tweet);

        // Push to all followers.
        // Because followers is a ConcurrentHashMap.newKeySet(), iterating here is 100% thread-safe.
        for (UserTwitter follower : user.followers){
            follower.addTweet(tweet);
        }
    }

    public List<Tweet> getNewsFeed(String userId){
        UserTwitter user = userList.get(userId);
        if (user == null) return new ArrayList<>();

        // Delegate to the synchronized method inside the User class
        return user.getNewsFeed();
    }

    public void printTweet(List<Tweet> tweets, String userId){
        System.out.println("Printing newsfeed for User: " + userId);
        for(Tweet tweet : tweets){
            System.out.println(" - TweetID: " + tweet.id + " | Time: " + tweet.timestamp);
        }
        System.out.println();
    }
}

public class Twitter {
    public static void main(String[] args) {
        TwitterFeed twitterFeed = new TwitterFeed();

        twitterFeed.registerUser("1234");
        twitterFeed.registerUser("1");
        twitterFeed.registerUser("12");
        twitterFeed.registerUser("123");

        twitterFeed.follow("123", "1234");
        twitterFeed.follow("12", "123");
        twitterFeed.follow("1", "123");
        twitterFeed.follow("1", "1234");

        // Post tweets out of chronological order to test the PriorityQueue sorting
        twitterFeed.postTweet("1234", "tweetId1", 15);
        twitterFeed.postTweet("123", "tweetId2", 25);
        twitterFeed.postTweet("12", "tweetId3", 5);

        twitterFeed.printTweet(twitterFeed.getNewsFeed("1"), "1");
        // Test destructive read bug (should still print the exact same list)
        twitterFeed.printTweet(twitterFeed.getNewsFeed("1"), "1 (Second Read)");

        twitterFeed.printTweet(twitterFeed.getNewsFeed("12"), "12");
        twitterFeed.printTweet(twitterFeed.getNewsFeed("123"), "123");
        twitterFeed.printTweet(twitterFeed.getNewsFeed("1234"), "1234");
    }
}