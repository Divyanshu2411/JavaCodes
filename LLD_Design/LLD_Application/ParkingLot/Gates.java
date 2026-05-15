package LLD_Application.ParkingLot;

import lombok.Data;

@Data
public class Gates {
    private Integer locationX;
    private Integer locationY;
    Gates(Integer locationX, Integer locatoinY){
        this.locationX=locationX;
        this.locationY=locatoinY;
    }
}
