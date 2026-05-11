package LLD_Application.Elevator;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class Elevator {

    private Integer currentFloor;
    @Getter
    private ElevatorState elevatorState;
    private Boolean gateOpen;
    private Set<Request> requests;
    private Integer maxFloor;

    public Elevator(int maxFloor){
        currentFloor=0;
        elevatorState= ElevatorState.IDLE;
        gateOpen= false;
        this.maxFloor = maxFloor;
        requests = new HashSet<>();
    }

    private void gateOpen(){
        System.out.println("Gate opening at floor" + currentFloor);
        gateOpen=true;
    }

    private void gateClose(){
        System.out.println("Gate closing at floor" + currentFloor);
        gateOpen=false;
    }
    private void wakeUpElevator(Request request){
        if(currentFloor> request.getFloor())
            elevatorState= ElevatorState.DOWN;
        else elevatorState =ElevatorState.UP;
    }

    public int getCurrentFloor(){
        return  currentFloor;

    }

    public void addRequest(Request request){
        requests.add(request);
        if(elevatorState.equals(ElevatorState.IDLE)){
            wakeUpElevator(request);
        }

    }

    public void step(){
        // if gate is open, close the gate
        if(gateOpen){
            gateClose();
            return;
        }

        //if no request is there for this elevator, mark it as idle
        if(requests.isEmpty()){
            elevatorState= ElevatorState.IDLE;
            return;
        }

        //if current floor is requested destination floor  OR serves a move up, move down
        Request destCall = new Request(currentFloor,RequestState.DESTINATION);
        RequestState hallCallDir = RequestState.DESTINATION;
        if(elevatorState.equals(ElevatorState.UP)){
            hallCallDir= RequestState.MOVE_UP;
        }
        if(elevatorState.equals(ElevatorState.DOWN)){
            hallCallDir=RequestState.MOVE_DOWN;
        }
        Request hallCall = new Request(currentFloor,hallCallDir);

        if(requests.contains(destCall) || requests.contains(hallCall)){
            requests.remove(destCall);
            requests.remove(hallCall);
            gateOpen();
            return;
        }

        if(!hasFurtherRequests()){
            elevatorState = elevatorState.equals(ElevatorState.UP)?ElevatorState.DOWN : ElevatorState.UP;
            return;
        }
        // if going up, go up until you are at top
        if(elevatorState.equals(ElevatorState.UP)){
            currentFloor++;
            return;
        }
        // if going down, go down, until you are at bottom.
        if(elevatorState.equals(ElevatorState.DOWN)){
            currentFloor--;
            return;
        }
    }

    private  Boolean hasFurtherRequests(){
        for(Request request : requests){
            if(elevatorState.equals(ElevatorState.UP) && request.getFloor()>currentFloor)
                return true;
            if(elevatorState.equals(ElevatorState.DOWN) && request.getFloor()<currentFloor)
                return true;
        }

        return  false;
    }

}
