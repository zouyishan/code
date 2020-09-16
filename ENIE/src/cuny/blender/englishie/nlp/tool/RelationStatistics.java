package cuny.blender.englishie.nlp.tool;


import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEntity;
import cuny.blender.englishie.ace.AceEntityMention;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.ace.AceEventMention;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


public class RelationStatistics {
	static final String home = "C:/Users/blender/workspace5/TAC/corpus/";
	//static final String home = "/jar/KBP/data/LDC/KBPEntity/";
	//static String encoding = "UTF-8";
	static String dataDir = home+"TAC_2009_KBP_Evaluation_Source_Data/";
	static String fileList = dataDir+"docs/docid_to_file_mapping.tab";
	static String queryList = dataDir+"query/filelist";
	
	static String apfDir = home+"newweboutput/";
	static String perFileName = home+"per";
	static String orgFileName = home+"org";
	static String gpeFileName = home+"gpe";
	static String txtFileList= home+"1";
	
	public static void main(String[] args) throws Exception {
		TreeMap<String,String> pathMap = new TreeMap<String,String>();
		String line;
		String output = home+"org_final";
		FileWriter fw = new FileWriter(output);
		
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		while ((line = reader.readLine()) != null) {
			String [] items = line.split("\\s+");
			pathMap.put(items[0],items[1]);
			
		}
		reader.close();
		
		reader = new BufferedReader (new FileReader(queryList));
		while ((line = reader.readLine()) != null) {
			String textFile = dataDir+pathMap.get(line);
			String xmlFile = textFile.replace("/data/", "/tagged/").replace(".sgm", ".apf.xml");
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			ArrayList relations = aceDoc.relations;
			for (int i=0; i<relations.size();i++){
				
			}
		}
		reader.close();
		fw.close();
		
	}
	
	
}