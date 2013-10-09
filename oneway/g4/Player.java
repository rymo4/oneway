package oneway.g4;

import oneway.sim.MovingCar;
import oneway.sim.Parking;

import java.util.*;

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
      Node node = new Node(currentTime, nsegments, nblocks, movingCars, 
          left, right, capacity, llights, rlights);

      Node choice = new Searcher().best(node);

      boolean[] newLLights = choice.getLLights();
      boolean[] newRLights = choice.getRLights();
      for(int i = 0; i < nsegments; i++) {
        llights[i] = newLLights[i];
        rlights[i] = newRLights[i];
      }
    }


    private int nsegments;
    private int[] nblocks;
    private int[] capacity;
}
