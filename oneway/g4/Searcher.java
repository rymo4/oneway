package oneway.g4;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Searcher {

  private long startTime = System.currentTimeMillis();

  // Find a good initial capacity later
  private PriorityBlockingQueue<Node> open = new PriorityBlockingQueue<Node>();
  private Set<Node> closed = new HashSet<Node>();
  private Set<Node> openCopy = new HashSet<Node>();
  private Node bestSoFar;
  private double bestScoreSoFar = Double.MAX_VALUE;
  private int bestTime = 0;

  public Node best(Node root){
    open.add(root);

    while (open.size() != 0){
      Node n = open.poll();
//      n.printNode();

//      System.out.println(open.size());
      if (n.f() < bestScoreSoFar && n.getCurrentTime() > bestTime){
        bestSoFar = n;
        bestScoreSoFar = n.f();
        bestTime = n.getCurrentTime();
      }
//      System.out.println(n.allCars.size());
//
//      if (outOfTime()) return firstStepInPath(bestSoFar);

      if (isGoal(n)) {
        System.out.println("Found Goal");
        System.out.println(n.allCars.size());
        return firstStepInPath(n);
      }
      
      ArrayList<Node> children = n.successors();
      while (children.size() < 2) {
        if (isGoal(n)) { return firstStepInPath(n); }
        n = children.get(0);
        children = n.successors();
      }
      System.out.println(children.size());
      System.out.println(n.getCurrentTime());
      for (int i = 0; i < children.size(); i++){
        Node child = children.get(i);
        child.parent = n;
        open.add(child);
      }
      
    }
    return null;
  }

  private boolean outOfTime(){
    return System.currentTimeMillis() - startTime >= 1000;
  }

  private boolean isGoal(Node n){
    return n.allCars.size() == n.carsFinished;
  }

  // x -> x -> x -> x
  private Node firstStepInPath(Node n){
    Node lastN = n;
    while (n.parent != null && n.parent.parent != null){
      if (n.parent.parent == null) {
        return n;
      }
//      n.printNode();
      n = n.parent;
      lastN = n;
    }
    return lastN;
  }
}
