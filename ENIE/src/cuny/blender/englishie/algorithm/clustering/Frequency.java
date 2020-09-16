package cuny.blender.englishie.algorithm.clustering;

import java.util.*;
import java.io.*;


public class Frequency {
	public static void main (String[] args) throws IOException {
		String str = "a b c a";//args[0];
		String [] items = str.split("\\s+");
		HashMap<String,Integer> frequencyMap= new HashMap<String, Integer>();
		for (int i=0;i<items.length;i++){
			if (frequencyMap.get(items[i])==null){
				frequencyMap.put(items[i], new Integer(0));
			}
			int freq = frequencyMap.get(items[i]);
			frequencyMap.put(items[i], new Integer(freq+1));
		}
		
		Iterator it = frequencyMap.keySet().iterator();
		while (it.hasNext()){
			String key = (String)it.next();
			System.out.print(key+":"+frequencyMap.get(key)+"\t");
		}
	}
}