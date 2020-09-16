package cuny.blender.englishie.algorithm.graph.shortestpath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class TestDijkstraAlgorithm {

	private static List<Vertex> nodes;
	private static List<Edge> edges;


	public static void main(String[] args) throws IOException  {
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		for (int i = 0; i < 11; i++) {
			Vertex location = new Vertex("Node_" + i, "Node_" + i);
			nodes.add(location);
		}

		addLane("Edge_0", 0, 1, 85);
		addLane("Edge_1", 0, 2, 217);
		addLane("Edge_2", 0, 4, 173);
		addLane("Edge_3", 2, 6, 186);
		addLane("Edge_4", 2, 7, 103);
		addLane("Edge_5", 3, 7, 183);
		addLane("Edge_6", 5, 8, 250);
		addLane("Edge_7", 8, 9, 84);
		addLane("Edge_8", 7, 9, 167);
		addLane("Edge_9", 4, 9, 502);
		addLane("Edge_10", 9, 10, 40);
		addLane("Edge_11", 1, 10, 600);
		/*addLane("Edge_12", 1, 0, 85);
		addLane("Edge_13", 2, 0, 217);
		addLane("Edge_14", 4, 0, 173);
		addLane("Edge_15", 6, 2, 186);
		addLane("Edge_16", 7, 2, 103);
		addLane("Edge_17", 7, 3, 183);
		addLane("Edge_18", 8, 5, 250);
		addLane("Edge_19", 9, 8, 84);
		addLane("Edge_20", 9, 7, 167);
		addLane("Edge_21", 9, 4, 502);
		addLane("Edge_22", 10, 9, 40);
		addLane("Edge_23", 10, 1, 600);*/

		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(nodes, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		dijkstra.execute(nodes.get(0));
		LinkedList<Vertex> path = dijkstra.getPath(nodes.get(10));
		
		
		for (Vertex vertex : path) {
			System.out.println(vertex);
		}
		
	}

	private static void addLane(String laneId, int sourceLocNo, int destLocNo,
			int duration) {
		Edge lane = new Edge(laneId,nodes.get(sourceLocNo), nodes.get(destLocNo), duration );
		edges.add(lane);
	}
}