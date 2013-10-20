package oneway.g4;
import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;

/*
 * A class representing one state of the game - meaning some combination of
 * car positions and light settings. This class should handle interfacing between
 * oneway.g4 package representation of the states, and oneway.sim representation
 * of game states.
*/

public class Node implements Comparable<Node> {
  public Segment[] segments;
  private ParkingLot[] lots;
  public ArrayList<Car> allCars;
  public Node parent = null;
  private int currentTime;
  private int m = 0;
  public int carsFinished = 0;
  public int depth = 0;
  private double NOT_SET = Double.MAX_VALUE;
  private double memoizedF = NOT_SET;

  // Initialize a Node from Simulator information
  public Node(int time, int nSegments, int[] lengths, MovingCar[] movingCars,
    Parking[] left, Parking[] right, int[] capacities, boolean[] llights,
    boolean[] rlights){

    currentTime = time;

    // Initialize the allCars array
    allCars = new ArrayList<Car>();

    // Create proper number of parking lots and fill with cars
    lots = new ParkingLot[nSegments+1];
    for (int i = 0; i <= nSegments; i++) {
      lots[i] = new ParkingLot(capacities[i]);
      addCarsToParkingLot(left[i], lots[i], Direction.LEFT);
      addCarsToParkingLot(right[i], lots[i], Direction.RIGHT);
      allCars.addAll(lots[i].getCars());
    }

    // Create proper number of segments
    segments = new Segment[nSegments];
    for (int i = 0; i < nSegments; i++) {
      segments[i] = new Segment(lengths[i], llights[i], rlights[i]);
      m += lengths[i];
    }
    // Decrement m by 1 because if a car is born facing a green light it starts
    // on that road segment instead of in the parking lot.
    m--;

    // Place the movingCars on the segments
    for(MovingCar movingCar : movingCars) {
      Direction dir = movingCar.dir > 0 ? Direction.RIGHT : Direction.LEFT;
      Segment segment = segments[movingCar.segment];
      Car car = new Car(movingCar.startTime, dir);

      allCars.add(car);
      segment.addCarAtPosition(car, movingCar.block);
    }
  }

  private Node(Node node) {
    currentTime = node.currentTime;
    m = node.m;
    allCars = new ArrayList<Car>();

    // Copy the parking lots and add their cars the arrays
    lots = new ParkingLot[node.lots.length];
    for (int i = 0; i < lots.length; i++) {
      lots[i] = node.lots[i].copy();
      List<Car> cars = lots[i].getCars();
      for (Car c : cars) {
        allCars.add(c);
      }
    }

    segments = new Segment[node.segments.length];
    for (int i = 0; i < segments.length; i++) {
      segments[i] = node.segments[i].copy();
      List<Car> cars = segments[i].getCars();
      for (Car c : cars) {
        allCars.add(c);
      }
    }

    for (Car c : node.allCars) {
      if (c.isComplete()) {
        allCars.add(c);
      }
    }
    
    carsFinished = node.carsFinished;
  }

