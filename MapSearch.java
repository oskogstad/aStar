import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.HashMap;
import java.io.*;
import java.util.PriorityQueue;
import org.openstreetmap.gui.jmapviewer.*;
import org.openstreetmap.gui.jmapviewer.interfaces.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.util.Random;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JOptionPane;;
import java.awt.event.*;
import javax.swing.event.*;

public class MapSearch implements ActionListener, MouseListener {

	final double EARTH_CONSTANT = 41701090.90909090909090909091;
	static String inputRegion;
	static boolean dijkstra;
	ArrayList<Node> nameLookup, nodes, search;
	PriorityQueue<Node> queue;
	Console cons;
	int totalTime, totalLength, nodeCounter;
	long startTime, algDuration;
	Node fromNode, toNode, result;
	String driveTimeString, lengthString, runtimeString, nodesString;
	Random rand;
	JLabel 	aHead, aDriveTime, aLength, aRuntime, aNodes,
		dHead, dDriveTime, dLength, dRuntime, dNodes,
		aTimeLabel, alengthLabel, aruntimeLabel, anodesLabel,
		dTimeLabel, dlengthLabel, druntimeLabel, dnodesLabel,
		spacer1, spacer2, spacer3, spacer4, spacer5, spacer6,
		from, to;
	JPanel input, dResult, aResult, buttonBox, maps;
	JSplitPane wrapper, results, textFrame;
	JButton sButton, rButton;
	JTextField tFrom, tTo;
	JFrame window;
	JMapViewer jmapA, jmapD;
	JScrollPane fromScroll, toScroll;
	JList<Node> fromScrollList, toScrollList;

	boolean startNodeSelected;
	
	public MapSearch() throws Exception {
		long inputTimerStart = System.nanoTime();
		init();
		long inputTimerEnd = (System.nanoTime() - inputTimerStart)/1000000000;
		System.out.println("init() took " + inputTimerEnd + "s");
	}

	// the star of A*
	public Node aStar(Node from, Node to) {
		
		// just in case someone tries
		if(from == to) {
			return from;
		}

		Node n; 			// current node
		int newTravelTime; 	// current nodes traveltime + the travelTime from current to target 
			
		// first node (from), set starting distance to 0 and F to straight line
		from.travelTime = 0;
		from.travelTimeHeuristic = getHeuristics(from, to);

		// Priorityque sorting on the nodes travelTimeHeuristic, lowest first
		queue.add(from);	

		// chop the queue until we pop the target node
		while((n=queue.remove())!= to) {
			
			// keeps track of stats for assignment, num of nodes processed
			nodeCounter++;
			
			// visited nodes does not get processed again
			n.beenVisited = true;
			for (Edge e : n.edges) {
				if(e.target.beenVisited) continue;
								
				newTravelTime = n.travelTime + e.travelTime;				
				
				if(newTravelTime <= e.target.travelTime) {
					e.target.travelTime = newTravelTime;
					e.target.travelTimeHeuristic = newTravelTime + getHeuristics(e.target, to);
					e.target.parent = n;
					if(e.target.open) 
						queue.remove(e.target);
					else {
						e.target.open = true;
					}
					queue.add(e.target);

				}
			}
		}
		return n;
	}

	// ----------------------------------------------------------------------------------------------------------------------------------------------------
	// init/userinput/main/timeconvert/io after this point
	public void actionPerformed(ActionEvent e) {
		try{
			searchButtonPushed();
		}
		catch(Exception err) {

		}
	}

