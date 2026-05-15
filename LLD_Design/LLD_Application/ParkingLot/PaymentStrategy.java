package LLD_Application.ParkingLot;

import java.math.BigDecimal;

public interface PaymentStrategy {
    public  Boolean pay(BigDecimal amount);
}
