package LLD_Application.ParkingLot;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

public class standardPricing implements  PricingStrategy{
    private final HashMap<VehicleType, BigDecimal> vehicleTypeRatePerHour = new HashMap<>();

    {
        vehicleTypeRatePerHour.put(VehicleType.CAR, BigDecimal.valueOf(20.0));
        vehicleTypeRatePerHour.put(VehicleType.BIKE, BigDecimal.valueOf(10.0));
        vehicleTypeRatePerHour.put(VehicleType.TRUCK, BigDecimal.valueOf(50.0));
    }


    @Override
    public BigDecimal calculatePrice(Vehicle vehicle) {
        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration= Duration.between(vehicle.getEntryTime(),currentTime);
        BigDecimal hoursParked = BigDecimal.valueOf(duration.toMinutes()/60.0);

        BigDecimal ratePerHour = vehicleTypeRatePerHour.get(vehicle.getVehicleType());

        if (ratePerHour == null) {
            throw new IllegalArgumentException("No hourly rate defined for " + vehicle.getVehicleType());
        }
        return hoursParked.multiply(ratePerHour);
    }
}
