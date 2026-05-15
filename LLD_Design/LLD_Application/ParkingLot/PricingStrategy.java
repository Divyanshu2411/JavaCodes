package LLD_Application.ParkingLot;

import java.math.BigDecimal;

public interface PricingStrategy {
    public BigDecimal calculatePrice(Vehicle vehicle);
}
