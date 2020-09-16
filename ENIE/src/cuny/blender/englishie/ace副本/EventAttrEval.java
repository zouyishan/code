
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
 *  EventEval evaluates an event tagger using simple metrics of events and
 *  arguments found, missing, and spurious.
 */

public class EventAttrEval {
	static String encoding = "UTF-8";
	int correctPol, missingPol, spuriousPol;
	int correctMod, missingMod, spuriousMod;
	int correctGen, missingGen, spuriousGen;
	int correctTen, missingTen, spuriousTen;
	double polP,polR,polF;
	double modP,modR,modF;
	double genP,genR,genF;
	double tenP,tenR,tenF;

	static final String home = "/jar/workspace/attr1/corpus/ACE05/";
	//static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	//static final String home ="C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home = "/users/zheng/EventAttr/ACE05/";
	static final String ace = home;
	static final String fileListTest = ace + "testfilelist";
	

	static String keyDir = home+"sources/";
	static String testDir = home+"output/";
	StringBuffer buf = new StringBuffer();
	static String outputfile = home+"attrlog";
	
	public static void main (String[] args) throws IOException {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		Pat.trace = false;
		Resolve.trace = false;
		AceDocument.ace2005 = true;
		EventAttrEval ea = new EventAttrEval();
		ea.evalEvents1(fileListTest);
	}

	/**
	 *  evaluate the event tagger using the documents list in file
	 *  <CODE>fileList</CODE>.
	 */

	public void evalEvents (String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		correctPol = 0;
		missingPol = 0;
		spuriousPol = 0;
		
		correctMod = 0;
		missingMod = 0;
		spuriousMod = 0;
		
		correctGen = 0;
		missingGen = 0;
		spuriousGen = 0;
		
		correctTen = 0;
		missingTen = 0;
		spuriousTen = 0;
		
		String currentDocPath;
	
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			//System.out.println("Evaluate file " + docCount + ":"
			//		+ currentDocPath);
			String textFile = keyDir + currentDocPath;
			String xmlFile = keyDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String testFile = testDir
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Control.processDocument (doc, null, false, 0);
			
			AceDocument keyDoc = new AceDocument(textFile, xmlFile);
			AceDocument testDoc = new AceDocument(textFile, testFile);
			
			evaluateBypair(doc,testDoc, keyDoc);
		}
		reader.close();
		System.out.println ("Polarity:  " +
		                    correctPol + " correct; " +
		                    
		                    missingPol + " missing; " +
		                    spuriousPol + " spurious");
		
		System.out.println ("Modality:  " +
                correctMod + " correct; " +
                
                missingMod + " missing; " +
                spuriousMod + " spurious");
		
		System.out.println ("Genericity:  " +
                correctGen + " correct; " +
                
                missingGen + " missing; " +
                spuriousGen + " spurious");
		
		System.out.println ("Tense:  " +
                correctTen + " correct; " +
                
                missingTen + " missing; " +
                spuriousTen + " spurious");
	
		polP =  (double)(correctPol)/(correctPol+spuriousPol);
		polR = (double)(correctPol)/(correctPol+missingPol);
		polF = 2*polP*polR/(polP+polR);
		
		modP =  (double)(correctMod)/(correctMod+spuriousMod);
		modR = (double)(correctMod)/(correctMod+missingMod);
		modF = 2*modP*modR/(modP+modR);
		
		genP =  (double)(correctGen)/(correctGen+spuriousGen);
		genR = (double)(correctGen)/(correctGen+missingGen);
		genF = 2*genP*genR/(genP+genR);

		tenP =  (double)(correctTen)/(correctTen+spuriousTen);
		tenR = (double)(correctTen)/(correctTen+missingTen);
		tenF = 2*tenP*tenR/(tenP+tenR);
		
		System.out.println("Polarity Evaluation ");
		System.out.println("Precision: "+Double.toString(polP));
		System.out.println("Recall: "+Double.toString(polR));
		System.out.println("F Measure: "+Double.toString(polF));
		
		System.out.println("Modality Evaluation ");
		System.out.println("Precision: "+Double.toString(modP));
		System.out.println("Recall: "+Double.toString(modR));
		System.out.println("F Measure: "+Double.toString(modF));
		
		System.out.println("Genericity Evaluation ");
		System.out.println("Precision: "+Double.toString(genP));
		System.out.println("Recall: "+Double.toString(genR));
		System.out.println("F Measure: "+Double.toString(genF));
		
