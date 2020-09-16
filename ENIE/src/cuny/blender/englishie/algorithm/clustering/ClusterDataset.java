package cuny.blender.englishie.algorithm.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class ClusterDataset {
	String name;
	int dimension;
	String path;
	TreeMap<Integer, ClusterItem> items = new TreeMap<Integer, ClusterItem> ();
	ArrayList<Integer> shuffledIndexes = new ArrayList<Integer> ();
	ArrayList<PageRankItem> pageranks = new ArrayList<PageRankItem>();
	
	int seedSize; 
	int colSize;
	int dbSize;
	
	public int getSeedSize(){
		return seedSize;
	}
	
	public void setSeedSize(int seedSize){
		this.seedSize = seedSize;
	}
	
	public int getColSize(){
		return colSize;
	}
	
	public int getSize(){
		return dbSize;
	}
	
	public ArrayList<Integer> getShuffledIndexes(){
		return shuffledIndexes;
	}
	
	public void setColSize(int colSize){
		this.colSize = colSize;
	}
	
	public ClusterDataset(String name,String path, int seedSize, int colSize){
		this.name = name;
		this.path = path;
		this.seedSize = seedSize;
		this.colSize = colSize;
	}
	
	public ClusterDataset(String name,String path, int seedSize){
		this.name = name;
		this.path = path;
		this.seedSize = seedSize;
	}
	
	
	
	public String getName(){
		return this.name;
	}
	
	public String getPath(){
		return this.path;
	}
	
	public void setDimension(int dimension){
		this.dimension = dimension;
	}
	
	public int getDimension(){
		return this.dimension;
	}
	
	public TreeMap<Integer, ClusterItem> getItems(){
		return this. items;
	}
	
	public ArrayList<PageRankItem> getPageRanks() {
		return pageranks;
	}
	
	public TreeMap<String,ArrayList<Integer>> classMap = new TreeMap<String,ArrayList<Integer>>();
	
	public void loadDataset(String filename) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(filename+".norm"));
		String line;
		line = reader.readLine();
		String[] strs = line.split("\\s+");
		setDimension(Integer.parseInt(strs[1]));
		
		//read data from the whole dataset
		int index = 0;
		while ((line = reader.readLine()) != null) {
			ClusterItem item = new ClusterItem(index, line);
			items.put(index, item);
			index++;
		}
		reader.close();
		
		dbSize = index;
		//read classid from rclass file
		reader = new BufferedReader(new FileReader(filename + ".rclass"));

		index = 0;
		while ((line = reader.readLine()) != null) {
			ClusterItem item = items.get(index);
			item.setClassId(line);
			
			if (classMap.get(line)==null){
				classMap.put(line, new ArrayList<Integer>());
			}
			ArrayList<Integer> classIndices = classMap.get(line);
			classIndices.add(index);
			index++;
		}
		reader.close();
		
	}
	
	public void writeSample(String filename, ArrayList<Integer> shuffledIndexes) throws IOException{
		StringBuffer buf = new StringBuffer();
		
		//write randomly selected "seedsize" items to a file
		int nonzeroes = 0;
		for (int i = 0; i < shuffledIndexes.size(); i++) {
			int id = shuffledIndexes.get(i);
			String content = items.get(id).getContent();
			buf.append( content + "\n");
			nonzeroes += content.split("\\s+").length;
		}
	
		PrintWriter pw = new PrintWriter(new File(filename));
		pw.println(shuffledIndexes.size() + " " + getDimension() + " " + nonzeroes / 2);
		pw.print(buf.toString());
		pw.close();

		//write classid of selected items to a file
		File fc = new File(filename + ".rclass");
		pw = new PrintWriter(fc);
		for (int i = 0; i < shuffledIndexes.size(); i++) {
			int id = shuffledIndexes.get(i);
			String classId = items.get(id).getClassId();
			pw.println(classId);
		}
	
		pw.close();
	
		//write id to a file
		File fi = new File(filename + ".index");
		pw = new PrintWriter(fi);
		for (int i = 0; i < shuffledIndexes.size(); i++) {
			pw.println(shuffledIndexes.get(i));
		}
		pw.close();
	}
	
	public void makeShuffledIndexes(){
		shuffledIndexes = new ArrayList<Integer>();
		for (int i=0;i<dbSize;i++){
			shuffledIndexes.add(i);
		}
		Collections.shuffle(shuffledIndexes);
	}
	
	TreeMap<String,Integer> sizeMap = new TreeMap<String,Integer>();
	
	public void loadSizeMap(int datasetId){
		sizeMap = new TreeMap<String,Integer>();
		switch (datasetId){
		case 1:
			sizeMap.put("computer", 100);
			sizeMap.put("electronics", 100);
			sizeMap.put("health", 250);
			sizeMap.put("medical", 100);
			sizeMap.put("research", 100);
			sizeMap.put("technology", 100);
			break;
		case 2:
			sizeMap.put("computer", 90);
			sizeMap.put("electronics", 90);
			sizeMap.put("health", 300);
			sizeMap.put("medical", 90);
			sizeMap.put("research", 90);
			sizeMap.put("technology", 90);
			break;
		case 3:
			
			sizeMap.put("computer", 80);
			sizeMap.put("electronics", 80);
			sizeMap.put("health", 350);
			sizeMap.put("medical", 80);
			sizeMap.put("research", 80);
			sizeMap.put("technology", 80);
			break;
		case 4:
			sizeMap.put("computer", 70);
			sizeMap.put("electronics", 70);
			sizeMap.put("health", 400);
			sizeMap.put("medical", 70);
			sizeMap.put("research", 70);
			sizeMap.put("technology", 70);
			break;
		case 5:
			sizeMap.put("computer", 60);
			sizeMap.put("electronics", 60);
			sizeMap.put("health", 450);
			sizeMap.put("medical", 60);
			sizeMap.put("research", 60);
			sizeMap.put("technology", 60);
			break;
		case 6:
			sizeMap.put("computer", 50);
			sizeMap.put("electronics", 50);
			sizeMap.put("health", 500);
			sizeMap.put("medical", 50);
			sizeMap.put("research", 50);
			sizeMap.put("technology", 50);
			break;
		case 7:
			sizeMap.put("computer", 40);
			sizeMap.put("electronics", 40);
			sizeMap.put("health", 550);
			sizeMap.put("medical", 40);
			sizeMap.put("research", 40);
			sizeMap.put("technology", 40);
			break;
		case 8:
			sizeMap.put("computer", 30);
			sizeMap.put("electronics", 30);
			sizeMap.put("health", 600);
			sizeMap.put("medical", 30);
			sizeMap.put("research", 30);
			sizeMap.put("technology", 30);
			break;
		}
	}
	public void makeCVdataset(int cvChoice, String filename) throws FileNotFoundException{
		
		loadSizeMap(cvChoice);
		Iterator it =  classMap.keySet().iterator();
		StringBuffer buf = new StringBuffer();
		int nonzeroes = 0;
		int count = 0;
		
		File fc = new File("tools/cluto/cv/"+name+"_cv_1_"+filename + ".rclass");
		PrintWriter pw = new PrintWriter(fc);
		while (it.hasNext()){
			String key = (String) it.next();
			ArrayList<Integer> arr = classMap.get(key);
			ArrayList<Integer> arr2= new ArrayList<Integer>(arr);
			Collections.shuffle(arr2);
			int size = sizeMap.get(key);
			for (int i = 0; i < size; i++) {
				int id = arr2.get(i);
				String content = items.get(id).getContent();
				buf.append( content + "\n");
				nonzeroes += content.split("\\s+").length;
				pw.println(key);
			}
			count+=size;
		}
		pw.close();
	
		PrintWriter pw2 = new PrintWriter(new File("tools/cluto/cv/"+name+"_cv_1_"+filename));
		pw2.println(count + " " + getDimension() + " " + nonzeroes / 2);
		pw2.print(buf.toString());
		pw2.close();

		
	}
	
	public void loadPagerank(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename+".pagerank"));
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#"))
				continue;
			
			String [] strs = line.split("\\s+");
			PageRankItem item = new PageRankItem(Integer.parseInt(strs[0]), Double.parseDouble(strs[1]));
			pageranks.add(item);
		}
		
		Collections.sort(pageranks,Collections.reverseOrder());

		reader.close();
	}
	
}
