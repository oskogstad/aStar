import java.io.Serializable;

public class Edge {
	public Node target;
	public int travelTime;
	public int length;
	public int speedLimit;
	public int targetID;

	public Edge(Node target, int travelTime, int length, int speedLimit) {
		this.target		    = target;
		this.travelTime		= travelTime;
		this.length 		= length;
		this.speedLimit 	= speedLimit;
	}

	@Override
	public String toString() {
		return "travelTime: " + travelTime + ", length: " + length + "speedLimit: " + speedLimit;
	}
}