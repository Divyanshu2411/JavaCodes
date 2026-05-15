package LLD_Application.ParkingLot;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ParkingLot  implements  PricingStrategy{
    private List<ParkingFloor> parkingFloorList;
    private List<Gates> gatesList;
    private PricingStrategy pricingStrategy;

    public ParkingSpot allotPark(Vehicle vehicle){
        for(ParkingFloor parkingFloor : parkingFloorList){
            if(parkingFloor.parkAtBestSpot(vehicle.getVehicleType(),vehicle.getEntryGate())!=null){
                return parkingFloor.parkAtBestSpot(vehicle.getVehicleType(),vehicle.getEntryGate());
            }
        }
        return  null;
    }

    public void unAllortPark(Vehicle vehicle){
        ParkingSpot parkingSpot = vehicle.getParkingSpot();
        Integer floor = parkingSpot.getFloor();
        ParkingFloor parkingFloor =null;
        for(ParkingFloor floor1 : parkingFloorList){
            if(floor1.getFloor().equals(floor)){
                parkingFloor=floor1;
                break;
            }
        }

        assert parkingFloor != null;
        parkingFloor.unPark(vehicle);
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy){
        this.pricingStrategy = pricingStrategy;
    }

    @Override
    public BigDecimal calculatePrice(Vehicle vehicle) {
        return pricingStrategy.calculatePrice(vehicle);
    }
}
