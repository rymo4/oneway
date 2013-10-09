package oneway.g4;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Searcher {

  private static long startTime = System.currentTimeMillis();

  // Find a good initial capacity later
  private static PriorityBlockingQueue<Node> open = new PriorityBlockingQueue<Node>();
  private static Set<Node> closed = new HashSet<Node>();

  public static Node best(Node root){
    open.add(root);

    while (open.size() != 0){
      Node n = open.poll();
      System.out.println(n.allCars.size());

      if (isGoal(n)) {
        System.out.println("FUCK YESH");
        return firstStepInPath(n);
      }
      ArrayList<Node> children = n.successors();
      System.out.println("Open: " + open.size() + " closed: " + closed.size() + " branch: " + children.size());
      for (int i = 0; i < children.size(); i++){
        Node child = children.get(i);
        System.out.print(" " + child.f());
        // TODO: Keep mirrored set of open to have fast lookups
        boolean in_closed = closed.contains(child);
        boolean in_open   = open.contains(child);
        if (in_closed || in_open){
          if (n.g() <= child.g()) continue;
        }

        child.parent = n;

        if (in_closed){
          closed.remove(child);
        }
        if (!in_open){
          open.add(child);
        }
      }
      closed.add(n);
    }
    return null;
  }

  private static boolean isGoal(Node n){
    return n.allCars.size() == 0;
  }

  private static Node firstStepInPath(Node n){
    Node p = n.parent;
    while (p != null){
      p = n.parent;
      if (p != null) n = p;
    }
    return n;
  }
}
