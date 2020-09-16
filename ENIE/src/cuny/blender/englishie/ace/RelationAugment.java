/**
 * 
 */
package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;



/**
 * @author Administrator
 *
 */
public class RelationAugment {

	static String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Locale.setDefault(Locale.US);
		TreeMap<String, List<ConnectWords>> clusters = new TreeMap<String, List<ConnectWords>>();
		readClusters(home+"paraphrases_hybrid_ACE05Relation2", clusters);
		System.out.println(clusters.get("abandoned"));
//		filterClusters(clusters, .95);
		// Test to see if reading in the clusters was good
		
		/*Iterator<String> itr = clusters.navigableKeySet().iterator();
		while(itr.hasNext()) {
			String curr = itr.next();
			List<ConnectWords> currList = clusters.get(curr);
			for(ConnectWords i : currList) {
				System.out.println(curr + " ||| " + i.words + " ||| " + i.confidence);
			}
		}
		*/
		
		// Generate sentences
		// TODO Fixme and give me a real path
//		RelationFileParser rfp = new RelationFileParser("path to file goes here");
//		List<Relation> relations = rfp.parse();
//		ArrayList<Sentence> toPrint = new ArrayList<Sentence>();
//		
//		for(Sentence i : toPrint) {
//			System.out.println(i);
//		}
//		
		
	}
	
	/**
	 * Filter the clusters based on the given threshold
	 * @param clusters the mapping of original connecting words to list of connecting words found in the clusters
	 * @param threshold anything below the given threshold will be filtered out
	 */
	public static void filterClusters(Map<String, List<ConnectWords>> clusters, Double threshold) {
		Iterator<String> itr = ((TreeMap<String, List<ConnectWords>>) clusters).navigableKeySet().iterator();
		while(itr.hasNext()) {
			String curr = itr.next();
			List<ConnectWords> currList = clusters.get(curr);
			ArrayList<ConnectWords> toRemove = new ArrayList<ConnectWords>();
			for(ConnectWords i : currList) {
				if(i.confidence<threshold) {
					toRemove.add(i);
				}
			}
			currList.removeAll(toRemove);
		}

	}

	/**
	 * Reads from file with cluster information delimited as follows
	 * connecting words ||| substitute words ||| confidence(as double)
	 * @param file path to file with cluster information
	 * @param clusters a map using connecting words as the key to a list of 
	 */
	public static void readClusters(String file, Map<String, List<ConnectWords>> clusters) {
		if (clusters == null) {
			return;
		}
		BufferedReader br;
		int lineNumber = 0;
		String line = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			line = br.readLine();
			while (line != null) {
				lineNumber++;
				String[] input = line.split(" \\|\\|\\| ");
				List<ConnectWords> temp = clusters.get(input[0].trim());
				if (temp == null) {
					ArrayList<ConnectWords> toInsert = new ArrayList<ConnectWords>();
					toInsert.add(new ConnectWords(input[1].trim(), Double.parseDouble(input[2].trim())));
					clusters.put(input[0].trim(), toInsert);
				} else {
					temp.add(new ConnectWords(input[1].trim(), Double.parseDouble(input[2].trim())));
				}
				line = br.readLine();
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			System.err.println("Error parsing on line: " + lineNumber + ": " + line);
		}
	}

}
