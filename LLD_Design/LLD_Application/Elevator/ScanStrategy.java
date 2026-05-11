package LLD_Application.Elevator;

import java.util.List;

public class ScanStrategy implements ElevatorStrategy{

    @Override
    public Elevator findBestElevator(Request request, List<Elevator> elevatorList) {
        int minDistance = Integer.MAX_VALUE;
        Elevator bestElevator=null;
        for(Elevator e: elevatorList){
            int score= Math.abs(e.getCurrentFloor()-request.getFloor());
            //Ignore stopped ones
            if(e.getElevatorState().equals(ElevatorState.STOP)) continue;

            //penalize opposite ones
            if(request.getRequestState().equals(RequestState.MOVE_UP)&& e.getElevatorState().equals(ElevatorState.DOWN))
                score+=1000;
            if(request.getRequestState().equals(RequestState.MOVE_UP)&& e.getElevatorState().equals(ElevatorState.DOWN))
                score+=1000;

            if(score<minDistance){
                minDistance=score;
                bestElevator=e;
            }

        }

        return  bestElevator;
    }
}