  private void addCarsToParkingLot(List<Integer> cars, ParkingLot lot, Direction dir) {
    if (cars == null) { return; }
    for (Integer carStartTime : cars) {
      Car car = new Car(carStartTime, dir);
      allCars.add(car);
      lot.add(car);
    }
  }
  public ArrayList<Node> successors() {
    ArrayList<Node> children = new ArrayList<Node>();

    boolean carsExitingLeft  = segments[0].anyCarsInDir(Direction.LEFT);
    boolean carsExitingRight = segments[segments.length-1].anyCarsInDir(Direction.RIGHT);
    // detect that we prob will swap next turn, so make ends red
    if (lots.length > 2){
      carsExitingLeft = carsExitingLeft || !segments[1].firstClear(Direction.RIGHT);
      carsExitingRight = carsExitingRight || !segments[segments.length-2].firstClear(Direction.LEFT);
    }
//    System.out.println("Exiting cars: " + carsExitingLeft + ", " + carsExitingRight);

    int numLights = segments.length * 2;
    
    // true lightbits are those that matter, false bits are those that don't
    // we want to set bits that don't matter to green
    // 1 is green, 0 is red
    // Therefore, we want all true lightbits to not be masked, and false ones to be masked to one.
    boolean[] lightbits = new boolean[segments.length * 2];
    // Generate a bit mask for paths that are irrelevant
    for (int i = 0; i < segments.length; i++) {
      Car leftSegmentRightboundCar = null;
      Car rightSegmentLeftboundCar = null;
      if (i > 0) {
        Car rightBoundCar = segments[i-1].getCarsByLocation()[segments[i-1].getLength() - 1];
        if (rightBoundCar != null && rightBoundCar.dir == Direction.RIGHT) {
          leftSegmentRightboundCar = rightBoundCar; 
        }
      }
      if (i < segments.length - 1) {
        Car leftBoundCar = segments[i + 1].getCarsByLocation()[0];
        if (leftBoundCar != null && leftBoundCar.dir == Direction.LEFT) {
          rightSegmentLeftboundCar = leftBoundCar;
        }
      }
      boolean hasCarsComingFromLeft = lots[i].getRightCarCount() > 0 || leftSegmentRightboundCar != null;
      boolean hasCarsComingFromRight = lots[i+1].getLeftCarCount() > 0 || rightSegmentLeftboundCar != null;
      lightbits[i+segments.length] = hasCarsComingFromRight;
      lightbits[i] = hasCarsComingFromLeft;
    }
    
    
    int bitMask = 0;
    for (int i = 0; i < lightbits.length; i++) {
      bitMask = (bitMask << 1) + (lightbits[i] ? 0 : 1);
    }
//    System.out.println("Lightbits: " + Arrays.toString(lightbits));
//    System.out.println("bitMask: " + Integer.toBinaryString(bitMask));
    
    // max is the maximum number of light permutations
    int max = (int) Math.pow(2, numLights);
    HashSet<Integer> combinations = new HashSet<Integer>();
    
    // Filter edge cases
    int edgeMask = max - 1;
    if (carsExitingLeft) { edgeMask -= Math.pow(2, numLights - 1); }
    if (carsExitingRight) { edgeMask -= 1; }
//    System.out.println(Integer.toBinaryString(edgeMask));

    // Use a bit vector to find different permutations of lights
    for(int i = 0; i < max; i++) {
      int binaryLightRepresentation = i;
      binaryLightRepresentation = binaryLightRepresentation | bitMask;
      binaryLightRepresentation = binaryLightRepresentation & edgeMask;
      combinations.add(binaryLightRepresentation);
    }
    
    //Go through all combinations
    for (Integer representation : combinations) {
//      System.out.println("This combination: " + Integer.toBinaryString(representation));
      int binaryLightRepresentation = representation;
      boolean[] lights = new boolean[segments.length * 2];
      for (int j = lights.length - 1; j >= 0; j--) {
        lights[j] = binaryLightRepresentation % 2 == 1;
        binaryLightRepresentation = binaryLightRepresentation >> 1;
      }

      //Create the child, test it out, and keep it if its good
      Node child = new Node(this);
      child.setLights(lights);
//      System.out.println("These lights were used: " + Arrays.toString(lights));
      child.parent = this;
      boolean turnSafe = !child.playTurn();
      boolean futureCrashSafe = child.noFutureCrashes();
      boolean futureOverflowSafe = child.noFutureOverflows();
      if(turnSafe && futureCrashSafe && futureOverflowSafe) {
        child.parent = this;
        children.add(child);
//        System.out.println("No crash detected.");
      }
      else {
//        System.out.println(turnSafe + ", " + futureCrashSafe + ", " + futureOverflowSafe);
      }
//      System.out.println();
    }
//    System.out.println("Generated " + children.size() + " children.");
    return children;
  }

