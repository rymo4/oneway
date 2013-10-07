package oneway.g4;

public class Segment {
  boolean leftGreen = false;
  boolean rightGreen = false;

  Car[] carsByLocation;

  public Segment(int size, boolean leftGreen, boolean rightGreen) {
    carsByLocation = new Car[size];
    this.leftGreen = leftGreen;
    this.rightGreen = rightGreen;
  }
  
  public int getLength() {
    return carsByLocation.length;
  }

  public void addCarAtPosition(Car car, int block) {
    carsByLocation[block] = car;
  }
}
