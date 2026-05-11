package LLD_Application.Elevator;

import java.util.List;

public interface ElevatorStrategy {
    public Elevator findBestElevator(Request request, List<Elevator> elevatorList);
}
