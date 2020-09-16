package cuny.blender.englishie.nlp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import cuny.blender.englishie.algorithm.clustering.ClutoWrapper;



public class ClusteringUtils {
	static int instanceSize = 0;
	
	static DecimalFormat df = new DecimalFormat("#.####");
	
	public static double computeCV(TreeMap<String,Integer> classmap) {
		List<Integer> distributions = new ArrayList<Integer>(classmap.values());
		double average = 0;
		double std = 0;
		
		for (int i=0;i<distributions.size();i++){
			average += distributions.get(i);
		}
		instanceSize = (int)average;
		average = average/distributions.size();
		for (int i=0;i<distributions.size();i++){
			std += Math.pow((distributions.get(i)-average),2);
		}
		
		
		std = Math.sqrt(std/(distributions.size()-1));
		
		double CV=std/average;
		
		return CV;
	}
	
	
	

	public static void main(String[] args) throws Exception {
		System.out.println(df.format(computeCV("E:/research/software/EnglishIE/ENIE/tools/cluto/datasets/classic.mat.rclass")));
		System.out.println(df.format(computeCV("E:/research/software/EnglishIE/ENIE/tools/cluto/datasets/k1b.mat.rclass")));
		System.out.println(df.format(computeCV("E:/research/software/EnglishIE/ENIE/tools/cluto/datasets/la1.mat.rclass")));
		System.out.println(df.format(computeCV("E:/research/software/EnglishIE/ENIE/tools/cluto/datasets/reviews.mat.rclass")));
		System.out.println(df.format(computeCV("E:/research/software/EnglishIE/ENIE/tools/cluto/datasets/sports.mat.rclass")));
		System.out.println(df.format(computeCV("E:/research/software/EnglishIE/ENIE/tools/cluto/datasets/tr31.mat.rclass")));
	}
	
	public static double computeCV(String filename) throws IOException{

		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;
		TreeMap<String,Integer> classmap = new TreeMap<String,Integer>();
		
		while ((line = reader.readLine()) != null) {
			if (classmap.get(line)==null){
				classmap.put(line, new Integer(0));
			}
			int count = classmap.get(line);
			classmap.put(line, new Integer(count+1));
		}
		reader.close();
		
		return computeCV(classmap);
	}

	
	
	
}
