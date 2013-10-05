package oneway.g4;
import oneway.sim.MovingCar;
import oneway.sim.Parking;
import java.util.*;

/*
 * A class representing one state of the game - meaning some combination of
 * car positions and light settings.
*/

public class Node {
  private MovingCar[] movingCars;
  private Parking[] left;
  private Parking[] right;
  private boolean[] llights;
  private boolean[] rlights;
  // Number of time ticks so far
  private int currentTime;
  private int numSegments;

  public Node(int time, int nsegments, MovingCar[] movingCars,
      Parking[] left, Parking[] right, boolean[] llights, boolean[] rlights){
    movingCars = movingCars;
    left = left;
    right = right;
    llights = llights;
    rlights = rlights;
    currentTime = time;
    numSegments = nsegments;
  }

  public ArrayList<Node> successors() {
    int newTime = currentTime + 1;
    ArrayList<Node> children = new ArrayList<Node>();
    // Find all the states
    return children;
  }

  public double f() {
    return g() + h();
  }

  // Path cost until this point
  private double g() {
    double cost = 0.0;
    double m = (double) numSegments / 20;

    // Sum the moving car costs
    for (MovingCar car : movingCars){
      int L = currentTime - car.startTime;
      // Should this be base 2 10 or e?
      cost += (L * Math.log10(L)) - (m * Math.log10(m));
    }
    // TODO: Have to figure out how to get the cost for parked cars

    return cost;
  }

  // Estimate cost from here to the end
  // Maybe use a feature vector of different heuristics and later train
  // to get optimal weights
  private double h() {
    return 10000;
  }
}