		System.out.println("Tense Evaluation ");
		System.out.println("Precision: "+Double.toString(tenP));
		System.out.println("Recall: "+Double.toString(tenR));
		System.out.println("F Measure: "+Double.toString(tenF));
	}

	public void evalEvents1 (String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		correctPol = 0;
		missingPol = 0;
		spuriousPol = 0;
		
		correctMod = 0;
		missingMod = 0;
		spuriousMod = 0;
		
		correctGen = 0;
		missingGen = 0;
		spuriousGen = 0;
		
		correctTen = 0;
		missingTen = 0;
		spuriousTen = 0;
		
		String currentDocPath;
	
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			//System.out.println("Evaluate file " + docCount + ":"
			//		+ currentDocPath);
			String textFile = keyDir + currentDocPath;
			String xmlFile = keyDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String testFile = testDir
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Control.processDocument (doc, null, false, 0);
			
			AceDocument keyDoc = new AceDocument(textFile, xmlFile);
			AceDocument testDoc = new AceDocument(textFile, testFile);
			
			evaluateBypair1(doc,testDoc, keyDoc);
		}
		reader.close();
		System.out.println ("Polarity:  " +
		                    correctPol + " correct; " +
		                    
		                    missingPol + " missing; " +
		                    spuriousPol + " spurious");
		
		System.out.println ("Modality:  " +
                correctMod + " correct; " +
                
                missingMod + " missing; " +
                spuriousMod + " spurious");
		
		System.out.println ("Genericity:  " +
                correctGen + " correct; " +
                
                missingGen + " missing; " +
                spuriousGen + " spurious");
		
		System.out.println ("Tense:  " +
                correctTen + " correct; " +
                
                missingTen + " missing; " +
                spuriousTen + " spurious");
	
		polP =  (double)(correctPol)/(correctPol+spuriousPol);
		polR = (double)(correctPol)/(correctPol+missingPol);
		polF = 2*polP*polR/(polP+polR);
		
		modP =  (double)(correctMod)/(correctMod+spuriousMod);
		modR = (double)(correctMod)/(correctMod+missingMod);
		modF = 2*modP*modR/(modP+modR);
		
		genP =  (double)(correctGen)/(correctGen+spuriousGen);
		genR = (double)(correctGen)/(correctGen+missingGen);
		genF = 2*genP*genR/(genP+genR);

		tenP =  (double)(correctTen)/(correctTen+spuriousTen);
		tenR = (double)(correctTen)/(correctTen+missingTen);
		tenF = 2*tenP*tenR/(tenP+tenR);
		
		System.out.println("Polarity Evaluation ");
		System.out.println("Precision: "+Double.toString(polP));
		System.out.println("Recall: "+Double.toString(polR));
		System.out.println("F Measure: "+Double.toString(polF));
		
		System.out.println("Modality Evaluation ");
		System.out.println("Precision: "+Double.toString(modP));
		System.out.println("Recall: "+Double.toString(modR));
		System.out.println("F Measure: "+Double.toString(modF));
		
		System.out.println("Genericity Evaluation ");
		System.out.println("Precision: "+Double.toString(genP));
		System.out.println("Recall: "+Double.toString(genR));
		System.out.println("F Measure: "+Double.toString(genF));
		
		System.out.println("Tense Evaluation ");
		System.out.println("Precision: "+Double.toString(tenP));
		System.out.println("Recall: "+Double.toString(tenR));
		System.out.println("F Measure: "+Double.toString(tenF));
	}
	
	static Annotation findContainingSentence(Document doc, Span span) {
		Vector sentences = doc.annotationsOfType("sentence");
		if (sentences == null) {
			System.err.println("findContainingSentence:  no sentences found");
			return null;
		}
		Annotation sentence = null;
		for (int i = 0; i < sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			//if (span.within(s.span())) {
			if (span.start()>=s.span().start()&&span.start()<s.span().end()) {
				sentence = s;
				break;
			}
		}
		if (sentence == null) {
			/*System.err
			.println(doc.text());*/
			System.err
					.println("findContainingSentence:  can't find sentence with span");
			return null;
		}
		return sentence;
	}
	
	public void evaluateBypair(Document doc,AceDocument testDoc,
			AceDocument keyDoc) throws IOException {
		
		ArrayList testEvents = testDoc.events;
		ArrayList keyEvents = keyDoc.events;
		
		for (int i=0;i<testEvents.size();i++){
			AceEvent testE=(AceEvent)testEvents.get(i);
			String mod=testE.modality;
			String pol=testE.polarity;
			String gen=testE.genericity;
			String ten=testE.tense;
			
			buf.append("(m:"+mod+"//p:"+pol+"//g:"+gen+"//t:"+ten+")\n");
			
			for (int j=0;j<testE.mentions.size();j++){
				AceEventMention testEm= (AceEventMention)testE.mentions.get(j);
				AceEvent keyE=findEventMention(testEm,keyDoc);
				if (keyE!=null){
					//testE.polarity = "Positive";
					if (testE.polarity.equals(keyE.polarity)){
						correctPol++;
					}
					else{
						/*Annotation s = findContainingSentence(doc,testEm.anchorExtent);
						if (s==null)
							continue;
						String str = doc.text(s);
						str=str.replace("\n", " ");
						str = str.replaceAll("\\b\\s{2,}\\b", "");
						buf.append("("+Integer.toString(j+1)+"//"+testEm.anchorText+"):"+str+"\n");
					*/
						spuriousPol++;
					}
					if (testE.modality.equals(keyE.modality)){
						correctMod++;
					}
					else{
						
						spuriousMod++;
					}
					if (testE.genericity.equals(keyE.genericity)){
						correctGen++;
					}
					else{
						
						spuriousGen++;
					}
					if (testE.tense.equals(keyE.tense)){
						correctTen++;
					}
					else{
						Annotation s = findContainingSentence(doc,testEm.anchorExtent);
						if (s==null)
							continue;
						String str = doc.text(s);
						str=str.replace("\n", " ");
						str = str.replaceAll("\\b\\s{2,}\\b", "");
						buf.append("("+Integer.toString(j+1)+"//"+testEm.anchorText+"):"+str+"\n");
					
						spuriousTen++;
					}
				}
			}
		}
		for (int i=0;i<keyEvents.size();i++){
			AceEvent keyE=(AceEvent)keyEvents.get(i);
			for (int j=0;j<keyE.mentions.size();j++){
				AceEventMention keyEm= (AceEventMention)keyE.mentions.get(j);
				AceEvent testE=findEventMention(keyEm,testDoc);
				if (testE==null){
					missingPol++;
					missingMod++;
					missingGen++;
					missingTen++;
					
				}
			}
		}
		
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		bw.write(buf.toString());
		bw.close();
		
	}
	public void evaluateBypair1(Document doc,AceDocument testDoc,
			AceDocument keyDoc) throws IOException {
		
		ArrayList testEvents = testDoc.events;
		ArrayList keyEvents = keyDoc.events;
		
		for (int i=0;i<testEvents.size();i++){
			AceEvent testE=(AceEvent)testEvents.get(i);
			String mod=testE.modality;
			String pol=testE.polarity;
			String gen=testE.genericity;
			String ten=testE.tense;
			
		//	buf.append("(m:"+mod+"//p:"+pol+"//g:"+gen+"//t:"+ten+")\n");
			
			for (int j=0;j<testE.mentions.size();j++){
				AceEventMention testEm= (AceEventMention)testE.mentions.get(j);
				AceEvent keyE=findEventMention(testEm,keyDoc);
				if (keyE!=null){
					testE.polarity = "Positive";
					if (testE.polarity.equals(keyE.polarity)){
						correctPol++;
					}
					else{
						/*Annotation s = findContainingSentence(doc,testEm.anchorExtent);
						if (s==null)
							continue;
						String str = doc.text(s);
						str=str.replace("\n", " ");
						str = str.replaceAll("\\b\\s{2,}\\b", "");
						buf.append("("+Integer.toString(j+1)+"//"+testEm.anchorText+"):"+str+"\n");
					*/
						spuriousPol++;
					}
					testE.modality = "Asserted";
					if (testE.modality.equals(keyE.modality)){
						correctMod++;
					}
					else{
						
						spuriousMod++;
					}
					testE.genericity = "Specific";
					if (testE.genericity.equals(keyE.genericity)){
						correctGen++;
					}
					else{
						
						spuriousGen++;
					}
					testE.tense = "Past";
					if (testE.tense.equals(keyE.tense)){
						correctTen++;
					}
					else{
						
						spuriousTen++;
					}
				}
			}
		}
		for (int i=0;i<keyEvents.size();i++){
			AceEvent keyE=(AceEvent)keyEvents.get(i);
			for (int j=0;j<keyE.mentions.size();j++){
				AceEventMention keyEm= (AceEventMention)keyE.mentions.get(j);
				AceEvent testE=findEventMention(keyEm,testDoc);
				if (testE==null){
					missingPol++;
					missingMod++;
					missingGen++;
					missingTen++;
				}
			}
		}
		
		/*BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		bw.write(buf.toString());
		bw.close();*/
		
	}
	public AceEvent findEventMention(AceEventMention m,AceDocument aceDoc){
		for (int i=0;i<aceDoc.events.size();i++){
			AceEvent e = aceDoc.events.get(i);
			for (int j=0;j<e.mentions.size();j++){
				AceEventMention em = (AceEventMention)e.mentions.get(j);
				if (em.anchorExtent.equals(m.anchorExtent)){
					return e;
				}
			}
		}
		return null;
	}
	

}
