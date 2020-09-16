package cuny.blender.englishie.algorithm.clustering;

public class Edge {
	protected String id;
	protected double weight;
	protected String pair;
	
	public Edge(String id, double weight, String pair) {
		this.id = id;
		this.weight = weight;
		this.pair = pair;
	}
	
	public String toString () {
		
		return this.id+":("+this.pair+"):"+weight;
	}
}