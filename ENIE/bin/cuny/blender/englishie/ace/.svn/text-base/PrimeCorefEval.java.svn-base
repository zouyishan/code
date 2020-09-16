
//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkit
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

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

public class PrimeCorefEval {

	static final String home =
		"C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	static final String ace =
		home + "sources/";
	static final String fileListTest = home + "testfilelist";
	static String logFile = home + "coreflog";
	static PrintStream writer;
	
	static int rnume = 0;
	static int rdeno = 0;
	static int pnume = 0;
	static int pdeno = 0;
	static int nume = 0;
	static int deno = 0;

	public static void main (String[] args) throws IOException {
		writer = new PrintStream(new FileOutputStream(
				logFile));
		evalCoref(fileListTest);
		writer.close();
	}

	/**
	 *  evaluate the event tagger using the documents list in file
	 *  <CODE>fileList</CODE>.
	 */
	public static void evalCoref (String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.err.println ("\nEvaluate file " + Integer.toString(docCount)+":"+currentDocPath);
			writer.println("==================\n"+currentDocPath);
			String textFile = ace + currentDocPath;
			String keyFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");;
			String testFile = home + "output/04_17/" + currentDocPath.replaceFirst(".sgm", ".apf");
			
			String docId = currentDocPath.replaceFirst(".sgm","");
			
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);
			//compute precision
			writer.println("**********Wrong coreference");
			evalCoref (aceKeyDoc, aceTestDoc);
			writer.println("##########Miss coreference");
			evalCoref (aceTestDoc, aceKeyDoc);
			
		}
		/*double precision = (double)pnume/pdeno;
		double recall = (double)rnume/rdeno;
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("Recall="+Double.toString(2*precision*recall/(precision+recall)));
	*/}
	
	public static void evalCoref (AceDocument aceKeyDoc, AceDocument aceTestDoc) {
		ArrayList testEvents = aceTestDoc.events;

		for (int i=0; i< testEvents.size();i++){
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			ArrayList testMentions = testEvent.mentions;
			if (testMentions.size()<=1)
				continue;
			for (int j=0; j<testMentions.size()-1;j++){
				AceEventMention m1 = (AceEventMention) testMentions.get(j);
				AceEvent keyEvent1 = correctEvent(m1.anchorExtent,m1,aceKeyDoc.events);
				if (keyEvent1==null)
					continue;
				for (int k=j+1;k<testMentions.size();k++){
					AceEventMention m2 =(AceEventMention) testMentions.get(k);
					//validate the event mention pairs m1 and m2
					AceEvent keyEvent2 = correctEvent(m2.anchorExtent,m2,aceKeyDoc.events);
					if (keyEvent2==null)
						continue;
					if (keyEvent1.id!=keyEvent2.id){
						writer.println("-----------------------");
						writer.println("M1:"+m1);
						writer.println("M2:"+m2);
					}	
				}
			}
			
		}
	}
	
	static AceEvent correctEvent (Span anchorExtent, AceEventMention mention, ArrayList keyEvents) {
		
		for (int i=0; i<keyEvents.size(); i++) {
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			ArrayList keyMentions = keyEvent.mentions;
			for (int j=0; j<keyMentions.size(); j++) {
				AceEventMention keyMention = (AceEventMention) keyMentions.get(j);
				Span keyExtent = keyMention.jetExtent;
				if (anchorExtent.within(keyExtent) &&
					mention.event.type.equals(keyEvent.type) &&
					mention.event.subtype.equals(keyEvent.subtype)) {
				 
					return keyEvent;
				}
			}
		}
		return null;
	}
}
