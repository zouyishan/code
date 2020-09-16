package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.ace.AceEventMention;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


public class EventStatistics {
	static String encoding = "UTF-8";
	static String home ="C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	static String fileList = home+"totalfilelist";
	static String dataDir = home+"sources/";
	static String mapDir = home+"map/";
	static String posmapDir = home+"posmap/";
	static String outputfile = home+"es";
	
	int absLength;
	int aceLength;
	
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
	
	public static void main1(String[] args) throws Exception {
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		Pat.trace = false;
		Resolve.trace = false;
		AceDocument.ace2005 = true;
		
		int mCount = 0;
		
		TreeMap modality = new TreeMap();
		TreeMap polarity = new TreeMap();
		TreeMap genericity = new TreeMap();
		TreeMap tense = new TreeMap();
		StringBuffer buf = new StringBuffer();
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
			Control.processDocument (doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			
			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			
			for (int i = 0; i < events.size(); i++) {
				AceEvent event = (AceEvent) events.get(i);
				
				mCount+=event.mentions.size();
				String mod=event.modality;
				String pol=event.polarity;
				String gen=event.genericity;
				String ten=event.tense;
				
				buf.append("(m:"+mod+"//p:"+pol+"//g:"+gen+"//t:"+ten+")\n");
				for (int j=0;j<event.mentions.size();j++){
					Annotation s = findContainingSentence(doc,event.mentions.get(j).anchorExtent);
					if (s==null)
						continue;
					String str = doc.text(s);
					str=str.replace("\n", "");
					str = str.replaceAll("\\b\\s{2,}\\b", "");
					buf.append("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
				}
				
				
				if (!modality.containsKey(mod))
					modality.put(mod, new Integer(0));
				Integer count = (Integer)modality.get(mod);
				modality.put(mod, new Integer(count+event.mentions.size()));
				
				if (!polarity.containsKey(pol))
					polarity.put(pol, new Integer(0));
				count = (Integer)polarity.get(pol);
				polarity.put(pol, new Integer(count+event.mentions.size()));
				
				if (!genericity.containsKey(gen))
					genericity.put(gen, new Integer(0));
				count = (Integer)genericity.get(gen);
				genericity.put(gen, new Integer(count+event.mentions.size()));
				
				if (!tense.containsKey(ten))
					tense.put(ten, new Integer(0));
				count = (Integer)tense.get(ten);
				tense.put(ten, new Integer(count+event.mentions.size()));
			}
			
		}
		
		System.out.println("Event mention count="+mCount);
		System.out.println(modality);
		System.out.println(polarity);
		System.out.println(genericity);
		System.out.println(tense);
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		bw.write(buf.toString());
		bw.close();
	}
	
	public static void main(String[] args) throws Exception {
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
	
		int mCount = 0;
	
		int count1=0;//specific, argument size>1
		int count2=0;//specific, argument size=1
		int count3=0;//generic, argument size>1
		int count4=0;//generic, argument size=1
		StringBuffer buf = new StringBuffer();
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
		/*	ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Control.processDocument (doc, null, false, 0);
		*/
			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			
			for (int i = 0; i < events.size(); i++) {
				AceEvent event = (AceEvent) events.get(i);
				
				mCount+=event.mentions.size();
				String mod=event.modality;
				String pol=event.polarity;
				String gen=event.genericity;
				String ten=event.tense;
				
				buf.append("(m:"+mod+"//p:"+pol+"//g:"+gen+"//t:"+ten+")\n");
				for (int j=0;j<event.mentions.size();j++){
				/*	Annotation s = findContainingSentence(doc,event.mentions.get(j).anchorExtent);
					if (s==null)
						continue;
					String str = doc.text(s);
					str=str.replace("\n", " ");
					str = str.replaceAll("\\b\\s{2,}\\b", "");*/
					/*	buf.append("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
				*/
					boolean time=false;
					AceEventMention em=event.mentions.get(j);
					for (int k=0;k<em.arguments.size();k++){
						if (em.arguments.get(k).role.contains("Place")){
							time=true;
						}
					}
					if (mod.equals("Asserted")&&time){
						count1++;
					}
					else if  (mod.equals("Asserted")&&!time){
						count2++;
//					/	System.out.print("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
					}
					else if  (mod.equals("Other")&&time){
						count3++;
					}
					else if  (mod.equals("Other")&&!time){
						count4++;
					}
					/*if (gen.equals("Specific")&&time){
						count1++;
					}
					else if  (gen.equals("Specific")&&!time){
						count2++;
//					/	System.out.print("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
					}
					else if  (gen.equals("Generic")&&time){
						count3++;
					}
					else if  (gen.equals("Generic")&&!time){
						count4++;
					}*/
					
					/*if (gen.equals("Specific")&&em.arguments.size()>0){
						count1++;
					}
					else if  (gen.equals("Specific")&&em.arguments.size()==0){
						count2++;
//					/	System.out.print("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
					}
					else if  (gen.equals("Generic")&&em.arguments.size()>0){
						count3++;
					}
					else if  (gen.equals("Generic")&&em.arguments.size()==0){
						count4++;
					}*/
					/*if (!ten.equals("Unspecified")&&time){
						count1++;
					}
					else if  (!ten.equals("Unspecified")&&!time){
						count2++;
//					/	System.out.print("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
					}
					else if  (ten.equals("Unspecified")&&time){
						count3++;
					//	System.out.print("("+Integer.toString(j+1)+"//"+event.mentions.get(j).anchorText+"):"+str+"\n");
						
					}
					else if  (ten.equals("Unspecified")&&!time){
						count4++;
					}*/
				}
				
				
				
			}
			
		}
		
		System.out.println("Event mention count="+mCount);
		System.out.println(count1);
		System.out.println(count2);
		System.out.println(count3);
		System.out.println(count4);
		/*BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		bw.write(buf.toString());
		bw.close();*/
	}
	
	
}