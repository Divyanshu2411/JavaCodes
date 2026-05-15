package LLD_Application;

//23:20

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 Requirements:
 System needs to manage different user tier, standard, gold, VIP
 System must support sending notification via multiple channels (SMS, email, Push)
 User should be able to opt in and opt out
 System Needs to support different kinds of discount campapign
 System must tirgger a campaign that calculates the dicounnt and send appropriate message


 =========
 Entities (rough)

 System - main class
 - List<User>
 + triggerNotification(Notiification)


 User
 - tier : enum Tier
 - subscribedTo: ArrayList<Channel Enums>
 + optIn(channel)
 + optOut(channel)
 + getDiscount() -> strategy
 + send(List<Channel>>)

 Notification - class
 - content
 - baseDiscount
 + SendAll(List<User>)
 + SendAllUserChannel(List<User>,List<Channel>)
 + SendUser(User, List<Channel>)




 //notiifcation simply sends,

 UserTier: enum
 - GOLD
 - SILVER
 - VIP

 CHANNEL: interface class
  + getType()
  + send()

 Msg implement channle
 Email implement channel
 Push implement channel


 */

/**
 * Learning
 * Data classes me don't add infra logic (like sending notification)
 * Only data related to class should be there
 * Implement stategy to get discount
 */

enum USER_TIER{
    GOLD,
    SILVER,
    VIP
}

enum ChannelType{
    SMS,
    PUSH,
    EMAIL
}

interface Channel{
    public ChannelType getType();
    public void send(User user, String notificationContent);

}
class Email implements  Channel {
    @Override
    public ChannelType getType(){
        return ChannelType.EMAIL;
    }

    @Override
    public void send(User user,String notification){
        System.out.println("Sent Email to " + user.name+ " : "+ notification);
    }
}

class Sms implements  Channel {
    @Override
    public ChannelType getType(){
        return ChannelType.SMS;
    }

    @Override
    public void send(User user,String notification){
        System.out.println("Sent SMS to " + user.name+ " : "+ notification);
    }
}

 class  ChannelFactory{
    public static Channel getChannel(ChannelType channel) {
        Channel channelImpl = null;
        if(channel.equals(ChannelType.EMAIL)) channelImpl=new Email();
        else if(channel.equals(ChannelType.SMS)) channelImpl=new Sms();
        else if(channel.equals(ChannelType.PUSH)) channelImpl=new Push();
        return  channelImpl;
    }
}

class Push implements  Channel {
    @Override
    public ChannelType getType(){
        return ChannelType.PUSH;
    }

    @Override
    public void send(User user,String notification){
        System.out.println("Sent Push to " + user.name+ " : "+ notification);
    }
}

class User {
//    User
// - tier : enum Tier
// - subscribedTo: ArrayList<Channel Enums>
//            + optIn(channel)
// + optOut(channel)
// + getDiscount() -> strategy
    USER_TIER userTier;
    String name;
    Set<ChannelType> subscribedChannels = new HashSet<>();

    void optOut(List<ChannelType> channelList ){
        for(ChannelType channel : channelList){
            subscribedChannels.remove(channel);
        }
    }

    void optIn(List<ChannelType> channelList ){
        subscribedChannels.addAll(channelList);
    }

    /**
     * THIS IS BAD AS DATA ENTITY SHOULD NOT HAVE THIS
     * */
//    void send(List<CHANNEL> channelList, Notif notification){
//        for(CHANNEL channel: channelList){
//            if(this.subscribedChannels.contains(channel)){
//                Channel notiChannel = ChannelFactory.getChannel(channel);
//                notiChannel.send(notification);
//            }
//        }
//    }
//    void send(Notif notification){
//        send((List<CHANNEL>) subscribedChannels,notification);
//    }
}




// 1. Interface accepts the User context
interface DiscountStrategy {
    String getOfferMessage(User user);
}

// 2. SRP: Centralize the tier multiplier logic so we don't violate DRY
class TierMultiplier {
    public static double get(USER_TIER tier) {
        if (tier == USER_TIER.VIP) return 2.0;
        if (tier == USER_TIER.GOLD) return 1.5;
        return 1.0; // SILVER / Standard
    }
}

// 3. Flat Discount Implementation
class FlatDiscount implements DiscountStrategy {
    private double baseAmount;

