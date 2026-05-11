package LLD_Application.Elevator;

import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ElevatorController {
    private List<Elevator> elevatorList;
    @Setter
    private Integer numFloors;
    private BlockingQueue<Request> requests;

    public ElevatorController(int numElevator, int numFloors){
        this.numFloors= numFloors;
        elevatorList = new ArrayList<>();
        requests= new LinkedBlockingQueue<>(1000);
        for(int i=0; i<numElevator; i++){
            elevatorList.add(new Elevator(numFloors));
        }
    }

    private  boolean validRequest(int floor){
        return floor <= numFloors && floor >= 0;
    }

    public Boolean requestElevator(int floor, RequestState requestState){ //consumer
        if(!validRequest(floor)){
            System.out.println("Requested Floor Does Not Exist");
            return  false;
        }
        try{
            requests.put(new Request(floor,requestState));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return  false;
        }

        return  true;
    }

    public void step(){ //producer -> this allows for consumer to just add and move on
        Request currentRequest;
        while((currentRequest=requests.poll())!=null){


            ElevatorStrategy stategy = new ElevatorStategyImpl(new ScanStrategy()); // this allows us to not use any thread safe in elevator as everything is happening like an event loop
            Elevator bestElevator= stategy.findBestElevator(currentRequest,elevatorList);
            bestElevator.addRequest(currentRequest);
        }
        for(Elevator elevator : elevatorList){
            elevator.step();
        }
    }






}
