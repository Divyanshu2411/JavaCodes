package LLD_Application.ParkingLot;

import lombok.Data;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ParkingFloor {
    private List<ParkingSpot> parkingSpots;
    private Integer floor;
    private ConcurrentHashMap<VehicleType, AtomicInteger> availableSpace;

    public ParkingFloor(List<ParkingSpot> parkingSpots, Integer floor) {
        this.parkingSpots = parkingSpots;
        this.floor = floor;
        availableSpace = new ConcurrentHashMap<>();

        for (ParkingSpot spot : parkingSpots) {
            AtomicInteger availableVehicleSpot = availableSpace.get(spot.getParkingType());
            if (availableVehicleSpot == null) availableVehicleSpot = new AtomicInteger(0);
            availableVehicleSpot.incrementAndGet();
            availableSpace.put(spot.getParkingType(), availableVehicleSpot);
        }

    }

   public  synchronized  ParkingSpot  parkAtBestSpot(VehicleType vehicleType, Gates gate){
        // no space in floor
        if(availableSpace.get(vehicleType).get()==0) return  null;
        // space is there in floor
       int minDist= Integer.MAX_VALUE;
       ParkingSpot bestSpot=null;
       for(ParkingSpot spot : parkingSpots){
           if(!spot.getIsOccupied() && spot.getParkingType().equals(vehicleType) && spot.getSpotDistance().get(gate)<minDist){
               minDist=spot.getSpotDistance().get(gate);
               bestSpot=spot;
           }
       }
       if(bestSpot !=null){
           bestSpot.park();
           AtomicInteger vehicleFrequency = availableSpace.get(vehicleType);
           vehicleFrequency.decrementAndGet();
           availableSpace.put(vehicleType,vehicleFrequency);

       }
       return bestSpot;
    }

    public synchronized void unPark(Vehicle vehicle){
        ParkingSpot parkingSpot = vehicle.getParkingSpot();
        VehicleType vehicleType = vehicle.getVehicleType();
        AtomicInteger vehicleFreq = availableSpace.get(vehicleType);
        vehicleFreq.incrementAndGet();
        availableSpace.put(vehicleType,vehicleFreq);
        parkingSpot.unPark();
    }


}