  private void setLights(boolean[] lights) {
    int nSegments = segments.length;
    for(int i = 0; i < nSegments; i++) {
      //System.out.print(" l:" + lights[i]);
      //System.out.print(" r:" + lights[i+nSegments]);
      segments[i].setLight(Direction.RIGHT, lights[i]);
      segments[i].setLight(Direction.LEFT, lights[i+nSegments]);
    }
    //System.out.println();
  }

  // return false if overflows are guaranteed in the future
  public boolean noFutureOverflows() {

    for(int i = 1; i<lots.length-1; i++)
    {
      int lcount = 0,rcount = 0;
      Car[] cl = new Car[segments[i-1].getLength()];
      Car[] cr = new Car[segments[i].getLength()];

      cl = segments[i-1].getCarsByLocation();
      cr = segments[i].getCarsByLocation();

      for(int j =0; j < cl.length; j++) {
        if(cl[j]!=null && cl[j].isRightbound()) {
          lcount++;
        }
      }

      for(int k =0; k < cr.length; k++) {
        if(cr[k]!=null && cr[k].isLeftbound()) {
          rcount++;
        }
      }


      if(lots[i].getLeftCarCount() + lots[i].getRightCarCount() + rcount > lots[i].getCapacity() && lcount > 0 ||
        lots[i].getLeftCarCount() + lots[i].getRightCarCount() + lcount > lots[i].getCapacity() && rcount > 0  ||
        lots[i].getLeftCarCount() + lots[i].getRightCarCount() + lcount + rcount > lots[i].getCapacity() && (rcount > 0 && lcount > 0) ){
        return false;
      }
    }

    return true;
  }

  // return false if crashes are guaranteed in the future
  public boolean noFutureCrashes() {
    for(int i = 0;i < segments.length; i++) {
      boolean left = false, right = false;
      Car[] c = new Car[segments[i].getLength()];
      c = segments[i].getCarsByLocation();

      if (i > 0 && i < segments.length - 1) {
        Car incomingLeftboundCar = segments[i+1].getCarsByLocation()[0];
        Car incomingRightboundCar = segments[i-1].getCarsByLocation()[segments[i-1].getLength() - 1];
        if(segments[i].isLeftGreen() &&
            (lots[i+1].getLeftCarCount() > 0
                || incomingLeftboundCar != null
                && incomingLeftboundCar.dir == Direction.LEFT)
            && segments[i].isRightGreen() &&
              (lots[i].getRightCarCount() > 0)) {
          return false;
        }
      }
      for(int j =0; j < c.length; j++) {
        if(c[j] != null && c[j].isRightbound()) {
            right = true;
        }
      }
      for(int k =0; k < c.length; k++) {
        if(c[k] != null && c[k].isLeftbound()) { 
            left = true;
        }
      }


      if(right && left)
        return false;

    }
    return true;

  }

  /**
   * Plays out a single turn on the current node.
   * @return Whether or not the result of playing the turn results in overflow or crash
   */
  private boolean playTurn() {
    currentTime += 1;
    for (Car c : allCars) {
      c.updateLatency(currentTime);
    }

    boolean fail = false;
    for (int i = 0; i < segments.length; i++) {
      fail = fail || segments[i].moveCarsForward(lots[i], lots[i+1]);
    }
    for (int i = 0; i < lots.length; i++) {
      Segment leftSegment = i > 0 ? segments[i-1] : null;
      Segment rightSegment = i < segments.length ? segments[i] : null;
      fail = fail || lots[i].unparkCars(leftSegment, rightSegment);
    }
    lots[0].removeCars(Direction.LEFT, currentTime, this);
    lots[lots.length-1].removeCars(Direction.RIGHT, currentTime, this);
    return fail;
  }

  public void printNode() {
    System.out.println("{F: " + f() + "}");
    System.out.print("\t");
    for (Car c : allCars) {
      System.out.print("{" + c.g + ", " + c.h + "} ");
    }
    System.out.println();
    System.out.println("\tAll cars: " + allCars.size() + " Completed Cars: " + carsFinished);
    System.out.print("\t");
    for(int i = 0; i < lots.length; i++) {
      System.out.print("{<<" + lots[i].getLeftCarCount() + "-" + lots[i].getRightCarCount() + ">>}");
      if (i == lots.length - 1) { break; }
      System.out.print("{" + Arrays.toString(segments[i].getCarsByLocation()) + "}");
    }
    System.out.println();
  }

