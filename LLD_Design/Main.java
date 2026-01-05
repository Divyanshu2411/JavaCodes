import customDS.hashmaps.CusHashMap;
import customDS.list.CusDLL;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize the List with String Keys and Integer Values
        CusDLL<String, Integer> list = new CusDLL<>();

        System.out.println("--- Testing Add ---");
        list.add("Apple", 10);
        list.add("Banana", 20);
        list.add("Cherry", 30);
        list.print(); // Expected: Apple : 10->Banana : 20->Cherry : 30->NULL
        System.out.println("Current Size: " + list.getSize()); // Expected: 3

        System.out.println("\n--- Testing Update ---");
        list.update("Banana", 99);
        list.print(); // Expected: Apple : 10->Banana : 99->Cherry : 30->NULL

        System.out.println("\n--- Testing Remove (Middle) ---");
        list.remove("Banana");
        list.print(); // Expected: Apple : 10->Cherry : 30->NULL

        System.out.println("\n--- Testing Remove (Head) ---");
        list.remove("Apple");
        list.print(); // Expected: Cherry : 30->NULL

        System.out.println("\n--- Testing Remove (Tail/Last Item) ---");
        list.remove("Cherry");
        list.print(); // Expected: NULL
        System.out.println("Final Size: " + list.getSize()); // Expected: 0

        // 2. Testing Exception Handling
        try {
            System.out.println("\n--- Testing Invalid Remove ---");
            list.remove("Dragonfruit");
        } catch (RuntimeException e) {
            System.out.println("Caught Expected Error: " + e.getMessage());
        }

        CusHashMap<String, Integer> map = new CusHashMap<>();

        // 1. Add 7 elements (Threshold is 7 for size 10)
        for(int i = 1; i <= 8; i++) {
            map.add("Key" + i, i);
        }
        System.out.println("Size before rehash: " + map.containerSize); // Should be 10

        // 2. Add 8th element -> Triggers reHash()
        map.add("Key9", 8);
        System.out.println("Size after rehash: " + map.containerSize);  // Should be 20

        // 3. Verify data integrity
        System.out.println("Value for Key1: " + map.get("Key1")); // Should be 1
        System.out.println("Value for Key9: " + map.get("Key9")); // Should be 8

    }
}