# Learning
- For simulations where time sort of matters (elevator) keep a function simply to simulate it
- You don't have to have all the functions during class diagram
  - Just scan the requirement, ensure all the functionality is covered at higher level
  - Defer the all other helper/private functions to implementation phase
- AGAIN, DO NOT TRY TO WRITE ALL THE FUNCTIONS DURING CLASS DIAGRAM, ONLY WHAT IS IN REQUIREMENT


## Requirements
    
1. Elevator needs to go between multiple floors of a building
2. While chosing elevator, ensure the choice is efficient.
3. Number of floors and elevators are configurable
4. Should support hall call  (call from outside) and destination call (call from inside)
5. Support up/down in hall call (no numpad) -> note that this can be a strategy to swap later
6. Should not open at same floors again and again and if dest and current floor is same, no action


### ElevatorController - class
 ```
    - list of elevator
    - number of floors
    - list of Requests //this can be part of elevator or elevator class both
    
    +ElevatorController(int numFloor, int numElevator)    
    + addElevator();
    + removeElevator();
    + setNumberOfFloor(int numFloor);
    + requestElevator(int floor,dir) - return Elevator
    + step(int n) -> to simulate time passing -> n = number of steps
    
       
 ```

### Elevator - class
```
    - currentFloor -> int
    - state - ElevatorState
    - gateOpen - boolean
    - Set of Requests
    + Elevator()
    + getCurrentFloor()
    + getState()
    + step() -> to simulate every step/move
    + gateOpen() 
    + gateClose()
    + 

```

### ElevatorState
```
    UP, 
    DOWN, 
    IDLE, 
    STOP // some issue, out of action
```

### RequestState
```
    UP, 
    DOWN, 
    DESTINATION
```

### REQUEST
```
- direction - RequestState 
- floor - int
+ getFloor()
+ getDirection()
```


## Learning
 - SCAN strategy -> always look ahead if us direction me kuch hai ki nahi and then move
 - the tick/step simulation (it gets run by a thread in the bg) while user input are kept separately, look at ElevatorTest
 - Both best elevator selection and elevator movement itself can be strategy
 - Don't overcomplicate with patterns as of now
 - Requirements dekho, find noun for classes, and verb for functions, create candidates at first, don't commit
 - See if the noun has any state or just a variable of sort
 - any simulation related, step is really good to move time ahead, probably can be used in all game/parking/elevator
 - treat ENUMS like state, if a field is of some type ENUM, all ENUM states should be valid
   - that's why we had to keep elevator state and request state enum separte as request can't be idle or stop
