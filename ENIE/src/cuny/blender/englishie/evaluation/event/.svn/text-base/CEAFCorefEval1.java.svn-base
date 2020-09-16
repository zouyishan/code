
//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkit
//              (ACE extensions)

package cuny.blender.englishie.evaluation.event;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.*;
import cuny.blender.englishie.algorithm.graph.*;
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

public class CEAFCorefEval1 {
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	//static final String home =	"C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home ="/home/zheng/Application/Coref/graph/ex1/ACE05/";
	static final String ace =
		home + "source/";
	static final String fileListTest = home + "testfilelist";


	double deno = 0;
	int testEntities = 0;
	int systemEntities = 0;
	public double precision = 0;
	public double recall = 0;
	public double F = 0;
	static private float[][] costMatrix;
	
	static float[][] copyOfMatrix() {
        // make a copy of the passed array
        float[][] retval = new float[costMatrix.length][];
        for (int i = 0; i < costMatrix.length; i++) {
            retval[i] = new float[costMatrix[i].length];
            System.arraycopy(costMatrix[i], 0, retval[i], 0, costMatrix[i].length);
        }
        return retval;
    }
	
	public static void main (String[] args) throws IOException {
		CEAFCorefEval1 ee = new CEAFCorefEval1();
		ee.evalCoref(fileListTest);

	}

	/**
	 *  evaluate the event tagger using the documents list in file
	 *  <CODE>fileList</CODE>.
	 */
	public void evalCoref (String fileList) throws IOException {
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
			
			evalCoref (aceKeyDoc, aceTestDoc);
		}
		precision = deno/testEntities;
		recall = deno/systemEntities;
		F= 2*precision*recall/(precision+recall);
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	public void evalCoref (AceDocument aceKeyDoc, AceDocument aceTestDoc) {
		
		ArrayList testEvents = aceTestDoc.events;
		ArrayList keyEvents = aceKeyDoc.events;

		testEntities += testEvents.size();
		systemEntities += keyEvents.size();
		
		if (testEvents.size()==0 || keyEvents.size()==0)
			return;
		costMatrix= new float[testEvents.size()][];
        for (int i = 0; i < testEvents.size(); i++) {
        	costMatrix[i] = new float[keyEvents.size()];
        }
        
		for (int i=0; i< testEvents.size();i++){
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			
			for (int j=0;j<keyEvents.size();j++){
				AceEvent keyEvent = (AceEvent) keyEvents.get(j);
				ArrayList testMentions = new ArrayList(testEvent.mentions);
				
				testMentions.retainAll(keyEvent.mentions);
				costMatrix[i][j]=testMentions.size();
			}
		}
		
		float [][] matrix = copyOfMatrix();
		KuhnMunkresAlgorithm alg = new KuhnMunkresAlgorithm();
		alg.convert2Min(matrix);
		int [][] assignment = alg.computeAssignments(matrix);
		
		for (int i=0;i<assignment.length;i++){
			if (assignment[i]!=null){
				double interCount = costMatrix[assignment[i][0]][i];
				AceEvent e1= (AceEvent)testEvents.get(assignment[i][0]);
				AceEvent e2= (AceEvent)keyEvents.get(i);
				deno+= 2*interCount/(e1.mentions.size()+e2.mentions.size());
			}
		}
		
		
	}
	
	public static AceEvent findEvent(AceDocument aceDoc,AceEventMention testMention){
		ArrayList events = aceDoc.events;
		for (int i=0;i<events.size();i++){
			AceEvent event = (AceEvent)events.get(i);
			if (!event.subtype.equals(testMention.event.subtype))
				continue;
			for (int j=0;j<event.mentions.size();j++){
				if (testMention.equals(event.mentions.get(j)))
					return event;
			}
		}
		return null;
	}
}
