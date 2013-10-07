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
}
