import java.util.ArrayList;
import java.io.Serializable;
public class Node implements Comparable<Node> {
	
	public ArrayList<Edge> edges;
	public String name;
	public double lat, lon, latRad, lonRad, latCos;
	public Node parent;
	public int travelTime;
	public int travelTimeHeuristic;
	public boolean beenVisited;
	public boolean open;

	public Node(double lat, double lon, String name) {
		travelTimeHeuristic 		= Integer.MAX_VALUE;
		travelTime 					= Integer.MAX_VALUE;
		parent 						= null;
		this.lat 					= lat;
		this.lon					= lon;
		this.name 					= name;
		latRad						= (Math.PI * lat)/180;
		lonRad						= (Math.PI * lon)/180;
		latCos						= Math.cos(latRad);
		beenVisited					= false;
		edges 						= new ArrayList<Edge>();
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Node n) {
		if(this.travelTimeHeuristic > n.travelTimeHeuristic) return 1;
		if(this.travelTimeHeuristic < n.travelTimeHeuristic) return -1;
		else return 0;
	}
}