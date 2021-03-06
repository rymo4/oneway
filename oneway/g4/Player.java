package oneway.g4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

public class Player extends oneway.sim.Player
{
    private int currentTime = -1;

    public Player() {}

    public void init(int nsegments, int[] nblocks, int[] capacity)
    {
      this.nsegments = nsegments;
      this.nblocks = nblocks;
      this.capacity = capacity.clone();
    }


    public void setLights(MovingCar[] movingCars,
                          Parking[] left,
                          Parking[] right,
                          boolean[] llights,
                          boolean[] rlights)
    {
      currentTime++;
      
      // Strategy 0: On the first turn, just let cars come in since search will
      // instantly terminate
      boolean moreThanOneRoad = left.length > 2;
      if (moreThanOneRoad && movingCars.length == 0 && !anyParkedCars(left, right) && capacity[1] > 0){
        System.out.println("First turn with 2+ segments, so lettings cars in.");
        llights[llights.length-1] = true;
        rlights[0] = true;
        return;
      }
      
      Node node = new Node(currentTime, nsegments, nblocks, movingCars, 
          left, right, capacity, llights, rlights);
//      Node choice = new Searcher().best(node);
      
      List<Node> children = node.successors();
      Collections.sort(children);
      if (children.size() == 0) {
        System.out.println("No nodes generated");
        for(int i = 0; i < nsegments; i++) {
          if (!node.segments[i].anyCarsInDir(Direction.RIGHT)){
            llights[i] = true;
          } else {
            llights[i] = false;
          }
          rlights[i] = false;
        }
        return;
      }
      Node choice = children.get(0);
      System.out.println("noFutureCrashes: " + choice.noFutureCrashes() + " noFutureOverflows: " + choice.noFutureOverflows());

      if (choice == null || choice == node) {
        // This is the DEADLOCK case
        // Escape Strategy: set all greens in direction that has most cars morving in it
        // and all red is the other direction
        int leftCount = 0;
        int rightCount = 0;
        for (Car c : node.allCars){
          if (c.isRightbound()){
            rightCount++;
          } else {
            leftCount++;
          }
        }

        boolean leftVal = leftCount > rightCount;
        boolean rightVal = !leftVal;
        for(int i = 0; i < nsegments; i++) {
          llights[i] = leftVal;
          rlights[i] = rightVal;
        }

        return;
      }

      boolean[] newLLights = choice.getLLights();
      boolean[] newRLights = choice.getRLights();
      for(int i = 0; i < nsegments; i++) {
        llights[i] = newLLights[i];
        rlights[i] = newRLights[i];
      }
    }

    private boolean anyParkedCars(Parking[] left, Parking[] right){
      for (int i = 0; i < left.length; i++){
        if (left[i] != null && left[i].size() != 0) return true;
        if (right[i] != null && right[i].size() != 0) return true;
      }
      return false;
    }


    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
