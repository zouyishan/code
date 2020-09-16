//Author:       Zheng Chen
//Description:  Evalution metric called BCube for event coreference resolution

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


public class CEAFCorefEval {
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	static final String ace = home + "source/";
	static final String fileListTest = home + "testfilelist";

	static int N = 0;
	static int testMCount = 0;
	static int keyMCount = 0;
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
		//costMatrix= new float[][]{{5,0,0,0,0},{0,1,0,0,0},{0,1,0,0,0},{0,0,1,0,0},
		//		{0,0,1,0,0},{0,0,0,1,0},{0,0,0,1,0},{0,0,0,0,1},{0,0,0,0,1}};
		costMatrix= new float[][]{{4,0,0,0,0},{1,0,0,0,0},{0,2,0,0,0},
				{0,0,2,0,0},{0,0,0,2,0},{0,0,0,0,2}};
		float [][] matrix = copyOfMatrix();
		KuhnMunkresAlgorithm alg = new KuhnMunkresAlgorithm();
		alg.convert2Min(matrix);
		int [][] assignment = alg.computeAssignments(matrix);
		
		for (int i=0;i<assignment.length;i++){
			if (assignment[i]!=null)
				N+= costMatrix[assignment[i][0]][i];
		}
		/*precision = (double)N/13;
		recall = (double)N/13;
		F= 2*precision*recall/(precision+recall);
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	*/

	}

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
		precision = (double)N/testMCount;
		recall = (double)N/keyMCount;
		F= 2*precision*recall/(precision+recall);
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	public void evalCoref (String home, String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.err.println ("\nEvaluate file " + Integer.toString(docCount)+":"+currentDocPath);
		 
			String textFile = home+"source/" + currentDocPath;
			String keyFile = home+"source/" + currentDocPath.replaceFirst(".sgm", ".apf.xml");;
			String testFile = home + "output/" + currentDocPath.replaceFirst(".sgm", ".apf");
			
			String docId = currentDocPath.replaceFirst(".sgm","");
			
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);
			
			evalCoref (aceKeyDoc, aceTestDoc);
		}
		precision = (double)N/testMCount;
		recall = (double)N/keyMCount;
		F= 2*precision*recall/(precision+recall);
		System.out.println("Precision="+Double.toString(precision));
		System.out.println("Recall="+Double.toString(recall));
		System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	public void evalCoref(String sourceDir, String apfDir, String outDir,
			List<String> fileNames) throws IOException {
		for (int i = 0; i < fileNames.size(); i++) {
			String fileName = fileNames.get(i);
			// System.err.println ("\nEvaluate file " +
			// Integer.toString(docCount)+":"+currentDocPath);

			String textFile = sourceDir + fileName;
			String keyFile = apfDir + fileName.replaceFirst(".sgm", ".apf.xml");
			
			String testFile = outDir
					+ fileName.replaceFirst(".sgm", ".apf.xml");

			String docId = fileName.replaceFirst(".sgm", "");
			
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);
			
			evalCoref (aceKeyDoc, aceTestDoc);
		}
		precision = (double)N/testMCount;
		recall = (double)N/keyMCount;
		F= 2*precision*recall/(precision+recall);
		//System.out.println("Precision="+Double.toString(precision));
		//System.out.println("Recall="+Double.toString(recall));
		//System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}
	
	public void evalCoref (AceDocument aceKeyDoc, AceDocument aceTestDoc) {
		
		ArrayList testEvents = aceTestDoc.events;
		ArrayList keyEvents = aceKeyDoc.events;

		for (int i=0; i< testEvents.size();i++){
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			//if (testEvent.genericity.equals("Specific")&&testEvent.polarity.equals("Positive"))
				testMCount += testEvent.mentions.size();
			//else 
			//	testEvents.remove(testEvent);
		}
		for (int i=0; i< keyEvents.size();i++){
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			//if (keyEvent.genericity.equals("Specific")&&keyEvent.polarity.equals("Positive"))
				keyMCount += keyEvent.mentions.size();
			//else
			//	keyEvents.remove(keyEvent);
		}
		
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
			if (assignment[i]!=null)
				N+= costMatrix[assignment[i][0]][i];
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
