package LLD_Application.Elevator;

import lombok.Setter;

import java.util.List;

public class ElevatorStategyImpl  implements  ElevatorStrategy{
    @Setter
    private  ElevatorStrategy elevatorStrategy;
    public ElevatorStategyImpl(ElevatorStrategy elevatorStrategy){
        this.elevatorStrategy = elevatorStrategy;
    }
    @Override
    public Elevator findBestElevator(Request request, List<Elevator> elevatorList) {
       return elevatorStrategy.findBestElevator(request,elevatorList);
    }
}
