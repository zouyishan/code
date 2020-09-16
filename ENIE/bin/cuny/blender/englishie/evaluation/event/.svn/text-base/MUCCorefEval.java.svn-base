//Author:       Zheng Chen
//Description:  Implementation of a metric called MUC for event coreference resolution


package cuny.blender.englishie.evaluation.event;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.*;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.parser.SyntacticRelationSet;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;

/**
 *  CorefEval evaluates an event tagger using simple metrics of events and
 *  arguments found, missing, and spurious.
 */

public class MUCCorefEval {
	//static final String home = "/jar/workspace/blender/corpus/ACE05/";
	static String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	//static final String home = "/jar/workspace/blender/corpus/ACE05/";
	//static final String home ="C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home ="/home/zheng/Application/Coref/graph/ex1/ACE05/";
	//static final String home ="/home/zheng/Application/Coref/ex2/ACE05/";
	static final String ace =
		home + "source/";
	static final String fileListTest = home + "testfilelist";

	int rnume = 0;
	int rdeno = 0;
	int pnume = 0;
	int pdeno = 0;
	int nume = 0;
	int deno = 0;

	public double precision;
	public double recall;
	public double F;
	static void main (String[] args) throws IOException {
		//evalCoref(fileListTest);
	}

	public  void evalCoref (String home,String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.err.println ("\nEvaluate file " + Integer.toString(docCount)+":"+currentDocPath);
		 
			String textFile = home + "source/"+currentDocPath;
			String keyFile = home + "source/"+currentDocPath.replaceFirst(".sgm", ".apf.xml");;
			String testFile = home + "output/" + currentDocPath.replaceFirst(".sgm", ".apf");
			
			String docId = currentDocPath.replaceFirst(".sgm","");
			
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);
			//compute recall
			evalCoref (aceKeyDoc, aceTestDoc);
			rnume += nume; 
			rdeno += deno;
			//compute precision
			evalCoref (aceTestDoc, aceKeyDoc);
			pnume += nume; 
			pdeno += deno;
		}
		precision = (double)pnume/pdeno;
		recall = (double)rnume/rdeno;
		F=2*precision*recall/(precision+recall);
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	/**
	 *  evaluate the event tagger using the documents list in file
	 *  <CODE>fileList</CODE>.
	 */
	public  void evalCoref (String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.err.println ("\nEvaluate file " + Integer.toString(docCount)+":"+currentDocPath);
		 
			String textFile = ace + currentDocPath;
			String keyFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");;
			String testFile = home + "output/" + currentDocPath.replaceFirst(".sgm", ".apf");
			
			String docId = currentDocPath.replaceFirst(".sgm","");
			
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);
			//compute recall
			evalCoref (aceKeyDoc, aceTestDoc);
			rnume += nume; 
			rdeno += deno;
			//compute precision
			evalCoref (aceTestDoc, aceKeyDoc);
			pnume += nume; 
			pdeno += deno;
		}
		precision = (double)pnume/pdeno;
		recall = (double)rnume/rdeno;
		F=2*precision*recall/(precision+recall);
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	public  void evalCoref (String sourceDir, String apfDir, String outDir, List<String> fileNames) throws IOException {
		
		for (int i=0;i<fileNames.size();i++){
			String fileName = fileNames.get(i);
			//System.err.println ("\nEvaluate file " + Integer.toString(docCount)+":"+currentDocPath);
		 
			String textFile = sourceDir + fileName;
			String keyFile = apfDir + fileName.replaceFirst(".sgm", ".apf.xml");;
			String testFile = outDir + fileName.replaceFirst(".sgm", ".apf.xml");
			
			String docId = fileName.replaceFirst(".sgm","");
			
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);
			//compute recall
			evalCoref (aceKeyDoc, aceTestDoc);
			rnume += nume; 
			rdeno += deno;
			//compute precision
			evalCoref (aceTestDoc, aceKeyDoc);
			pnume += nume; 
			pdeno += deno;
		}
		precision = (double)pnume/pdeno;
		recall = (double)rnume/rdeno;
		F=2*precision*recall/(precision+recall);
		//System.out.print("Precision="+Double.toString(precision)+",");
		//System.out.print("Recall="+Double.toString(recall)+",");
		//System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	public  void evalCoref (AceDocument aceKeyDoc, AceDocument aceTestDoc) {
		ArrayList keyEvents = aceKeyDoc.events;
		nume = 0;
		deno = 0;
		for (int i=0; i< keyEvents.size();i++){
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
		//	if (!keyEvent.genericity.equals("Specific")||!keyEvent.polarity.equals("Positive"))
		//		continue;
			ArrayList keyMentions = new ArrayList(keyEvent.mentions);
			ArrayList testEvents = aceTestDoc.events;
			int setCount = 0;
			for (int j=0;j< testEvents.size();j++){
				AceEvent testEvent = (AceEvent) testEvents.get(j);
				if (keyMentions.removeAll(testEvent.mentions))
					setCount++;
			}
			setCount += keyMentions.size();
			nume += keyEvent.mentions.size()-setCount;
			deno += keyEvent.mentions.size()-1;
		}
	}
}
