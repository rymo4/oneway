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

  public Node best(Node root){
    open.add(root);

    while (open.size() != 0){
      Node n = open.poll();

      if (n.f() < bestScoreSoFar){
        bestSoFar = n;
        bestScoreSoFar = n.f();
        System.out.println();
        bestSoFar.printNode();
      }
//      System.out.println(n.allCars.size());
//
//      if (outOfTime()) return firstStepInPath(bestSoFar);

      if (isGoal(n)) {
        System.out.println("Found Goal");
        System.out.println(n.allCars.size());
        n.printNode();
        return firstStepInPath(n);
      }
      ArrayList<Node> children = n.successors();
      //System.out.println("Open: " + open.size() + " closed: " + closed.size() + " branch: " + children.size());
      for (int i = 0; i < children.size(); i++){
        Node child = children.get(i);
        // TODO: Keep mirrored set of open to have fast lookups
        boolean in_closed = closed.contains(child);
        boolean in_open   = openCopy.contains(child);
        if (in_closed || in_open){
          if (n.g() <= child.g()) continue;
        }

        child.parent = n;

        if (in_closed){
          closed.remove(child);
        }
        if (!in_open){
          open.add(child);
          openCopy.add(child);
        }
      }
      closed.add(n);
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
      n = n.parent;
      lastN = n;
    }
    return lastN;
  }
}
