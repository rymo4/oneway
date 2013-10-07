package oneway.g4;

public class Car {
	int startTime;
	Direction dir;
	boolean complete = false;
	int latency = 0;
	
	public Car(int startTime, Direction dir) {
		this.startTime = startTime;
		this.dir = dir;
	}

  public Car copy() {
    return new Car(this.startTime, this.dir);
  }

  public boolean isRightbound() {
    return dir == Direction.RIGHT;
  }

  public void setComplete(int currentTime) {
    complete = true;
    int latency = currentTime - startTime;
  }
  
  public int getLatency() {
    return latency;
  }

  public boolean isComplete() {
    return complete;
  }
}
