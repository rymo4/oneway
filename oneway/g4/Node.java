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

public class Node {
  private static final int LENGTHS_PER_SECOND = 4;
  private Segment[] segments;
  private ParkingLot[] lots;
  private ArrayList<Car> allCars;
  private Node parent = null;
  private int currentTime;
  
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
      addCarsToParkingLot(left[i], lots[i]);
      addCarsToParkingLot(right[i], lots[i]);
    }
    
    // Create proper number of segments
    segments = new Segment[nSegments];
    for (int i = 0; i < nSegments; i++) {
      segments[i] = new Segment(lengths[i], llights[i], rlights[i]);
    }
    
    // Place the movingCars on the segments
    for(MovingCar movingCar : movingCars) {
      Direction dir = movingCar.dir > 0 ? Direction.RIGHT : Direction.LEFT;
      Segment segment = segments[movingCar.segment];
      Car car = new Car(movingCar.startTime, dir);
      
      allCars.add(car);
      segment.addCarAtPosition(car, movingCar.block);
    }
  }
  
  private void addCarsToParkingLot(List<Integer> cars, ParkingLot lot) {
    for (Integer carStartTime : cars) {
      Car car = new Car(carStartTime, Direction.LEFT);
      allCars.add(car);
      lot.add(car);
    }
  }

  public ArrayList<Node> successors() {
    int newTime = currentTime + 1;
    ArrayList<Node> children = new ArrayList<Node>();
    // TODO: Find all the states
    return children;
  }

  public double f() {
    return g() + h();
  }

  // Path cost until this point
  private double g() {
    double cost = 0.0;
    double m = 0.0;
    for (Segment segment : segments) {
      m += (double) segment.getLength() / LENGTHS_PER_SECOND;
    }

    // Sum cost of each car
    for (Car car : allCars) {
      int l = currentTime - car.startTime;
      cost += (l * Math.log10(l)) - (m * Math.log10(m));
    }

    return cost;
  }

  // Estimate cost from here to the end
  // Maybe use a feature vector of different heuristics and later train
  // to get optimal weights
  private double h() {
    // TODO: Make this
    return 10000;
  }

  // Returns something like this:
  //   left lights  right lights
  // [[true false], [false true]]
  // This is what the Player class will call to figure out what to do
  public static boolean[][] bestSetting(Node root, int numLights){
    int LEFT = 0;
    int RIGHT = 1;
    boolean[][] lights = new boolean[2][numLights];

    // TODO: Do some A* and fill lights[LEFT] and lights[RIGHT] with
    // what you want them to be.
    return lights;
  }
}