	public void searchButtonPushed()  throws Exception {
		reset();
		if(!getUserInput()) return;
		
		jmapA.removeAllMapMarkers();
		jmapD.removeAllMapMarkers();
		// A*
		dijkstra 	= false;
		startTime 	= System.nanoTime();
		result 		= aStar(fromNode, toNode);
		algDuration = (System.nanoTime() - startTime)/1000000;
		output();

		reset();

		// Dijkstra
		dijkstra 	= true;
		startTime 	= System.nanoTime();
		result 		= aStar(fromNode, toNode);	
		algDuration = (System.nanoTime() - startTime)/1000000;
		output();

		toNode 		= null;
		fromNode 	= null;
	}
	// initialize all the stuff
	public void init() throws Exception {
		// variables init
		nameLookup 			= new ArrayList<Node>();
		search 				= new ArrayList<Node>();
		nodes 				= new ArrayList<Node>();
		queue 				= new PriorityQueue<Node>();
		cons 				= System.console();
		rand 				= new Random();
		totalTime 			= 0;
		totalLength 		= 0;
		startTime 			= 0;
		algDuration			= 0;
		driveTimeString 	= "Driving time: ";
		lengthString 		= "Length: ";
		runtimeString 		= "Runtime: ";
		nodesString 		= "#nodes: ";
		// create map view and windows

		// start reading input
		readFiles();

		// GUI
		aHead 				= new JLabel();
		aDriveTime 			= new JLabel();
		aLength 			= new JLabel();
		aRuntime 			= new JLabel();
		aNodes 				= new JLabel();
		
		aTimeLabel 			= new JLabel();
		alengthLabel 		= new JLabel();
		aruntimeLabel 		= new JLabel();
		anodesLabel 		= new JLabel();
		
		dHead 				= new JLabel();
		dDriveTime 			= new JLabel();
		dLength 			= new JLabel();
		dRuntime 			= new JLabel();
		dNodes 				= new JLabel();

		dTimeLabel 			= new JLabel();
		dlengthLabel 		= new JLabel();
		druntimeLabel 		= new JLabel();
		dnodesLabel 		= new JLabel();

		from 				= new JLabel();
		to 					= new JLabel();
		tFrom				= new JTextField();
		tTo 				= new JTextField();
		spacer1				= new JLabel();
		spacer2				= new JLabel();
		spacer3				= new JLabel();
		spacer4				= new JLabel();
		spacer5				= new JLabel();
		spacer6				= new JLabel();

		sButton				= new JButton(" Search ");
		rButton 			= new JButton("Random");
		buttonBox			= new JPanel();

		window 				= new JFrame();

		jmapA 				= new JMapViewer();
		jmapD				= new JMapViewer();

		input 				= new JPanel();
		dResult 			= new JPanel();
		aResult 			= new JPanel();

		results				= new JSplitPane(JSplitPane.VERTICAL_SPLIT, aResult, dResult);
		maps 				= new JPanel();
		textFrame			= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, input, results);
		wrapper				= new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textFrame, jmapA);

		fromScrollList		= new JList<Node>();
		toScrollList		= new JList<Node>();
		fromScroll 			= new JScrollPane(fromScrollList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		toScroll 			= new JScrollPane(toScrollList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


		jmapA.addMouseListener(this);
		jmapD.addMouseListener(this);

		DefaultListModel<Node> listModel = new DefaultListModel<Node>();
		for (Node n : nameLookup) {
			listModel.addElement(n);
		}
		fromScrollList.setModel(listModel);
		toScrollList.setModel(listModel);

		fromScrollList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent event) {
					if(!event.getValueIsAdjusting()) {
						tFrom.setText(fromScrollList.getSelectedValue().toString());
					}
				}
			});

		toScrollList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent event) {
					if(!event.getValueIsAdjusting()) {
						tTo.setText(toScrollList.getSelectedValue().toString());
					}
				}
			});


		Font font 			= new Font(aHead.getFont().getName(), Font.BOLD, 20); 

		aHead.setText("A* results");
		dHead.setText("Dijkstra results");
		
		aTimeLabel.setText(driveTimeString);
		aTimeLabel.setFont(font);
		alengthLabel.setText(lengthString);
		alengthLabel.setFont(font);
		aruntimeLabel.setText(runtimeString);
		aruntimeLabel.setFont(font);
		anodesLabel.setText(nodesString);
		anodesLabel.setFont(font);
		
		aDriveTime.setText("------");
		aLength.setText("------");
		aRuntime.setText("------");
		aNodes.setText("------");

		aHead.setFont(font);	
		aDriveTime.setFont(font);
		aLength.setFont(font);
		aRuntime.setFont(font);		
		aNodes.setFont(font);

		dTimeLabel.setText(driveTimeString);
		dTimeLabel.setFont(font);
		dlengthLabel.setText(lengthString);
		dlengthLabel.setFont(font);
		druntimeLabel.setText(runtimeString);
		druntimeLabel.setFont(font);
		dnodesLabel.setText(nodesString);
		dnodesLabel.setFont(font);
		
		dDriveTime.setText("------");
		dLength.setText("------");
		dRuntime.setText("------");		
		dNodes.setText("------");

		dHead.setFont(font);	
		dDriveTime.setFont(font);
		dLength.setFont(font);
		dRuntime.setFont(font);		
		dNodes.setFont(font);
		
		from.setText("From:");
		to.setText("To:");

		from.setFont(font);
		to.setFont(font);
		tTo.setFont(new Font(aHead.getFont().getName(), Font.BOLD, 30));
		tFrom.setFont(new Font(aHead.getFont().getName(), Font.BOLD, 30));
		tTo.addActionListener(this);
		tFrom.addActionListener(this);


		aResult.setLayout(new BoxLayout(aResult, BoxLayout.Y_AXIS));
		dResult.setLayout(new BoxLayout(dResult, BoxLayout.Y_AXIS));
		aResult.setBorder(new EmptyBorder(100, 50, 10, 10));
		dResult.setBorder(new EmptyBorder(100, 50, 10, 10));


		buttonBox.setLayout(new BoxLayout(buttonBox, BoxLayout.X_AXIS));
		buttonBox.add(sButton);
		buttonBox.add(rButton);

		toScroll.setPreferredSize(new Dimension(300, 300));
		fromScroll.setPreferredSize(new Dimension(300, 300));

		input.setLayout(new BoxLayout(input, BoxLayout.Y_AXIS));
		input.setBorder(new EmptyBorder(100, 10, 100, 10));
		input.add(fromScroll);
		input.add(from);
		input.add(tFrom);
		input.add(to);
		input.add(tTo);
		input.add(buttonBox);
		input.add(toScroll);

		spacer1.setText(" ");
		spacer1.setFont(font);
		spacer2.setText(" ");
		spacer2.setFont(font);
		spacer3.setText(" ");
		spacer3.setFont(font);
		spacer4.setText(" ");
		spacer4.setFont(font);
		spacer5.setText(" ");
		spacer5.setFont(font);
		spacer6.setText(" ");
		spacer6.setFont(font);

		aResult.add(aHead);
		aResult.add(aTimeLabel);
		aResult.add(aDriveTime);
		aResult.add(spacer1);
		aResult.add(alengthLabel);
		aResult.add(aLength);
		aResult.add(spacer2);
		aResult.add(aruntimeLabel);
		aResult.add(aRuntime);
		aResult.add(spacer3);
		aResult.add(anodesLabel);
		aResult.add(aNodes);
		

		dResult.add(dHead);
		dResult.add(dTimeLabel);
		dResult.add(dDriveTime);
		dResult.add(spacer4);
		dResult.add(dlengthLabel);
		dResult.add(dLength);
		dResult.add(spacer5);
		dResult.add(druntimeLabel);
		dResult.add(dRuntime);
		dResult.add(spacer6);
		dResult.add(dnodesLabel);
		dResult.add(dNodes);
		
		aHead.setFont(new Font(aHead.getFont().getName(), Font.BOLD, 40));
		dHead.setFont(new Font(dHead.getFont().getName(), Font.BOLD, 40));

		wrapper.setResizeWeight(0.15);
		wrapper.setDividerSize(1);

		textFrame.setResizeWeight(0.8);
		textFrame.setDividerSize(1);
		

		results.setResizeWeight(0.5);
		results.setDividerSize(1);
		

		window.setSize(1920, 1080);
		window.add(wrapper);

		rButton.setPreferredSize(new Dimension(50,50));
		rButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						tFrom.setText(nameLookup.get(rand.nextInt(nameLookup.size())).name);
						tTo.setText(nameLookup.get(rand.nextInt(nameLookup.size())).name);
						searchButtonPushed();
					}
					catch(Exception eeeee) {

					}
				}
			});
		sButton.setPreferredSize(new Dimension(50,50));
		sButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						searchButtonPushed();
					}
					catch(Exception eeeee) {

					}
				}
			});

		double minLat, minLon, maxLat, maxLon;
		maxLat = maxLon = 0.0;
		minLat = minLon = 360.0;
		for(Node n : nodes) {
			if(n.lat < minLat) minLat = n.lat;
			else if(n.lat > maxLat) maxLat = n.lat;
			if(n.lat < minLon) minLon = n.lon;
			else if(n.lon > maxLon) maxLon = n.lon;			
		}
		double centerLat = minLat + (maxLat-minLat)/2.0;
		double centerLon = minLon + (maxLon-minLon)/2.0;
		System.out.printf("Center Lat: %f, Center Lon: %f\n", centerLat, centerLon);
		jmapA.setDisplayPosition(new Coordinate(centerLat, centerLon), 5);
		jmapD.setDisplayPosition(new Coordinate(centerLat, centerLon), 5);
		
		window.setVisible(true);
	}

	// reset everything for a new search
	public void reset() {
		queue 						= new PriorityQueue<Node>();
		search 						= new ArrayList<Node>();
		totalTime 					= 0;
		totalLength 				= 0;
		startTime					= 0;
		algDuration					= 0;
		nodeCounter 				= 0;
		
		for (Node n : nodes) {	
			n.travelTimeHeuristic	= Integer.MAX_VALUE;
			n.travelTime 			= Integer.MAX_VALUE;
			n.beenVisited 			= false;
			n.open                  = false;
			n.parent 				= null;
		}
	}

	// Reads the files 'nodes.txt' and 'edges.txt', create nodes and insert edges
	public void readFiles() throws Exception { 

		// Read Node file and create all nodes. Nodes with name also goes in the nameLookup ArrayList
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("nodes"+inputRegion+".txt"), StandardCharsets.UTF_8));
		String currLine;

		System.out.println("Starting node reading ...");
		while ((currLine=in.readLine()) != null) {
			int nameIndex	= currLine.indexOf("\"");
			String[] line 	= currLine.trim().split("\\s+");
			double lat 		= Double.parseDouble(line[1]);
			double lon 		= Double.parseDouble(line[2]);
			String name 	= currLine.substring(nameIndex).replaceAll("\"", ""); 
			Node node 		= new Node(lat, lon, name);
			nodes.add(node);
			if(name.length() > 0) {
				nameLookup.add(node);
			}
		}

		System.out.println("Nodes done ... " + nodes.size() + " nodes.");
		int edgeCounter = 0;
		System.out.println("Starting edge reading ...");
			
		in = new BufferedReader(new FileReader("edges"+inputRegion+".txt"));
		while((currLine=in.readLine()) != null) {
			String[] line 	= currLine.trim().split("\\s+");
			int from		= Integer.parseInt(line[0]);
			int to 			= Integer.parseInt(line[1]);
			int driveTime	= Integer.parseInt(line[2]);
			int length		= Integer.parseInt(line[3]);
			int speedLimit	= Integer.parseInt(line[4]);

			Edge edge 		= new Edge(nodes.get(to), driveTime, length, speedLimit);
			nodes.get(from).edges.add(edge);
			edgeCounter++;
		}
		System.out.println("Edges done ... " + edgeCounter + " edges.");
	}

	public boolean getUserInput() {
		if(toNode != null && fromNode != null) {
			return true;
		}
		
		// add all matching from names to search arraylist
		for (Node n : nameLookup) {
			if(n.name.equalsIgnoreCase(tFrom.getText()))
				search.add(n);
		}
		

		if(search.size()==0) {
			JOptionPane.showMessageDialog(null, "No node '" + tFrom.getText() + "' found");
			return false;
		}


		if(search.size()>1) {
			String indexChoice = JOptionPane.showInputDialog("Multiple choices for '" + tFrom.getText() + "', enter index, 0 to "+ (search.size() - 1));

			fromNode = search.get(Integer.parseInt(indexChoice));
		}
		else {
			fromNode = search.get(0);
		}
		

		// reset search
		search = new ArrayList<Node>();
		
		// add all matching to names to search arraylist
		for (Node n : nameLookup) {
			if(n.name.equalsIgnoreCase(tTo.getText()))
				search.add(n);
		}

		if(search.size()==0) {
			JOptionPane.showMessageDialog(null, "No node '" + tTo.getText() + "' found");
			return false;
		}
		if(search.size()>1) {
			String indexChoice = JOptionPane.showInputDialog("Multiple choices for '" + tTo.getText() + "', enter index, 0 to "+ (search.size() - 1));

			toNode = search.get(Integer.parseInt(indexChoice));
		}
		else {
			toNode = search.get(0);
		}
		return true;
	}
	
	// Print route to file and stats to console, add markers to jmap
	public void output() throws Exception {
		
		//PrintWriter pw = new PrintWriter(new FileWriter("route"+((dijkstra)?"D":"A")));
		while(result != fromNode) {
			for (Edge e : result.parent.edges) {
				if(e.target == result) {
					totalLength += e.length;
					totalTime += e.travelTime;
					break;
				}
			}
	  		MapMarkerDot newDot = new MapMarkerDot(new Coordinate(result.lat, result.lon));
	  		if(dijkstra)
	  			jmapD.addMapMarker(newDot);
			else
				jmapA.addMapMarker(newDot);
			result = result.parent;
		}

		MapMarkerDot newDot = new MapMarkerDot(new Coordinate(result.lat, result.lon));
		if(dijkstra)
			jmapD.addMapMarker(newDot);
		else
			jmapA.addMapMarker(newDot);		


		int totalTimeSec 	= totalTime / 100;
		Double totalLengthD = totalLength / 1000.0;
		
		// set map focus and visibility
		if(dijkstra) {
			dDriveTime.setText(timeConversion(totalTimeSec));
			dLength.setText(totalLengthD + " km");
			dRuntime.setText(algDuration + " ms");
			dNodes.setText("" + nodeCounter);
			jmapD.setDisplayToFitMapMarkers();
		}
		else {
			aDriveTime.setText(timeConversion(totalTimeSec));
			aLength.setText(totalLengthD + " km");
			aRuntime.setText(algDuration + " ms");
			aNodes.setText("" + nodeCounter);
			jmapA.setDisplayToFitMapMarkers();
		}
	}

    // returns straight line drivingTime between "to" and "from" (in 1/100 secs)
	public int getHeuristics(Node from, Node to) {
		if(dijkstra) return 0;
		double sinLat = Math.sin((from.latRad-to.latRad)/2.0);
		double sinLon = Math.sin((from.lonRad-to.lonRad)/2.0);
		return (int)(EARTH_CONSTANT * Math.asin(Math.sqrt(
														  sinLat*sinLat + from.latCos*to.latCos*sinLon*sinLon)));
	}

	// converts to hrs, min, sec
	public String timeConversion(int totalSeconds) {
    	final int MINUTES_IN_AN_HOUR 	= 60;
    	final int SECONDS_IN_A_MINUTE 	= 60;

   		int seconds 					= totalSeconds % SECONDS_IN_A_MINUTE;
    	int totalMinutes 				= totalSeconds / SECONDS_IN_A_MINUTE;
    	int minutes 					= totalMinutes % MINUTES_IN_AN_HOUR;
    	int hours 						= totalMinutes / MINUTES_IN_AN_HOUR;

    	return hours + "hrs, " + minutes + "m, and " + seconds + "s";
	}

	public static void main(String[] args)  throws Exception {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if(info.getClassName().contains("GTK")) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}

			
		}
		catch (Exception ex) {
			System.out.println("Failed setting System laf. Reverting to Java defult.");
		}

		inputRegion = args[0];
		new MapSearch();
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		int mouseX = me.getX();
		int mouseY = me.getY();
		if(startNodeSelected)
		{
			ICoordinate coord = ((JMapViewer)me.getSource()).getPosition(mouseX, mouseY);
			toNode = getNearestNode(coord.getLat(), coord.getLon());
			tTo.setText(toNode.toString()); 
			startNodeSelected = false;

		}
		else
		{
			ICoordinate coord = ((JMapViewer)me.getSource()).getPosition(mouseX, mouseY);
			fromNode = getNearestNode(coord.getLat(), coord.getLon());
			tFrom.setText(fromNode.toString());
			tTo.setText("");
			toNode = null;
			startNodeSelected = true;

		}
	}

	@Override
	public void mousePressed(MouseEvent me) {
		// Ignore
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		// Ignore
	}

	@Override
	public void mouseEntered(MouseEvent me) {
		// Ignore
	}

	@Override
	public void mouseExited(MouseEvent me) {
		// Ignore
	}

	Node getNearestNode(double lat, double lon) {
		dijkstra = false;
		Node dummy = new Node(lat, lon, "");
		Node closest = null;
		double dist = Double.MAX_VALUE;
		double h;
		for(Node n : nameLookup) {
			h = getHeuristics(dummy, n);
			if(h < dist)
			{
				closest = n;
				dist = h;
			}
		}
		
		return closest;
	}
}
