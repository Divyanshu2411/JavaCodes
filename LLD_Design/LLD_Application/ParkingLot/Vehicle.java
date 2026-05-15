package LLD_Application.ParkingLot;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Vehicle implements PaymentStrategy {
    private VehicleType vehicleType;
    private ParkingSpot parkingSpot;
    private Gates entryGate;
    private LocalDateTime entryTime;
    private  PaymentStrategy paymentStrategy;

    public void enterAndPark(ParkingLot parkingLot){
        parkingSpot=parkingLot.allotPark(this);
    }
    public void exitAndPay(ParkingLot parkingLot){
        BigDecimal amount = parkingLot.calculatePrice(this);

        try{
            pay(amount);
            parkingLot.unAllortPark(this);
        }
        catch (Exception ex){
            System.out.println("Payment Failed, try another method");
        }

    }

    @Override
    public Boolean pay(BigDecimal amount) {
        System.out.println("Payment through Card");
        return true;
    }


}
