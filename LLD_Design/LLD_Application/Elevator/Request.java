package LLD_Application.Elevator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @EqualsAndHashCode
public class Request {
    private int floor;
    private RequestState requestState;
    Request(int floor, RequestState requestState){
        this.floor= floor;
        this.requestState= requestState;
    }

}
