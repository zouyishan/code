
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

public class EventAttrEval2 {
	static String encoding = "UTF-8";
	int correctPol, missingPol, spuriousPol;
	int correctMod, missingMod, spuriousMod;
	int correctGen, missingGen, spuriousGen;
	int correctTen, missingTen, spuriousTen;
	double polP,polR,polF;
	double modP,modR,modF;
	double genP,genR,genF;
	double tenP,tenR,tenF;

	//static final String home = "/jar/workspace/attr1/corpus/ACE05/";
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	//static final String home ="C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home = "/users/zheng/EventAttr/ACE05/";
	static final String ace = home;
	static final String fileListTest = ace + "testfilelist";
	StringBuffer log1 = new StringBuffer();
	StringBuffer log2 = new StringBuffer();
	StringBuffer log3 = new StringBuffer();
	StringBuffer log4 = new StringBuffer();
	static String keyDir = home+"sources/";
	static String testDir = home+"output/";
	StringBuffer buf = new StringBuffer();
	static String outputfile = home+"attr_log";
	
	public static void main (String[] args) throws IOException {
		
		EventAttrEval2 ea = new EventAttrEval2();
		ea.evalEvents(fileListTest);
		outputfile = home+"attr_log1";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log1.toString());
		bw.close();
		
		outputfile = home+"attr_log2";
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log2.toString());
		bw.close();
		
		outputfile = home+"attr_log3";
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log3.toString());
		bw.close();
		
		outputfile = home+"attr_log4";
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log4.toString());
		bw.close();
		
	}

	/**
	 *  evaluate the event tagger using the documents list in file
	 *  <CODE>fileList</CODE>.
	 */

	public void evalEvents (String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		
		String currentDocPath;
	
		int count1=0,count2=0,count3=0,count4=0;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("Evaluate file " + docCount + ":"
					+ currentDocPath);
			String textFile = keyDir + currentDocPath;
			String xmlFile = keyDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			
			
			AceDocument keyDoc = new AceDocument(textFile, xmlFile);
			
			ArrayList keyEvents = keyDoc.events;
			log1.append(currentDocPath+"\n");
			log2.append(currentDocPath+"\n");
			log3.append(currentDocPath+"\n");
			log4.append(currentDocPath+"\n");
			
			for (int i=0;i<keyEvents.size()-1;i++){
				AceEvent e1= (AceEvent)keyEvents.get(i);
				for (int j=i+1;j<keyEvents.size();j++){
					AceEvent e2= (AceEvent)keyEvents.get(j);
					if (e2.subtype.equals(e1.subtype)){
						if (!e2.modality.equals(e1.modality)){
							count1++;
							log1.append("case "+count1+"\n");
							log1.append("em1: (subtype="+e1.subtype+",modality="+e1.modality+")\n");
							log1.append(e1.mentions.get(0).text.replace("\n", " ")+"\n");
							log1.append("em2: (subtype="+e2.subtype+",modality="+e2.modality+")\n");
							log1.append(e2.mentions.get(0).text.replace("\n", " ")+"\n\n");
						}
						if (!e2.polarity.equals(e1.polarity)){
							count2++;
							log2.append("case "+count2+"\n");
							log2.append("em1: (subtype="+e1.subtype+",polarity="+e1.polarity+")\n");
							log2.append(e1.mentions.get(0).text.replace("\n", " ")+"\n");
							log2.append("em2: (subtype="+e2.subtype+",polarity="+e2.polarity+")\n");
							log2.append(e2.mentions.get(0).text.replace("\n", " ")+"\n\n");
						}
						if (!e2.genericity.equals(e1.genericity)){
							count3++;
							log3.append("case "+count3+"\n");
							log3.append("em1: (subtype="+e1.subtype+",genericity="+e1.genericity+")\n");
							log3.append(e1.mentions.get(0).text.replace("\n", " ")+"\n");
							log3.append("em2: (subtype="+e2.subtype+",genericity="+e2.genericity+")\n");
							log3.append(e2.mentions.get(0).text.replace("\n", " ")+"\n\n");
						}
						if (!e2.tense.equals(e1.tense)){
							count4++;
							log4.append("case "+count4+"\n");
							log4.append("em1: (subtype="+e1.subtype+",tense="+e1.tense+")\n");
							log4.append(e1.mentions.get(0).text.replace("\n", " ")+"\n");
							log4.append("em2: (subtype="+e2.subtype+",tense="+e2.tense+")\n");
							log4.append(e2.mentions.get(0).text.replace("\n", " ")+"\n\n");
						}
					}
				}
			}
			
		}
		reader.close();
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
