package cuny.blender.englishie.algorithm.clustering;

public class PageRankItem implements Comparable<PageRankItem>{
	int id;
	double score;
	boolean used;
	
	public PageRankItem(int id, double score){
		this.id = id;
		this.score = score;
	}
	
	public int getId(){
		return this.id;
	}
	
	public double getScore(){
		return this.score;
	}
	
	public int compareTo(PageRankItem item) {
		if (this.score<item.score)
			return -1;
		else if (this.score>item.score)
			return 1;
		else
			return 0;
	}
}
