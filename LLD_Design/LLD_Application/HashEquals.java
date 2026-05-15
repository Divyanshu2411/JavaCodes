package LLD_Application;

import java.util.Objects;

class UserTest {
    private int id;
    private String name;

    public UserTest(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        // 1. The Fast Path: Are they literally the exact same object in memory?
        if (this == obj) {
            return true;
        }

        // 2. The Null & Type Check: Is the other object null, or a completely different class?
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        // 3. The Cast: We now know it's safe to cast 'obj' to a User
        UserTest otherUser = (UserTest) obj;

        // 4. The Data Comparison: Compare primitives with ==, and Objects with Objects.equals()
        return this.id == otherUser.id &&
                Objects.equals(this.name, otherUser.name);
    }

    @Override
    public int hashCode() {
        // Use the built-in utility (Available since Java 7)
        // You MUST pass in the exact same fields you used in the equals() method.
        return Objects.hash(id, name);
    }
}

public class HashEquals {
}
