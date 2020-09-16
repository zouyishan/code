package cuny.blender.englishie.nlp.tool;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.ace.AceEventMention;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


public class EventCorefStatistics {
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	//static String encoding = "UTF-8";
	static String fileList = home+"totalfilelist";
	static String dataDir = home+"source/";
	static String logFileName = home+"coreflog3";
	

	
	public static void main1(String[] args) throws Exception {
		
		JetTest.initializeFromConfig("props/MEace06.properties");
		Pat.trace = false;
		Resolve.trace = false;
		
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
	
		FileWriter fw = new FileWriter(logFileName);
		
		int count1 = 0;
		int count2 = 0;
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			Control.processDocument (doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent e = (AceEvent)events.get(i);
				for (int j=0;j<e.mentions.size()-1;j++){
					AceEventMention em1 = e.mentions.get(j);
					Annotation sentence1 = findContainingSentence(doc, em1.anchorExtent);
					if (sentence1==null){
						System.out.println("sentence1 is not found");
						continue;
					}
						
					for (int k=j+1;k<e.mentions.size();k++){
						AceEventMention em2 = e.mentions.get(k);
						Annotation sentence2 = findContainingSentence(doc, em2.anchorExtent);
						count1++;
						if (sentence2==null){
							System.out.println("sentence2 is not found");
							continue;
						}
						if (sentence1.span().equals(sentence2.span())){
							count2++;
							fw.write(count2+"\n"+e.subtype+"\nem1:\nSUM:"+em1.toString()+"\nTEXT:"+em1.text+"\nSEN:"+doc.text(sentence1)+"\nem2:\nSUM:"+em2.toString()+"\nTEXT:"+em2.text+"\nSEN:"+doc.text(sentence2)+"\n");
						}
					}
				}
			}
		}
		reader.close();
		
		fw.close();
		System.out.println("Total:"+count1);
		System.out.println("Found:"+count2);
	}
	
	public static void main(String[] args) throws Exception {
		
		JetTest.initializeFromConfig("props/MEace06.properties");
		Pat.trace = false;
		Resolve.trace = false;
		
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
	
		FileWriter fw = new FileWriter(logFileName);
		
		int count1 = 0;
		int count2 = 0;
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			Control.processDocument (doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size()-1;i++)
			{
				AceEvent e1 = (AceEvent)events.get(i);
				for (int j=0;j<e1.mentions.size();j++){
					AceEventMention em1 = e1.mentions.get(j);
					Annotation sentence1 = findContainingSentence(doc, em1.anchorExtent);
					if (sentence1==null){
						System.out.println("sentence1 is not found");
						continue;
					}
					for (int m=i+1;m<events.size();m++){
						AceEvent e2 = (AceEvent)events.get(m);
						if (e2.subtype.equals(e1.subtype)){
							for (int n=0;n<e2.mentions.size();n++){
								AceEventMention em2 = e2.mentions.get(n);
								Annotation sentence2 = findContainingSentence(doc, em2.anchorExtent);
								if (sentence2==null){
									System.out.println("sentence2 is not found");
									continue;
								}
								count1++;
								if (sentence1.span().equals(sentence2.span())){
									count2++;
									fw.write(count2+"\n"+e1.subtype+"\nem1:\nSUM:"+em1.toString()+"\nTEXT:"+em1.text+"\nSEN:"+doc.text(sentence1)+"\nem2:\nSUM:"+em2.toString()+"\nTEXT:"+em2.text+"\nSEN:"+doc.text(sentence2)+"\n");
															}
							}	
						}
					}
				}
			}
		}
		reader.close();
		
		fw.close();
		System.out.println("Total:"+count1);
		System.out.println("Found:"+count2);
	}

	private static Annotation findContainingSentence(Document doc, Span span) {
		Vector sentences = doc.annotationsOfType("sentence");
		if (sentences == null) {
			System.err.println("findContainingSentence:  no sentences found");
			return null;
		}
		Annotation sentence = null;
		for (int i = 0; i < sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			if (span.within(s.span())) {
				sentence = s;
				break;
			}
		}
		if (sentence == null) {
			System.err
					.println("findContainingSentence:  can't find sentence with span");
			return null;
		}
		return sentence;
	}
	
}