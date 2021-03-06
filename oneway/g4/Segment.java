package oneway.g4;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Segment {
  private boolean leftGreen = false;
  private boolean rightGreen = false;

  Car[] carsByLocation;

  public Segment(int size, boolean leftGreen, boolean rightGreen) {
    carsByLocation = new Car[size];
    this.leftGreen = leftGreen;
    this.rightGreen = rightGreen;
  }
  
  public int getLength() {
    return carsByLocation.length;
  }
  
  public boolean isLeftGreen() {
    return leftGreen;
  }

  public boolean isRightGreen() {
    return rightGreen;
  }

  public void addCarAtPosition(Car car, int block) {
    carsByLocation[block] = car;
  }

  public Car[] getCarsByLocation() {
    return Arrays.copyOf(carsByLocation, carsByLocation.length);
  }

  public List<Car> getCars() {
    List<Car> cars = new LinkedList<Car>();
    for (Car c : carsByLocation) {
      if (c != null) {
        cars.add(c);
      }
    }
    return cars;
  }

  public boolean anyCarsInDir(Direction dir){
    for (Car c : getCars()){
      if (c.dir == dir) return true;
    }
    return false;
  }
  
  public Segment copy() {
    Segment segment = new Segment(carsByLocation.length, leftGreen, rightGreen);
    for (int i = 0; i < carsByLocation.length; i++) {
      Car carToAdd = carsByLocation[i];
      if (carsByLocation[i] != null) {
        segment.addCarAtPosition(carToAdd.copy(), i);
      }
    }
    return segment;
  }

  /**
   * 
   * @param parkingLot
   * @param parkingLot2
   * @return true if there has been an accident, false otherwise
   */
  public boolean moveCarsForward(ParkingLot leftLot, ParkingLot rightLot) {
    for (int i = 0; i < carsByLocation.length; i++) {
      Car car = carsByLocation[i];
      if (car != null) {
        if (car.isRightbound()) {
          if (i == carsByLocation.length - 1) {
            rightLot.add(car);
            carsByLocation[i] = null;
          }
          else if ((i + 1 < carsByLocation.length 
              && carsByLocation[i+1] != null 
              && !carsByLocation[i+1].isRightbound())
              || (i + 2 < carsByLocation.length
              && carsByLocation[i+2] != null
              && !carsByLocation[i+2].isRightbound())
              ) { 
            return true; 
          }
          else {
            carsByLocation[i+1] = car;
            carsByLocation[i] = null;
            i++;
          }
        }
        else {
          if (i == 0) {
            leftLot.add(car);
          }
          else if ((i - 1 >= 0 
              && carsByLocation[i-1] != null 
              && carsByLocation[i-1].isRightbound())
              || (i - 2 >= 0
              && carsByLocation[i-2] != null
              && carsByLocation[i-2].isRightbound())) {
            return true;
          }
          else {
            carsByLocation[i-1] = car;
          }
          carsByLocation[i] = null;
        }
      }
    }
    return false;
  }

  public boolean firstClear(Direction dir) {
    boolean firstEmpty;
    if (dir == Direction.LEFT) {
      firstEmpty = carsByLocation[getLength() - 1] == null;
    }
    else {
      firstEmpty = carsByLocation[0] == null;
    }
    return firstEmpty;
  }

  public boolean firstTwoClear(Direction dir) {
    boolean firstEmpty;
    boolean secondEmpty;
    if (dir == Direction.LEFT) {
      firstEmpty = carsByLocation[getLength() - 1] == null;
      secondEmpty = carsByLocation[getLength() - 2] == null;
      
    }
    else {
      firstEmpty = carsByLocation[0] == null;
      secondEmpty = carsByLocation[1] == null;
    }
    return firstEmpty && secondEmpty;
  }

  public void setLight(Direction light, boolean green) {
    if (light == Direction.LEFT) {
      leftGreen = green;
    }
    else {
      rightGreen = green;
    }
    
  }
}
