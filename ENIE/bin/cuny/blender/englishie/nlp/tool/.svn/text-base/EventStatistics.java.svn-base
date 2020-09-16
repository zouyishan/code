package cuny.blender.englishie.nlp.tool;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.Ace;
import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEntity;
import cuny.blender.englishie.ace.AceEntityMention;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.ace.AceEventMention;
import cuny.blender.englishie.ace.AceRelation;
import cuny.blender.englishie.ace.AceRelationMention;
import cuny.blender.englishie.ace.Gazetteer;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


public class EventStatistics {
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	//static String encoding = "UTF-8";
	static String fileList = home+"totalfilelist";
	static String dataDir = home+"source/";
	static String xmlDir = home+"source/";
	static String outputDir = home+"coref/tac09/";
	static String logFileName = home+"divorce";
	
	static Stemmer ps = Stemmer.getDefaultStemmer();
	

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
	
	
	static int findContainingSentence1(Document doc, Span span) {
		Vector sentences = doc.annotationsOfType("sentence");
		if (sentences == null) {
			System.err.println("findContainingSentence:  no sentences found");
			return -1;
		}
		Annotation sentence = null;
		for (int i = 0; i < sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			//if (span.within(s.span())) {
			if (span.start()>=s.span().start()&&span.start()<s.span().end()) {
				sentence = s;
				return i;
			}
		}
		
		System.err
				.println("findContainingSentence:  can't find sentence with span");
		return -1;
		
		
	}
	
	
	static Annotation findPreviousSentence(Document doc, Span span) {
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
				if (i==0)
					return null;
				else
					sentence = (Annotation) sentences.get(i-1);
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
	
	static AceEntityMention findContainingEntity(AceEntity entity, Span span, Span entitySpan) {
		
		for (int j=0;j<entity.mentions.size();j++){
			AceEntityMention em = (AceEntityMention)entity.mentions.get(j);
			if (em.head.equals(entitySpan))
				continue;
			if (em.head.within(span))
				return em;
			
		}
		
		return null;
	}
	
	static AceEventMention findContainingEvent(AceEvent event, Span span, Span entitySpan) {
		
		for (int j=0;j<event.mentions.size();j++){
			AceEventMention em = (AceEventMention)event.mentions.get(j);
			if (em.anchorExtent.equals(entitySpan))
				continue;
			if (em.anchorExtent.within(span))
				return em;
			
		}
		
		return null;
	}
	
	public static void main1(String[] args) throws Exception {
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		
		int count1 = 0;
		int count2 = 0;
		HashMap <Integer,Integer> map= new HashMap<Integer,Integer>();
 		FileWriter fw = new FileWriter(logFileName);
		
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
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			
			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent event = (AceEvent) events.get(i);
				for (int j=0;j<event.mentions.size();j++){
					count1++;
					AceEventMention em = (AceEventMention)event.mentions.get(j);
					String str = em.anchorText.replace("\n", " ").toLowerCase();
					if (str.equals("it") /*&&event.mentions.size()>=1*/){
						count2++;
						//int pos1 = findContainingSentence1(doc,em.anchorExtent);
						Annotation s = findContainingSentence(doc,em.anchorExtent);
						AceEventMention em2=findContainingEvent(event,s.span(),em.anchorExtent);
						
						/*String str1 = doc.text(s);
						String str2=str1.substring(0,em.anchorExtent.end()+1-s.span().start());
						String str3=str1.substring(em.anchorExtent.end()+1-s.span().start());
						
						String str5 = str2+"(Event:"+em.event.subtype+")"+str3;
						str5=str5.replace("\n", " ");
						str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
						fw.write(str5+"\n");
						count1++;*/
					/*	int min = Integer.MAX_VALUE;
						for (int k=0;k<event.mentions.size();k++){
							AceEventMention emk = (AceEventMention)event.mentions.get(k);
							if (emk.anchorExtent.equals(em.anchorExtent))
								continue;
							int pos2 = findContainingSentence1(doc,emk.anchorExtent);
							
							if (pos2<=pos1&&pos1-pos2<min){
								min = pos1-pos2;
							}
						}
						if (map.containsKey(min))
						{
							int count4 = map.get(min);
							map.put(min, new Integer(count4+1));
						}
						else{
							map.put(min, 1);
						}*/
						if (em2!=null){
							/*String str1 = doc.text(s);
							String str2=str1.substring(0,em.anchorExtent.end()+1-s.span().start());
							String str3=str1.substring(em.anchorExtent.end()+1-s.span().start());
							String str4=em2.toString();
							String str5 = str2+"(Event:"+str4+")"+str3;
							str5=str5.replace("\n", " ");
							str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
							fw.write(str5+"\n");
							count1++;
							System.out.println(count1);*/
						}
						//else{
							/*Annotation s2 = findPreviousSentence(doc,em.anchorExtent);
							if (s2==null)
								continue;
							em2=findContainingEvent(event,s2.span(),em.anchorExtent);
							if (em2!=null){
								Span s3= new Span(s2.start(),s.end());
								String str1 = doc.text(s3);
								String str2=str1.substring(0,em.anchorExtent.end()+1-s3.start());
								String str3=str1.substring(em.anchorExtent.end()+1-s3.start());
								String str4=em2.toString();
								String str5 = str2+"(Event:"+str4+")"+str3;
								str5=str5.replace("\n", " ");
								str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
								fw.write(str5+"\n");
								count1++;
								System.out.println(count1);
							}*/
						//}
					}
						
				} 
			}
		}
		fw.close();
		//System.out.println(map);
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
	}
	
	public static void main2(String[] args) throws Exception {
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		
		int count1 = 0;
		int count2 = 0;
		
		FileWriter fw = new FileWriter(logFileName);
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			fw.write("DOC " + docCount + " : " + currentDoc);
			fw.write("<ul>");
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			
			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent event = (AceEvent) events.get(i);
				for (int j=0;j<event.mentions.size();j++){
					count2++;
					AceEventMention em = (AceEventMention)event.mentions.get(j);
					String str = em.anchorText.replace("\n", " ").toLowerCase();
					if (str.equals("it") &&event.mentions.size()>1){
						Annotation s = findContainingSentence(doc,em.anchorExtent);
						AceEventMention em2=findContainingEvent(event,s.span(),em.anchorExtent);
						
						if (em2!=null){
							String str1 = doc.text(s);
							String str2=str1.substring(0,em.anchorExtent.end()+1-s.span().start());
							String str3=str1.substring(em.anchorExtent.end()+1-s.span().start());
							String str4=em2.toString();
							String str5 = str2+"(Entity:"+str4+")"+str3;
							str5=str5.replace("\n", " ");
							str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
							fw.write(str5+"\n");
							count1++;
							System.out.println(count1);
						}
						
						if (em2!=null){
							fw.write("<li>");
							String str1 = doc.text(s);
							String str5="";
							if (em.anchorExtent.end()<em2.anchorExtent.start())
								str5=getHTML(str1,em,em2,s.span());
							else
								str5=getHTML(str1,em2,em,s.span());
							str5=str5.replace("\n", " ");
							str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
							fw.write(str5+"</br></li>");
							count1++;
							System.out.println(count1);
						}
						else{
							Annotation s2 = findPreviousSentence(doc,em.anchorExtent);
							if (s2==null)
								continue;
							em2=findContainingEvent(event,s2.span(),em.anchorExtent);
							if (em2!=null){
								
								String str1 = doc.text(new Span(s2.start(),s.end()));
								String str5="";
								str5=getHTML(str1,em2,em, new Span(s2.start(),s.end()));
								str5=str5.replace("\n", " ");
								str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
								fw.write(str5+"</br></li>");
								count1++;
								System.out.println(count1);
							}
						}
						
						
					}
						
				} 
			}
			fw.write("</ul>");
		}
		fw.close();
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
	}
	
	public static void main4(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
			
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		
		FileWriter fw = new FileWriter(logFileName);
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		
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
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
			ArrayList entities = aceDoc.entities;
			if (entities.size()==0)
			{
				continue;
			}
			for (int i=0;i<entities.size();i++)
			{
				AceEntity entity = (AceEntity) entities.get(i);
				for (int j=0;j<entity.mentions.size();j++){
					count2++;
					AceEntityMention em1 = (AceEntityMention)entity.mentions.get(j);
					String str = em1.getHeadText().replace("\n", " ").toLowerCase();
					if (str.equals("it")/*&&entity.mentions.size()>1*/){
						count3++;
						Annotation s = findContainingSentence(doc,em1.head);
						AceEntityMention em2=findContainingEntity(entity,s.span(),em1.head);
						if (em2!=null){
							
							String str1 = doc.text(s);
							String str2=str1.substring(0,em1.head.end()+1-s.span().start());
							String str3=str1.substring(em1.head.end()+1-s.span().start());
							String str4=doc.text(new Span(em2.head.start(),em2.head.end()+1));
							
							String str5 = str2+"(Entity:"+str4+")"+str3;
							str5=str5.replace("\n", " ");
							str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
							fw.write(str5+"\n");
							count1++;
							System.out.println(count1);
						}
						else{
							Annotation s2 = findPreviousSentence(doc,em1.head);
							if (s2==null)
								continue;
							em2=findContainingEntity(entity,s2.span(),em1.head);
							if (em2!=null){
								Span s3= new Span(s2.start(),s.end());
								String str1 = doc.text(s3);
								String str2=str1.substring(0,em1.head.end()+1-s3.start());
								String str3=str1.substring(em1.head.end()+1-s3.start());
								String str4=doc.text(new Span(em2.head.start(),em2.head.end()+1));
								String str5 = str2+"(Entity:"+str4+")"+str3;
								str5=str5.replace("\n", " ");
								str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
								fw.write(str5+"\n");
								//count3++;
								System.out.println(count1);
							}
						} 
						
						//.out.println(str1);
					}
				} 
			}
		}
		fw.close();
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
		System.out.println("count2 " + count3);
	}

	public static AceEntityMention findContainingEntity(AceDocument aceDoc,Span s){
		ArrayList entities = aceDoc.entities;
		
		for (int i=0;i<entities.size();i++)
		{
			AceEntity entity = (AceEntity) entities.get(i);
			for (int j=0;j<entity.mentions.size();j++){
				AceEntityMention em1 = (AceEntityMention)entity.mentions.get(j);
				if (em1.head.within(s)){
					return em1;
				}
			}
			
		}
		return null;
	}
	
	public static AceEventMention findContainingEvent(AceDocument aceDoc,Span s){
		ArrayList events = aceDoc.events;
		
		for (int i=0;i<events.size();i++)
		{
			AceEvent event = (AceEvent) events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention em1 = (AceEventMention)event.mentions.get(j);
				if (em1.anchorExtent.within(s)){
					return em1;
				}
			}
			
		}
		return null;
	}
	
	public static AceEventMention findItEvent(AceDocument aceDoc,Span s){
		ArrayList events = aceDoc.events;
		
		for (int i=0;i<events.size();i++)
		{
			AceEvent event = (AceEvent) events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention em1 = (AceEventMention)event.mentions.get(j);
				if (em1.anchorExtent.within(s)/*&&event.mentions.size()>1*/){
					return em1;
				}
			}
			
		}
		return null;
	}
	public static AceEventMention findContainingEvent(AceDocument aceDoc,Span s, Span sentence){
		ArrayList events = aceDoc.events;
		
		for (int i=0;i<events.size();i++)
		{
			AceEvent event = (AceEvent) events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention em1 = (AceEventMention)event.mentions.get(j);
				if (!em1.anchorExtent.within(s)&&em1.anchorExtent.within(sentence)){
					return em1;
				}
			}
		}
		return null;
	}
	
	public static void main3(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
			
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		int count1=0;
		int count2=0;
		int count3=0;
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = xmlDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
			FileWriter fw = new FileWriter(outputDir+currentDoc.replaceFirst(".sgm", ".coref"));
			//StringBuffer buf = new StringBuffer();
			Vector sentences = doc.annotationsOfType("sentence");
			
			Annotation sentence = null;
			for (int i = 0; i < sentences.size(); i++) {
				Annotation s = (Annotation) sentences.get(i);
				String str1 = doc.text(s);
				int offset = 0;
				Vector<Annotation> tokens = doc.annotationsOfType("token",s.span());
				if (tokens != null) {
					for (Annotation token : tokens) {
						doc.shrink(token);
						Span tokenSpan = token.span();
						String term = doc.text(tokenSpan).toLowerCase().trim();
						if (term.equals("it")){
							count1++;
							/*AceEntityMention em1 = findContainingEntity(aceDoc,tokenSpan);
							if (em1!=null){
								String str2=str1.substring(0,em1.head.end()+1-s.span().start()+offset);
								String str3=str1.substring(em1.head.end()+1-s.span().start()+offset);
								String str4= "";
								for (int j=0;j<em1.entity.mentions.size();j++){
									
									AceEntityMention em2 =(AceEntityMention)em1.entity.mentions.get(j);
									if (em2.head.equals(em1.head)||em2.head.start()>em1.head.start())
										continue;
									else if (em2.head.within(s.span())){
										str4=str4+em2.getHeadText();
										break;
									}
								}
								if (str4.isEmpty()){
									Annotation s2= findPreviousSentence(doc, em1.head);
									for (int j=0;j<em1.entity.mentions.size();j++){
										
										AceEntityMention em2 =(AceEntityMention)em1.entity.mentions.get(j);
										
										if (em2.head.within(s2.span())){
											str4=str4+em2.getHeadText();
											break;
										}
									}
								}
								
							//	str4=str4.substring(0,str4.length()-1);
								if (!str4.isEmpty()){
									str1 = str2+"(Entity:"+str4+")"+str3;
									offset +=str4.length()+9;
									count2++;
									System.out.println(count2);
								}
								else{
									str1 = str2+str3;
								}
								
								
							}
							AceEventMention em2 = findContainingEvent(aceDoc,tokenSpan);
							if (em2!=null){
								String str2=str1.substring(0,em2.anchorExtent.end()+1-s.span().start()+offset);
								String str3=str1.substring(em2.anchorExtent.end()+1-s.span().start()+offset);
								String str4= "";
								AceEventMention em4=null;
								int span=0;
								for (int j=0;j<em2.event.mentions.size();j++){
									AceEventMention em3 =(AceEventMention)em2.event.mentions.get(j);
									if (em3.anchorExtent.equals(em2.anchorExtent)||em3.anchorExtent.start()>em2.anchorExtent.start())
										continue;
									else if (em3.anchorExtent.start()>span){
										span =em3.anchorExtent.start();
										em4= em3;
									}
										
								}
								if (em4!=null)
									str4=str4+em4.anchorText;
								//str4=str4.substring(0,str4.length()-1);
								
								str1 = str2+"(Event:"+str4+")"+str3;
								offset +=str4.length()+8;
								count3++;
								System.out.println(count3);
							}*/
						}
					}
				}
				//fw.append(str1);
			}
			fw.close();
		}
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
		System.out.println("count3 " + count3);
	}
	
	
	public static void main17(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
			
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		int count1=0;
		int count2=0;
		int count3=0;
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = xmlDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
		//	FileWriter fw = new FileWriter(outputDir+currentDoc.replaceFirst(".sgm", ".coref"));
			//StringBuffer buf = new StringBuffer();
			Vector sentences = doc.annotationsOfType("sentence");
			
			Annotation sentence = null;
			for (int i = 0; i < sentences.size(); i++) {
				Annotation s = (Annotation) sentences.get(i);
				String str1 = doc.text(s);
				int offset = 0;
				Vector<Annotation> tokens = doc.annotationsOfType("token",s.span());
				if (tokens != null) {
					for (Annotation token : tokens) {
						doc.shrink(token);
						Span tokenSpan = token.span();
						String term = doc.text(tokenSpan).toLowerCase().trim();
						if (term.equals("it")){
							count1++;
							AceEntityMention em1 = findContainingEntity(aceDoc,tokenSpan);
							if (em1!=null){
								count2++;
							}
							AceEventMention em2 = findContainingEvent(aceDoc,tokenSpan);
							if (em2!=null){
								count3++;
							}
						}
					}
				}
			//	fw.append(str1);
			}
		//	fw.close();
		}
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
		System.out.println("count3 " + count3);
	}
	
	public static void main12(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
			
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		int count1=0;
		int count2=0;
		int count3=0;
		
		FileWriter fw = new FileWriter(logFileName);
		
		while ((currentDoc = reader.readLine()) != null) {
			docCount++;
			// process file 'currentDoc'
			fw.write("DOC " + docCount + " : " + currentDoc);
			fw.write("<ul>");
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = xmlDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
			
			//StringBuffer buf = new StringBuffer();
			Vector sentences = doc.annotationsOfType("sentence");
			
			Annotation sentence = null;
			for (int i = 0; i < sentences.size(); i++) {
				Annotation s = (Annotation) sentences.get(i);
				String str1 = doc.text(s);
				int offset = 0;
				Vector<Annotation> tokens = doc.annotationsOfType("token",s.span());
				if (tokens != null) {
					for (Annotation token : tokens) {
						doc.shrink(token);
						Span tokenSpan = token.span();
						String term = doc.text(tokenSpan).toLowerCase().trim();
						if (term.equals("it")){
							count1++;
							AceEventMention em1 = findItEvent(aceDoc,tokenSpan);
							
							AceEventMention em2 = findContainingEvent(aceDoc,tokenSpan,s.span());
							if (em1!=null&&em2!=null){
								fw.write("<li>");
								
								String str5="";
								if (em1.anchorExtent.start()<em2.anchorExtent.start())
									str5=getHTML(str1,em1,em2, s.span());
								else
									str5=getHTML(str1,em2,em1, s.span());
								fw.write(str5+"</br></li>");
								
							}
							else if (em1==null&&em2!=null){
								fw.write("<li>");
								
								String str5="";
								if (tokenSpan.start()<em2.anchorExtent.start())
									str5=getHTML1(str1,tokenSpan,em2, s.span());
								else
									str5=getHTML2(str1,tokenSpan,em2, s.span());
								fw.write(str5+"</br></li>");
								
							} 
						}
					}
				}
				
			}
			fw.write("</ul>");
			
		}
		fw.close();
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
		System.out.println("count3 " + count3);
		
	}
	
	public static void main5(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
			
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		
		FileWriter fw = new FileWriter(logFileName);
		int count1 = 0;
		int count2 = 0;
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			fw.write("DOC " + docCount + " : " + currentDoc);
			fw.write("<ul>");
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
			ArrayList entities = aceDoc.entities;
			if (entities.size()==0)
			{
				continue;
			}
			for (int i=0;i<entities.size();i++)
			{
				AceEntity entity = (AceEntity) entities.get(i);
				for (int j=0;j<entity.mentions.size();j++){
					count2++;
					AceEntityMention em1 = (AceEntityMention)entity.mentions.get(j);
					String str = em1.getHeadText().replace("\n", " ").toLowerCase();
					if (str.equals("it")&&entity.mentions.size()>1){
						Annotation s = findContainingSentence(doc,em1.head);
						AceEntityMention em2=findContainingEntity(entity,s.span(),em1.head);
						if (em2!=null){
							/*fw.write("<li>");
							String str1 = doc.text(s);
							String str5="";
							if (em1.head.end()<em2.head.start())
								str5=getHTML(str1,em1,em2,s.span());
							else
								str5=getHTML(str1,em2,em1,s.span());
							str5=str5.replace("\n", " ");
							str5 = str5.replaceAll("\\b\\s{2,}\\b", " ");
							fw.write(str5+"</br></li>");
							count1++;
							System.out.println(count1);*/
						}
						else{
							Annotation s2 = findPreviousSentence(doc,em1.head);
							if (s2==null)
								continue;
							em2=findContainingEntity(entity,s2.span(),em1.head);
							if (em2!=null){
								fw.write("<li>");
								String str1 = doc.text(new Span(s2.start(),s.end()));
								String str5="";
								str5=getHTML(str1,em2,em1, new Span(s2.start(),s.end()));
								fw.write(str5+"</br></li>");
								count1++;
								System.out.println(count1);
							}
						}
						
						//.out.println(str1);
					}
				} 
			}
			fw.write("</ul>");
		}
		
		fw.close();
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
	}
	
	public static void main16 (String[] args) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		FileWriter fw = new FileWriter(logFileName);
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			boolean bFound = false;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent event = (AceEvent) events.get(i);
				for (int j=0;j<event.mentions.size();j++){
					AceEventMention em1 = (AceEventMention)event.mentions.get(j);
					String str = em1.anchorText.replace("\n", " ").toLowerCase();
					if (str.equals("it")){
						bFound = true;
						break;
					}
				}
				if (bFound)
					break;
			}
			if (bFound)
				fw.write(currentDoc+"\n");
		}
		fw.close();
	}

	
	public static void main10(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
			
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		
		FileWriter fw = new FileWriter(logFileName);
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		int count4 = 0;
		
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			fw.write("DOC " + docCount + " : " + currentDoc);
			fw.write("<ul>");
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent event = (AceEvent) events.get(i);
				for (int j=0;j<event.mentions.size();j++){
					AceEventMention em1 = (AceEventMention)event.mentions.get(j);
					count1++;
					/*Vector constituents = doc.annotationsOfType("constit");
					for (int k=0; k<constituents.size(); k++) {
						Annotation constit = (Annotation) constituents.get(k);
						if (constit.span().overlap(em1.head)){
							String cat = (String) constit.get("cat");
							if (cat == "v" || cat == "tv" || cat == "ven") {
								count2++;
								break;
							}
						}
					}*/
					String cat = getAnchorCat(em1.anchorExtent, doc);
					if (cat == "v" || cat == "tv" || cat == "ven"|| cat == "ving") {
						count2++;
						
					}
					if (cat == "n") {
						count3++;
						
					}
					if (cat == "adj") {
						count4++;
						
					}
				} 
			}
			fw.write("</ul>");
		}
		
		fw.close();
		System.out.println("count1 " + count1);
		System.out.println("count2 " + count2);
		System.out.println("count3 " + count3);
		System.out.println("count4 " + count4);
	}
	
	public static void main7(String[] args) throws Exception {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		
		//Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
		
		EventStatistics es = new EventStatistics();
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int noEventDocCount = 0;
		int eventCount = 0;
		int mentionCount = 0;
		String currentDoc;
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (logFileName), "urf-8"));
		
		TreeMap eventByType = new TreeMap();
		TreeMap eventBySubtype = new TreeMap();
		TreeMap mentionByType = new TreeMap();
		TreeMap mentionBySubtype = new TreeMap();
		TreeMap anchorBySubtype = new TreeMap();
		TreeMap posType = new TreeMap();
		StringBuffer buf = new StringBuffer();
		buf.append("Starting Processing...\n");
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			buf.append("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
			buf.append("Processing document " + docCount + " : " + currentDoc+"\n");
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				noEventDocCount++;
				buf.append("No events in this document,skipped...\n");
				continue;
			}
			
			
			
			for (int i = 0; i < events.size(); i++) {
				eventCount++;
				AceEvent event = (AceEvent) events.get(i);
				buf.append("-------------------------\n");
				buf.append("Event type: " + event.type + "\n");
				buf.append("Event subtype: " + event.subtype + "\n");
				if (!eventByType.containsKey(event.type))
					eventByType.put(event.type, new Integer(1));
				else{
					int count;
					Integer countI = (Integer)eventByType.get(event.type);
					if (countI == null)
						count = 0;
					else
						count = countI.intValue();
					eventByType.put(event.type, new Integer(count+1));
				}
				
				if (!eventBySubtype.containsKey(event.subtype))
					eventBySubtype.put(event.subtype, new Integer(1));
				else{
					int count;
					Integer countI = (Integer)eventBySubtype.get(event.subtype);
					if (countI == null)
						count = 0;
					else
						count = countI.intValue();
					eventBySubtype.put(event.subtype, new Integer(count+1));
				}
				
				ArrayList mentions = event.mentions;
				mentionCount += mentions.size();
				if (!mentionByType.containsKey(event.type))
					mentionByType.put(event.type, new Integer(mentions.size()));
				else{
					int count;
					Integer countI = (Integer)mentionByType.get(event.type);
					if (countI == null)
						count = 0;
					else
						count = countI.intValue();
					mentionByType.put(event.type, new Integer(count+mentions.size()));
				}
				if (!mentionBySubtype.containsKey(event.subtype))
					mentionBySubtype.put(event.subtype, new Integer(mentions.size()));
				else{
					int count;
					Integer countI = (Integer)mentionBySubtype.get(event.subtype);
					if (countI == null)
						count = 0;
					else
						count = countI.intValue();
					mentionBySubtype.put(event.subtype, new Integer(count+mentions.size()));
				}
				for (int j = 0; j < mentions.size(); j++) {
					AceEventMention m = (AceEventMention) mentions.get(j);
					buf.append("\t mention " + m.id + " = " + m.text.replace("\n", "")+"\n");
					// get anchor text
					String anchorText = m.anchorText;
					anchorText = anchorText.replace(" ","");
					anchorText = anchorText.replace("\n", "");
					String posTag = "";
					buf.append("\t anchor  = " + anchorText+"; POS = "+posTag+"\n");
					if (!posType.containsKey(posTag))
						posType.put(posTag,new Integer(0));
					Integer posI = (Integer)posType.get(posTag);
					posType.put(posTag, posI.intValue()+1);
					if (!anchorBySubtype.containsKey(event.subtype))
						anchorBySubtype.put(event.subtype, new TreeMap());
					TreeMap anchorMap = (TreeMap)anchorBySubtype.get(event.subtype);
					if (!anchorMap.containsKey(anchorText))
						anchorMap.put(anchorText, new Integer(0));
					
					Integer countI = (Integer)anchorMap.get(anchorText);
					anchorMap.put(anchorText, new Integer(countI.intValue()+1));
				} 
			}
		}
		buf.append("Done... \n");
		buf.append("Documents processed:  " + docCount+"\n");
		buf.append("Documents with no events:  " + noEventDocCount+"\n");
		buf.append("Total events:  " + eventCount+"\n");
		buf.append("Event type:  \n" + eventByType+"\n");
		buf.append("Event subtype:  \n" + eventBySubtype+"\n");
		buf.append("Total event mentions:  " + mentionCount+"\n");
		buf.append("Event type (mentions):  \n" + mentionByType+"\n");
		buf.append("Event subtype (mentions):  \n" + mentionBySubtype+"\n");
		//buf.append("Anchor (Frequencies) by event subtype: \n" + anchorBySubtype + "\n");
		buf.append("POS: \n" + posType.toString() +"\n");
		Iterator it1 = anchorBySubtype.keySet().iterator();
		while (it1.hasNext()) {
			String subtype = (String) it1.next();
			TreeMap anchorMap = (TreeMap)anchorBySubtype.get(subtype);
			buf.append(subtype+"="+anchorMap.toString()+"\n");
			
		}
		bw.write(buf.toString());
		bw.close();
		
	}
	
	public static String getHTML(String str,AceEntityMention em1,AceEntityMention em2,Span s)
	{
		String str1=str.substring(0,em1.head.start()-s.start());
		String str2 = str.substring(em1.head.start()-s.start(),em1.head.end()+1-s.start());
		String str3=str.substring(em1.head.end()+1-s.start(),em2.head.start()-s.start());
		String str4=str.substring(em2.head.start()-s.start(),em2.head.end()+1-s.start());
		String str5=str.substring(em2.head.end()+1-s.start());
		return str1+"<font color=\"red\">"+str2+"</font>"+str3+"<font color=\"red\">"+str4+"</font>"+str5;
		
	}
	
	public static String getHTML(String str,AceEventMention em1,AceEventMention em2,Span s)
	{
		String str1=str.substring(0,em1.anchorExtent.start()-s.start());
		String str2 = str.substring(em1.anchorExtent.start()-s.start(),em1.anchorExtent.end()+1-s.start());
		String str3=str.substring(em1.anchorExtent.end()+1-s.start(),em2.anchorExtent.start()-s.start());
		String str4=str.substring(em2.anchorExtent.start()-s.start(),em2.anchorExtent.end()+1-s.start());
		String str5=str.substring(em2.anchorExtent.end()+1-s.start());
		return str1+"<font color=\"red\">"+str2+"</font>"+str3+"<font color=\"red\">"+str4+"</font>"+str5;
		
	}
	
	public static String getHTML1(String str,Span em1,AceEventMention em2,Span s)
	{
		String str1=str.substring(0,em1.start()-s.start());
		String str2 = str.substring(em1.start()-s.start(),em1.end()+1-s.start());
		String str3=str.substring(em1.end()+1-s.start(),em2.anchorExtent.start()-s.start());
		String str4=str.substring(em2.anchorExtent.start()-s.start(),em2.anchorExtent.end()+1-s.start());
		String str5=str.substring(em2.anchorExtent.end()+1-s.start());
		return str1+"<font color=\"blue\">"+str2+"</font>"+str3+"<font color=\"red\">"+str4+"</font>"+str5;
		
	}
	
	public static String getHTML2(String str,Span em1,AceEventMention em2,Span s)
	{
		String str1=str.substring(0,em2.anchorExtent.start()-s.start());
		String str2 = str.substring(em2.anchorExtent.start()-s.start(),em2.anchorExtent.end()+1-s.start());
		String str3=str.substring(em2.anchorExtent.end()+1-s.start(),em1.start()-s.start());
		String str4=str.substring(em1.start()-s.start(),em1.end()+1-s.start());
		String str5=str.substring(em1.end()+1-s.start());
		return str1+"<font color=\"red\">"+str2+"</font>"+str3+"<font color=\"blue\">"+str4+"</font>"+str5;
		
	}
	
	public static void main11(String[] args) throws Exception {
		EventStatistics es = new EventStatistics();
		JetTest.initializeFromConfig("props/MEace06.properties");
		Pat.trace = false;
		Resolve.trace = false;
		
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
	
		FileWriter fw = new FileWriter(logFileName);
		
		TreeMap anchorMap = new TreeMap();
		TreeMap posMap = new TreeMap();
		TreeMap typeMap = new TreeMap();
		int c1=0;
		int c2=0;
		int c3=0;
		int c4=0;
		int c5=0;
		
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
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			ArrayList events = aceDoc.events;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent e = (AceEvent)events.get(i);
				for (int j=0;j<e.mentions.size();j++){
					AceEventMention em = e.mentions.get(j);
					String pos = getAnchorCat(em.anchorExtent, doc);
					if (!pos.equals("v")&&!pos.equals("tv")&&!pos.equals("ven")&&!pos.equals("ving"))
						continue;
					//fw.write(e.subtype+"\t"+em.anchorText+"\t"+pos+"\n");
					String anchor = ps.getStem(em.anchorText.replaceAll("\n", " "),pos);
					/*if (em.anchorText.equals("married"))
						fw.write(pos+"\t"+ps.getStem(em.anchorText,pos)+"\n");
					*/
					if (!typeMap.containsKey(e.subtype))
						typeMap.put(e.subtype, new TreeMap());
					TreeMap typeAnchorMap = (TreeMap)typeMap.get(e.subtype);
					if (!typeAnchorMap.containsKey(anchor))
						typeAnchorMap.put(anchor, new Integer(0));
					Integer count = (Integer)typeAnchorMap.get(anchor);
					typeAnchorMap.put(anchor, new Integer(count+1));
					
					if (!anchorMap.containsKey(anchor))
						anchorMap.put(anchor, new TreeMap());
					TreeMap anchorTypeMap = (TreeMap)anchorMap.get(anchor);
					if (!anchorTypeMap.containsKey(e.subtype))
						anchorTypeMap.put(e.subtype, new Integer(0));
					count = (Integer)anchorTypeMap.get(e.subtype);
					anchorTypeMap.put(e.subtype, new Integer(count+1));
					
					if (!posMap.containsKey(pos))
						posMap.put(pos, new Integer(0));
					count = (Integer)posMap.get(pos);
					posMap.put(pos, new Integer(count+1));
					
					
				}
			}
		}
		reader.close();
		fw.write("---------------------------------\n");
		/*Iterator it1 = anchorMap.keySet().iterator();
		
		while (it1.hasNext()) {
			String anchor = (String) it1.next();
			fw.write(anchor+"\n");
			TreeMap anchorTypeMap = (TreeMap)anchorMap.get(anchor); 
			Iterator it2 = anchorTypeMap.keySet().iterator();
			
			while (it2.hasNext()) {
				String subtype = (String)it2.next();
				Integer freq =(Integer)anchorTypeMap.get(subtype);
				fw.write("\t"+subtype+"\t"+freq+"\n");
			}
				
		}*/
		Iterator it1 = typeMap.keySet().iterator();
		
		while (it1.hasNext()) {
			String type = (String) it1.next();
			fw.write(type+"\n");
			TreeMap typeAnchorMap = (TreeMap)typeMap.get(type); 
			Iterator it2 = typeAnchorMap.keySet().iterator();
			
			while (it2.hasNext()) {
				String anchor = (String)it2.next();
				Integer freq =(Integer)typeAnchorMap.get(anchor);
				fw.write("\t"+anchor+"\t"+freq+"\n");
			}
				
		}
		fw.write("---------------------------------\n");
		fw.write(posMap.toString());
		fw.close();
	}
	
	static String getAnchorCat(Span anchorSpan, Document doc) {
		int posn = anchorSpan.start();
		Vector constits = doc.annotationsAt(posn, "constit");
		if (constits != null) {
			for (int i = 0; i < constits.size(); i++) {
				Annotation constit = (Annotation) constits.get(i);
				String cat = (String) constit.get("cat");
				return cat;

			}
		}
		return "";

	}
	
	public static void main(String[] args) throws IOException {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Pat.trace = false;
		Resolve.trace = false;
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		FileWriter fw = new FileWriter(logFileName);
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			//System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			/*ArrayList relations = aceDoc.relations;
			for (int i=0;i<relations.size();i++)
			{
				AceRelation r = (AceRelation) relations.get(i);
				if (r.subtype.equals("Family")){
					AceEntity arg1= r.arg1;
					AceEntity arg2= r.arg2;
					ArrayList rms = r.mentions;
					for (int j=0;j<rms.size();j++){
						AceRelationMention rm = (AceRelationMention)rms.get(j);
						String sentence = rm.text.replace("\n", " ");
						sentence=sentence.replace("\\s+", " ");
						System.out.println(sentence);
					}
				}
			}*/
			
			//start analyzing each event and event mention
			ArrayList events = aceDoc.events;
			boolean bFound = false;
			if (events.size()==0)
			{
				continue;
			}
			for (int i=0;i<events.size();i++)
			{
				AceEvent event = (AceEvent) events.get(i);
				if (!event.subtype.equals("Be-Born"))
					continue;
				for (int j=0;j<event.mentions.size();j++){
					AceEventMention em1 = (AceEventMention)event.mentions.get(j);
					fw.append("++++++++++++\n");
					fw.append(em1.toString()+"\n");
					Annotation s = findContainingSentence(doc,em1.anchorExtent);
					String str = doc.text(s);
					str = str.replaceAll("\n", " ");
					str = str.replaceAll("\\s+", " ");
					fw.append(str+"\n");
				}
			}
		}
		fw.close();
	}

	
}