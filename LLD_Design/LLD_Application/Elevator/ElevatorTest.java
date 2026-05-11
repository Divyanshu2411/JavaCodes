package LLD_Application.Elevator;

public class ElevatorTest {

    public static void  simulation() throws InterruptedException {
        // Initialize 2 elevators, serving floors 0 through 10
        ElevatorController controller = new ElevatorController(2, 10);

        // 1. Start the Simulation Loop in a background thread
        Thread gameLoop = new Thread(() -> {
            System.out.println("Elevator system starting...");
            while (!Thread.currentThread().isInterrupted()) {
                controller.step();
                try {
                    // Pause for 1 second per "tick" so we can read the console output
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt flag
                    System.out.println("Elevator system shutting down.");
                }
            }
        });

        gameLoop.start();

        // 2. Simulate User Inputs
        Thread.sleep(500); // Give the system a half-second to boot up

        System.out.println("\n[EVENT] Person on Floor 2 presses UP");
        controller.requestElevator(2, RequestState.MOVE_UP);

        Thread.sleep(3000); // Wait 3 seconds

        System.out.println("\n[EVENT] Person on Floor 8 presses DOWN");
        controller.requestElevator(8, RequestState.MOVE_DOWN);

        System.out.println("\n[EVENT] Person inside elevator presses Floor 5");
        controller.requestElevator(5, RequestState.DESTINATION);

        System.out.println("\n[EVENT] Person inside elevator presses Floor 2");
        controller.requestElevator(2, RequestState.DESTINATION);

        System.out.println("\n[EVENT] Person inside elevator presses Floor 7");
        controller.requestElevator(7, RequestState.DESTINATION);
        System.out.println("\n[EVENT] Person outside elevator presses Floor 4 down");
        controller.requestElevator(4, RequestState.MOVE_DOWN);
        System.out.println("\n[EVENT] Person outside elevator presses Floor 4 UP");
        controller.requestElevator(4, RequestState.MOVE_UP);

        // Let the simulation run for 15 seconds to watch the elevators move
        Thread.sleep(30000);

        // 3. Graceful Shutdown
        System.out.println("\n[EVENT] Shutting down simulation...");
        gameLoop.interrupt();
    }
}

