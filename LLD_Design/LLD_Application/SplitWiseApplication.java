package LLD_Application;

import java.util.*;

enum SplitType { EQUAL, EXACT, PERCENT }

class Balance {
    String user;
    double amount;

    public Balance(String user, double amount) {
        this.user = user;
        this.amount = amount;
    }
}
// A generic POJO to hold who is involved and their value (if applicable)
class Split {
    String userId;
    double value; // Represents Exact Amount OR Percentage. (Ignored for EQUAL)

    public Split(String userId, double value) { this.userId = userId; this.value = value; }
    public Split(String userId) { this.userId = userId; } // Constructor for EQUAL splits
}

public class SplitWiseApplication {

    // The Ledger: Map<User, Map<PersonTheyOwe, Amount>>
    private Map<String, Map<String, Double>> balances = new HashMap<>();

    // Helper to initialize map entries safely
    private void ensureUser(String userId) {
        balances.putIfAbsent(userId, new HashMap<>());
    }

    public void addExpense(String payer, double totalAmount, List<Split> splits, SplitType type) {
        ensureUser(payer);
        double equalAmount = totalAmount / splits.size();

        for (Split split : splits) {
            ensureUser(split.userId);
            if (split.userId.equals(payer)) continue; // You don't owe yourself

            double oweAmount = 0;
            switch (type) {
                case EQUAL:   oweAmount = equalAmount; break;
                case EXACT:   oweAmount = split.value; break;
                case PERCENT: oweAmount = totalAmount * (split.value / 100.0); break;
            }

            // Java 8 merge: Adds 'oweAmount' to the existing value, or sets it if it doesn't exist.
            // 1. The Owee owes the Payer (+)
            balances.get(split.userId).merge(payer, oweAmount, Double::sum);
            // 2. The Payer is owed by the Owee (-)
            balances.get(payer).merge(split.userId, -oweAmount, Double::sum);
        }
    }

    public void simplifyDebts() {
        // 1. Calculate Net Balances
        Map<String, Double> netBalances = new HashMap<>();
        for (String user : balances.keySet()) {
            double net = 0;
            for (double amountOwedToOthers : balances.get(user).values()) {
                net -= amountOwedToOthers;
            }
            netBalances.put(user, net);
        }

        // 2. Build the Heaps (Both Max-Heaps sorting by absolute value descending)
        PriorityQueue<Balance> receivers = new PriorityQueue<>((a, b) -> Double.compare(b.amount, a.amount));
        PriorityQueue<Balance> givers = new PriorityQueue<>((a, b) -> Double.compare(b.amount, a.amount));

        // 2. Populate the heaps
        for (Map.Entry<String, Double> entry : netBalances.entrySet()) {
            double amount = Math.round(entry.getValue() * 100.0) / 100.0;

            if (amount > 0) {
                receivers.offer(new Balance(entry.getKey(), amount));
            } else if (amount < 0) {
                givers.offer(new Balance(entry.getKey(), Math.abs(amount)));
            }
        }

        System.out.println("\n--- Simplified Debts ---");

        // 3. Greedy Settlement
        while (!receivers.isEmpty() && !givers.isEmpty()) {
            Balance receiver = receivers.poll();
            Balance giver = givers.poll();

            double settleAmount = Math.min(receiver.amount, giver.amount);
            System.out.println(giver.user + " owes " + receiver.user + " $" + settleAmount);

            double remainingReceiver = receiver.amount - settleAmount;
            double remainingGiver = giver.amount - settleAmount;

            // Push back to heap if they still have a balance to settle
            if (remainingReceiver > 0.001) {
                receiver.amount = remainingReceiver; // Easy variable update!
                receivers.offer(receiver);
            }
            if (remainingGiver > 0.001) {
                giver.amount = remainingGiver;
                givers.offer(giver);
            }
        }

    }

    public static void main(String[] args) {
        SplitWiseApplication app = new SplitWiseApplication();

        // Expense 1: Alice pays $300 for Alice, Bob, and Charlie (EQUAL)
        app.addExpense("Alice", 300, Arrays.asList(
                new Split("Alice"), new Split("Bob"), new Split("Charlie")
        ), SplitType.EQUAL);

        // Expense 2: Bob pays $150 for Bob and Charlie (EQUAL)
        app.addExpense("Bob", 150, Arrays.asList(
                new Split("Bob"), new Split("Charlie")
        ), SplitType.EQUAL);

        // Simplify!
        app.simplifyDebts();
    }
}