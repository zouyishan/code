
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

public class EventAttrEval3 {
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
	static String testDir = home+"output/attr/output1/";
	StringBuffer buf = new StringBuffer();
	static String outputfile = home+"attr_log";
	
	public static void main (String[] args) throws IOException {
		
		EventAttrEval3 ea = new EventAttrEval3();
		ea.evalEvents(fileListTest);
		outputfile = home+"attr_log5";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log1.toString());
		bw.close();
		
		outputfile = home+"attr_log6";
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log2.toString());
		bw.close();
		
		outputfile = home+"attr_log7";
		bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputfile), "UTF-8"));
		bw.flush();
		bw.write(ea.log3.toString());
		bw.close();
		
		outputfile = home+"attr_log8";
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
			String testFile = testDir
			+ currentDocPath.replaceFirst(".sgm", ".apf");
			
			AceDocument keyDoc = new AceDocument(textFile, xmlFile);
			AceDocument testDoc = new AceDocument(textFile, testFile);
			
			ArrayList keyEvents = keyDoc.events;
			ArrayList testEvents = testDoc.events;
			
			log1.append(currentDocPath+"\n");
			log2.append(currentDocPath+"\n");
			log3.append(currentDocPath+"\n");
			log4.append(currentDocPath+"\n");
			
			for (int i=0;i<testEvents.size();i++){
				AceEvent e = (AceEvent)testEvents.get(i);
				if (e.mentions.size()==1)
					continue;
				for (int j=0;j<e.mentions.size()-1;j++){
					AceEventMention em1= e.mentions.get(j);
					AceEvent e1=findStrictEvent(em1,keyEvents);
					for (int k=j+1;k<e.mentions.size();k++){
						AceEventMention em2= e.mentions.get(k);
						AceEvent e2=findStrictEvent(em2,keyEvents);
						if (!e1.modality.equals(e2.modality)){
							count1++;
							log1.append("case "+count1+"\n");
							log1.append("em1: (subtype="+e1.subtype+",modality="+e1.modality+")\n");
							log1.append(em1.text.replace("\n", " ")+"\n");
							log1.append("em2: (subtype="+e2.subtype+",modality="+e2.modality+")\n");
							log1.append(em2.text.replace("\n", " ")+"\n\n");
						}
						if (!e1.polarity.equals(e2.polarity)){
							count2++;
							log2.append("case "+count2+"\n");
							log2.append("em1: (subtype="+e1.subtype+",polarity="+e1.polarity+")\n");
							log2.append(em1.text.replace("\n", " ")+"\n");
							log2.append("em2: (subtype="+e2.subtype+",polarity="+e2.polarity+")\n");
							log2.append(em2.text.replace("\n", " ")+"\n\n");
						}
						if (!e1.genericity.equals(e2.genericity)){
							count3++;
							log3.append("case "+count3+"\n");
							log3.append("em1: (subtype="+e1.subtype+",genericity="+e1.genericity+")\n");
							log3.append(em1.text.replace("\n", " ")+"\n");
							log3.append("em2: (subtype="+e2.subtype+",genericity="+e2.genericity+")\n");
							log3.append(em2.text.replace("\n", " ")+"\n\n");
						}
						if (!e1.tense.equals(e2.tense)){
							count4++;
							log4.append("case "+count4+"\n");
							log4.append("em1: (subtype="+e1.subtype+",tense="+e1.tense+")\n");
							log4.append(em1.text.replace("\n", " ")+"\n");
							log4.append("em2: (subtype="+e2.subtype+",tense="+e2.tense+")\n");
							log4.append(em2.text.replace("\n", " ")+"\n\n");
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
	
	
	private AceEvent findStrictEvent(AceEventMention em,
			ArrayList events) {
		AceEvent baseEvent;
		for (int i = 0; i < events.size(); i++) {
			baseEvent = (AceEvent) events.get(i);
			if (!em.event.subtype.equals(baseEvent.subtype))
				continue;
			ArrayList mentions = baseEvent.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention mention = (AceEventMention) mentions.get(j);
				Span keyAnchorExtent = mention.anchorExtent;
				
				if (em.anchorExtent.equals(keyAnchorExtent))
					return baseEvent;
			}
		}
		return null;
	}
	

}
