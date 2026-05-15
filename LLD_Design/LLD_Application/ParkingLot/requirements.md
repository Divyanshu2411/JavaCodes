# Parking Lot

## Requirements
    - multiple floors
    - different type of slots (bike, car, big car etc)
    - multiple entry and exit gates
    - pricing should be dynamic, weekend has different price, regular day has different, rush hours have different
    - you should let the user know nearest empty spot when they enter
    - you should allow user to pay in various ways
    - you should display number of empty spots per floor and overall
    

## Entites

### ParkingSpot - class
    - type : Vehicle Type ENUM
    - floor: int
    - distance : map <gate, distance>
    - locationX : int
    - locationY: int
    - isOccupied: Boolean
    + ParkingSpot()
    + getterSetter
    + Park()

### ParkingFloor - class
    - List<ParkingSpots>
    - floor:int
    - vehicleSpace -> map <vehicleType, availableSpace>
    + ParkingFloor(List<ParkingSpots>)
    + Park(parkingSpot)
    + getAvailableSpots(Vehicle) //changed it so that no synchronisation issue

### ParkingLot -class
    - List<ParkingFloor>
    - List<Gates>
    + ParkingLot(List<ParkingFloor>, List<Gates>)
    + allotPark()

### Vehicle -class
    - type: VehicleType ENUM
    - parkingSpot : ParkingSpot
    - paymentStrategy
    - entryGate: Gate
    - entryTime : long long
    + enterAndPark() : boolean
    + exitAndPay(): boolean

### Gates - class
    - locationX
    - locationY
    
### Pricing - Interface
    - calculatePrice(Vehicle, ParkingSpot)

### Pay - interface