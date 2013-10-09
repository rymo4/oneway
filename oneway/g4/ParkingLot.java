package oneway.g4;

import java.util.LinkedList;
import java.util.List;

public class ParkingLot {
	private int capacity;
	private LinkedList<Car> leftbound;
	private LinkedList<Car> rightbound;
	
	public ParkingLot(int capacity) {
		this.capacity = capacity;
		this.leftbound = new LinkedList<Car>();
		this.rightbound = new LinkedList<Car>();
	}
	
	public void addCars(List<Car> cars) {
	  for(Car car : cars) {
	    add(car);
	  }
	}

  public void add(Car car) {
    if(car.dir == Direction.LEFT) {
      leftbound.add(car);
    }
    else {
      rightbound.add(car);
    }
  }

  public List<Car> getCars() {
    LinkedList<Car> cars = new LinkedList<Car>();
    cars.addAll(leftbound);
    cars.addAll(rightbound);
    return cars;
  }
  
  public int getLeftCarCount() {
    return leftbound.size();
  }
  
  public int getRightCarCount() {
    return rightbound.size();
  }

  public ParkingLot copy() {
    ParkingLot newLot = new ParkingLot(this.capacity);
    for (Car c : this.leftbound) {
      newLot.leftbound.add(c.copy());
    }
    for (Car c : this.rightbound) {
      newLot.rightbound.add(c.copy());
    }
    return newLot;
  }

  /**
   *
   * @param leftSegment
   * @param rightSegment
   * @return true if this parking lot has overflowed, false otherwise.
   */
  public boolean unparkCars(Segment leftSegment, Segment rightSegment) {
    if (leftSegment != null // There's a left segment 
        && leftbound.size() > 0 // There are parked cars waiting to move left
        && leftSegment.isLeftGreen() // The leftbound light is green
        && leftSegment.firstTwoClear(Direction.LEFT)) { // There is room for another car
      leftSegment.addCarAtPosition(leftbound.remove(), leftSegment.getLength() - 1);
    }
    if (rightSegment != null
        && rightbound.size() > 0
        && rightSegment.isRightGreen()
        && rightSegment.firstTwoClear(Direction.RIGHT)) {
      rightSegment.addCarAtPosition(rightbound.remove(), 0);
    }
    return rightbound.size() + leftbound.size() > capacity;
  }

  public void removeCars(Direction dir, int currentTime, Node n) {
    if (dir == Direction.RIGHT) {
      while (rightbound.size() > 0) {
        Car c = rightbound.remove();
        c.setComplete(currentTime);
        n.allCars.remove(c);
      }
    }
    else {
      while (leftbound.size() > 0) {
        Car c = leftbound.remove();
        c.setComplete(currentTime);
        n.allCars.remove(c);
      }
    }    
  }

  public int getCapacity() {
    return capacity;
  }
}
