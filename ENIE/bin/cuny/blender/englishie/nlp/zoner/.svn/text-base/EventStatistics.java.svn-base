package cuny.blender.englishie.nlp.zoner;

import java.util.*;
import java.io.*;



public class EventStatistics {
	static String encoding = "UTF-8";
	static String home ="C:/Users/blender/GALE/ACE05/model/";
	static String fileList = home+"roleFeatureFile.log";
	
	static String outputfile = home+"output";

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileList), encoding));
		String line;
		int featureSize;
		String[] items;
		if ((line = reader.readLine())!=null){
			items = line.split("\\s+");
			featureSize = items.length-1;
		}
		else
			return;

		int lineNo = 1;
		while ((line = reader.readLine()) != null){
			lineNo++;
		}
		reader.close();
		
		String feature;
		String featureValue;
		int[][] fMatrix= new int[lineNo][featureSize];
		int [] cMatrix = new int[lineNo];
		
		TreeMap<String,Integer> c = new TreeMap<String,Integer>();

		int lineId;
		int count;
		for (int i=0;i<featureSize;i++){
			//System.out.println("processing feature"+Integer.toString(i));
			TreeMap<String,Integer> m = new TreeMap<String,Integer>();
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileList), encoding));
			
			count = 0;
			lineId = 0;
			while ((line = reader.readLine()) != null) {
				items = line.split("\\s+");
				if (items.length-1!=featureSize)
					System.out.println("Warning,unmatched feature length");
				feature = items[i];
				featureValue = feature.substring(feature.indexOf('=')+1);
				if (m.containsKey(featureValue)){
					fMatrix[lineId][i]=m.get(featureValue);
				}
				else {
					count ++;
					m.put(featureValue, new Integer(count));
					fMatrix[lineId][i]=count;
				}
				lineId++;
			}
			System.out.println("Feature "+Integer.toString(i+1)+": 1-"+m.size());
			reader.close();
				
		}
		
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileList), encoding));
		lineId = 0;
		count = 0;
		while ((line = reader.readLine()) != null) {
			items = line.split("\\s+");
			
			feature = items[items.length-1];
			
			if (c.containsKey(feature)){
				cMatrix[lineId]=c.get(feature);
			}
			else {
				count ++;
				c.put(feature, new Integer(count));
				cMatrix[lineId]=count;
			}
			lineId++;
		}
		System.out.println("Total classes: "+c.size());
		reader.close();
		
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(
						outputfile), encoding));
		for (int i=0;i<lineNo;i++){
			int j;
			String str;
			for (j=0;j<featureSize;j++){
				str = String.format("%5d",fMatrix[i][j]);
				bw.write(str+"\t");
			}
			str = String.format("%5d",cMatrix[i]);
			bw.write(str+"\n");
			
		}
		bw.close();
	}
	
}