  public double g(){
    fillG();
    double cost = 0.0;
    for (Car c: allCars){
      cost += cost(c.g);
    }
    return cost;
  }

  public double f() {
    if (memoizedF != NOT_SET){
      return memoizedF;
    }
    fillG();
    fillH();
    double cost = 0.0;
    for (Car c: allCars){
      cost += cost(c.g + c.h);
    }
    // This is a hack to favor greens on the ends. Because we don't
    // generate states where incoming cars will crash, we should always favor
    // states with greens on the sides
    if (segments[segments.length - 1].isLeftGreen()) cost -= 0.1;
    if (segments[0].isRightGreen()) cost -= 0.1;
    //System.out.println("g: " + g + ", h: " + h);
    memoizedF = cost;
    return cost;
  }

  // Path cost until this point
  public void fillG() {
    double cost = 0.0;
    // Sum cost of each car
    for (Car car : allCars) {
      int latency = car.getLatency();
      car.g = latency;
    }
  }

  private void fillH() {
    int totalDistance = m + 1;
    int partDistance = 0;

    for (int i = 0; i < lots.length; i++) {
      int leftboundWaitTime = 0;
      int rightboundWaitTime = 0;
      // Calculate the expected cost of the segment
      // Skip segment calculation if at index 0.
      if (i != 0) {
        Segment s = segments[i-1];
        Car[] cars = s.getCarsByLocation();
        leftboundWaitTime = 0;
        rightboundWaitTime = 0;
        for (int segDistance = 0; segDistance < cars.length; segDistance++) {
          if (cars[segDistance] != null) {
            int expectedFinish;
            if (cars[segDistance].dir == Direction.LEFT) {
              leftboundWaitTime = partDistance;
              expectedFinish = currentTime + segDistance + partDistance;
            }
            else {
              if (rightboundWaitTime == 0) { rightboundWaitTime = segDistance; } 
              expectedFinish = currentTime + (totalDistance - partDistance - (segDistance + 1));
            }
            cars[segDistance].h = (expectedFinish - currentTime + 1);
          }
        }
        partDistance += s.getLength();
        
        // Add incoming car information to previous parking lot
        for(Car c : lots[i-1].getRightCars()) {
          c.h += leftboundWaitTime;
        }
      }
      // Calculate the expected cost of the lot
      ParkingLot l = lots[i];
      int carsAhead = 0;
      for (Car c : l.getLeftCars()) {
        int expectedFinish;
        expectedFinish = currentTime + partDistance + carsAhead * 2 + rightboundWaitTime;
        c.h = (expectedFinish - currentTime + 1);
        carsAhead++;
      }
      carsAhead = 0;
      for (Car c : l.getRightCars()) {
        int expectedFinish;
        expectedFinish = currentTime + (totalDistance - partDistance) + carsAhead * 2;
        c.h = (expectedFinish - currentTime + 1);
        carsAhead++;
      }
    }
  }

  private double cost(double latency) {
    return (latency * Math.log10(latency)) - (((double) m) * Math.log10(m));
  }

  @Override
  public int compareTo(Node other) {
    return (int) Math.signum(this.f() - other.f());
  }

  public boolean[] getLLights() {
    boolean[] llights = new boolean[segments.length];
    for(int i = 0; i < llights.length; i++) {
      llights[i] = segments[i].isLeftGreen();
    }
    return llights;
  }

  public boolean[] getRLights() {
    boolean[] rlights = new boolean[segments.length];
    for(int i = 0; i < rlights.length; i++) {
      rlights[i] = segments[i].isRightGreen();
    }
    return rlights;
  }
  
  public int getCurrentTime() {
    return currentTime;
  }
}
