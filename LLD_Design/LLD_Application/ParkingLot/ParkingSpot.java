package LLD_Application.ParkingLot;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class ParkingSpot {
    private VehicleType parkingType;
    private Integer floor;
    private HashMap<Gates, Integer> spotDistance;
    private Integer locationX;
    private Integer locationY;
    private volatile Boolean isOccupied;

    ParkingSpot(VehicleType parkingType, Integer floor, Integer locationX, Integer locationY, List<Gates> gates){
        this.parkingType = parkingType;
        this.floor = floor;
        spotDistance = new HashMap<>();
        this.locationX = locationX;
        this.locationY= locationY;
        isOccupied = false;
        for(Gates gate : gates)
            spotDistance.put(gate,gate.getLocationX()-this.locationX + gate.getLocationY()-this.locationY);
    }

    public void park(){
        isOccupied=true;
    }
    public  void unPark(){
        isOccupied=false;
    }
}