    public FlatDiscount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    @Override
    public String getOfferMessage(User user) {
        // Calculate dynamic amount based on user's tier
        double finalAmount = baseAmount * TierMultiplier.get(user.userTier);
        return "Enjoy a flat $" + finalAmount + " off your next ride!";
    }
}

// 4. Percentage Discount Implementation
class PercentageDiscount implements DiscountStrategy {
    private double basePercentage;

    public PercentageDiscount(double basePercentage) {
        this.basePercentage = basePercentage;
    }

    @Override
    public String getOfferMessage(User user) {
        // Calculate dynamic percentage based on user's tier
        double finalPercentage = basePercentage * TierMultiplier.get(user.userTier);

        // Edge case safety: Don't let VIP multipliers exceed 100% off!
        finalPercentage = Math.min(finalPercentage, 100.0);

        return "Enjoy " + finalPercentage + "% off your next ride!";
    }
}

class UberDiscountNotif{
    /**
     *  System - main class
     *  - List<User>
     *  + Register User
     *  + Remove User
     *  + triggerNotification(Notiification)
     *  + triggerNotification(Notiification, List<Channel>)
     */

    Set<User> userList;
    UberDiscountNotif(){
        userList = new HashSet<>();
    }

    void registerUser(User user){
        userList.add(user);
    }

    void deregisterUser(User user){
        userList.remove(user);
    }

    public void executeCampaign(DiscountStrategy strategy){
        for(User user : userList){
            if(user.subscribedChannels.isEmpty()) continue;
            String personalisedMessage = strategy.getOfferMessage(user);

            for(ChannelType type : user.subscribedChannels){
                Channel channel = ChannelFactory.getChannel(type);
                if(channel!=null){
                    channel.send(user,personalisedMessage);
                }
            }
        }
    }

}
public class UberDiscountNotification {

    public static void main(String[] args) {
        UberDiscountNotif system = new UberDiscountNotif();

        // 1. Create Users
        User alice = new User();
        alice.name = "Alice";
        alice.userTier = USER_TIER.VIP;

        User bob = new User();
        bob.name = "Bob";
        bob.userTier = USER_TIER.GOLD;

        User charlie = new User();
        charlie.name = "Charlie";
        charlie.userTier = USER_TIER.SILVER;

        // 2. Set Preferences (Opting In)
        List<ChannelType> alicePrefs = new ArrayList<>();
        alicePrefs.add(ChannelType.PUSH);
        alicePrefs.add(ChannelType.EMAIL);
        alice.optIn(alicePrefs);

        List<ChannelType> bobPrefs = new ArrayList<>();
        bobPrefs.add(ChannelType.SMS);
        bob.optIn(bobPrefs);

        List<ChannelType> charliePrefs = new ArrayList<>();
        charliePrefs.add(ChannelType.PUSH);
        charlie.optIn(charliePrefs);

        // 3. Register Users
        system.registerUser(alice);
        system.registerUser(bob);
        system.registerUser(charlie);

        // --- SIMULATION TESTS ---

        // Test 1: Flat Discount ($5 Base)
        // Expected: VIP gets $10, Gold gets $7.5, Silver gets $5
        System.out.println("--- CAMPAIGN 1: $5.00 Base Flat Discount ---");
        DiscountStrategy flatPromo = new FlatDiscount(5.0);
        system.executeCampaign(flatPromo);

        // Test 2: Percentage Discount (60% Base)
        // Expected: VIP hits 120% but caps at 100%, Gold gets 90%, Silver gets 60%
        System.out.println("\n--- CAMPAIGN 2: 60% Base Percentage Discount ---");
        DiscountStrategy percentPromo = new PercentageDiscount(60.0);
        system.executeCampaign(percentPromo);

        // Test 3: Opt-Out Functionality
        // Expected: Alice should no longer receive PUSH notifications, only EMAIL
        System.out.println("\n--- CAMPAIGN 3: Alice opts out of PUSH ---");
        List<ChannelType> aliceOptOut = new ArrayList<>();
        aliceOptOut.add(ChannelType.PUSH);
        alice.optOut(aliceOptOut);

        system.executeCampaign(flatPromo); // Run the flat promo again to verify
    }
}