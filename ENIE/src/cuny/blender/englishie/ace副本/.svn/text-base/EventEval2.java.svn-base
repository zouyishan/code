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
 * EventEval evaluates an event tagger using simple metrics of events and
 * arguments found, missing, and spurious.
 */

public class EventEval2 {

	int correctArgs, roleErrorArgs, missingArgs, spuriousArgs, weirdArgs;
	int correctEvents, typeErrorEvents, missingEvents, spuriousEvents;

	double TIP,TIR,TIF;
	double TCP,TCR,TCF;
	double AIP,AIR,AIF;
	double ACP,ACR,ACF;
	StringBuffer log = new StringBuffer();
	static String encoding = "UTF-8";
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	//static String home = "C:/Users/blender/GALE/ACE05/";
	/*static final String home =
	    "C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";*/
	//static final String home = "/home/zheng/Application/EngET/ct1/";
	//static final String home = "C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home = "/home/zheng/Application/CT2/ACE05/";
	//static final String home = "C:/Users/blender/GALE/ACECorpus/";
	static String keyDir = home+"source/";
	static String testDir = home+"output/";
	static String devFileList = home + "devfilelist";
	static String logFileName = home + "log/log";
	static String testFileList = home + "testfilelist";
	private static AceEvent baseEvent;

	public static void main(String[] args) throws IOException {
		if (EventTagger.useParser)
			JetTest.initializeFromConfig("props/ace use parses.properties");
		else
			JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		Pat.trace = false;
		Resolve.trace = false;
		AceDocument.ace2005 = true;
		
		EventEval2 ee = new EventEval2();
		ee.evaluate(testFileList,testDir);
		String logName=logFileName+"10.html";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logName), "UTF-8"));
		
		bw.write(ee.log.toString());
		bw.close();
	}

	/**
	 * evaluate the event tagger using the documents list in file
	 * <CODE>fileList</CODE>.
	 */

	public void evaluate(String fileList,String inDir) throws IOException {
		log = new StringBuffer();
		correctArgs = 0;
		roleErrorArgs = 0;
		missingArgs = 0;
		spuriousArgs = 0;
		correctEvents = 0;
		typeErrorEvents = 0;
		missingEvents = 0;
		spuriousEvents = 0;

		weirdArgs = 0;

		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		log.append("<html><body>");
		log.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		log.append("</head>");
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("Evaluate file " + docCount + ":"
					+ currentDocPath);
			String textFile = keyDir + currentDocPath;
			String xmlFile = keyDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String testFile = inDir
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			/*String testFile = inDir
				+ currentDocPath.replaceFirst(".sgm", ".apf");*/
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println (">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			
			Control.processDocument (doc, null, false, 0);

			AceDocument keyDoc = new AceDocument(textFile, xmlFile);
			AceDocument testDoc = new AceDocument(textFile, testFile);
			log.append("<br><hr><br>");
			log.append("Document: "
					+ currentDocPath.replaceFirst(".sgm", ".apf") + "<br>");
			log.append("<ul>");
			evaluateBypair(doc, testDoc, keyDoc);
			log.append("</ul>");
		}
		reader.close();

		log.append("correctEvents: " + Integer.toString(correctEvents)
						+ "<br>");
		log.append("typeErrorEvents: " + Integer.toString(typeErrorEvents)
				+ "<br>");
		log.append("missingEvents: " + Integer.toString(missingEvents)
						+ "<br>");
		log.append("spuriousEvents: " + Integer.toString(spuriousEvents)
				+ "<br>");
		log.append("correctArgs: " + Integer.toString(correctArgs) + "<br>");
		log.append("roleErrorArgs: " + Integer.toString(roleErrorArgs) + "<br>");
		log.append("missingArgs: " + Integer.toString(missingArgs) + "<br>");
		log.append("spuriousArgs: " + Integer.toString(spuriousArgs) + "<br>");

		TIP = (double)(correctEvents+typeErrorEvents)/(correctEvents+typeErrorEvents+spuriousEvents);
		TIR = (double)(correctEvents+typeErrorEvents)/(correctEvents+typeErrorEvents+missingEvents);
		TIF = 2*TIP*TIR/(TIP+TIR);
		
		TCP = (double)(correctEvents)/(correctEvents+typeErrorEvents+spuriousEvents);
		TCR = (double)(correctEvents)/(correctEvents+typeErrorEvents+missingEvents);
		TCF = 2*TCP*TCR/(TCP+TCR);
		
		AIP =  (double)(correctArgs+roleErrorArgs)/(correctArgs+roleErrorArgs+spuriousArgs);
		AIR = (double)(correctArgs+roleErrorArgs)/(correctArgs+roleErrorArgs+missingArgs);
		AIF = 2*AIP*AIR/(AIP+AIR);
		
		ACP =  (double)(correctArgs)/(correctArgs+roleErrorArgs+spuriousArgs);
		ACR = (double)(correctArgs)/(correctArgs+roleErrorArgs+missingArgs);
		ACF = 2*ACP*ACR/(ACP+ACR);
		log.append("Trigger Identification <br>");
		log.append("Precision: "+Double.toString(TIP)+ "<br>");
		log.append("Recall: "+Double.toString(TIR)+ "<br>");
		log.append("F Measure: "+Double.toString(TIF)+ "<br>");
		log.append("Trigger Identification + Trigger Classification <br>");
		log.append("Precision: "+Double.toString(TCP)+ "<br>");
		log.append("Recall: "+Double.toString(TCR)+ "<br>");
		log.append("F Measure: "+Double.toString(TCF)+ "<br>");
		log.append("Argument Identification<br>");
		log.append("Precision: "+Double.toString(AIP)+ "<br>");
		log.append("Recall: "+Double.toString(AIR)+ "<br>");
		log.append("F Measure: "+Double.toString(AIF)+ "<br>");
		log.append("Argument Identification + Argument Classification<br>");
		log.append("Precision: "+Double.toString(ACP)+ "<br>");
		log.append("Recall: "+Double.toString(ACR)+ "<br>");
		log.append("F Measure: "+Double.toString(ACF)+ "<br>");
		log.append("</body></html>");
		
		System.out.println("Trigger Identification ");
		System.out.println("Precision: "+Double.toString(TIP));
		System.out.println("Recall: "+Double.toString(TIR));
		System.out.println("F Measure: "+Double.toString(TIF));
		System.out.println("Trigger Identification + Trigger Classification");
		System.out.println("Precision: "+Double.toString(TCP));
		System.out.println("Recall: "+Double.toString(TCR));
		System.out.println("F Measure: "+Double.toString(TCF));
		System.out.println("Argument Identification");
		System.out.println("Precision: "+Double.toString(AIP));
		System.out.println("Recall: "+Double.toString(AIR));
		System.out.println("F Measure: "+Double.toString(AIF));
		System.out.println("Argument Identification + Argument Classification");
		System.out.println("Precision: "+Double.toString(ACP));
		System.out.println("Recall: "+Double.toString(ACR));
		System.out.println("F Measure: "+Double.toString(ACF));
		
	}

	public void evaluateBypair1(Document doc, AceDocument testDoc,
			AceDocument keyDoc) throws IOException {
		ArrayList testEvents = testDoc.events;
		ArrayList keyEvents = keyDoc.events;
		for (int i = 0; i < testEvents.size(); i++) {
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			ArrayList testMentions = testEvent.mentions;
			for (int j = 0; j < testMentions.size(); j++) {
				AceEventMention m = (AceEventMention) testMentions.get(j);
				AceEventMention keym = findStrictEventMention(m.anchorExtent,
						keyEvents);
				if (keym == null) {
					spuriousEvents++;
					String s = colorTrigger(m, doc, "red");
					log.append("<li>False Event "
							+ Integer.toString(spuriousEvents) + "<br>" + s + "</li>");
				} else{
					typeErrorEvents++;
					String s = colorTrigger(m, doc, "brown");
					log.append("<li>Correct Event "
							+ Integer.toString(typeErrorEvents) + "<br>" + s + "</li>");

				}
			}
		}

		for (int i = 0; i < keyEvents.size(); i++) {
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			ArrayList keyMentions = keyEvent.mentions;
			for (int j = 0; j < keyMentions.size(); j++) {
				AceEventMention m = (AceEventMention) keyMentions.get(j);
				if (m.arguments.size()==0)
					continue;
				AceEventMention testm = findStrictEventMention(m.anchorExtent,
						testEvents);
				if (testm == null) {
					missingEvents++;

					Annotation sentence = findContainingSentence(doc,
							m.anchorExtent);
					String s = colorTrigger1(sentence.span(), m, doc, "cyan");
					// s= colorArg(m,doc,m.arguments,"cyan");
					log.append("<li>Miss Event "
							+ Integer.toString(missingEvents) + "<br>" + s + "</li>");

				}
			}
		}

	}

	public ArrayList<AceEventMentionArgument> getArguments(AceDocument aceDoc){
		ArrayList<AceEventMentionArgument> argList = new ArrayList<AceEventMentionArgument> ();
		for (int i = 0; i < aceDoc.events.size(); i++) {
			AceEvent event = (AceEvent) aceDoc.events.get(i);
			ArrayList mentions = event.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention m = (AceEventMention) mentions.get(j);
				argList.addAll(m.arguments);
			}
		}
		return argList;
	}
	
	public ArrayList<AceEventMentionArgument> getArguments(ArrayList mentions){
		ArrayList<AceEventMentionArgument> argList = new ArrayList<AceEventMentionArgument> ();
		for (int i = 0; i < mentions.size(); i++) {
			AceEventMention m = (AceEventMention) mentions.get(i);
			argList.addAll(m.arguments);
		}
		
		return argList;
	}
	
	private AceEventMentionArgument findArgument(AceEventMentionArgument a, ArrayList args) {
		for (int i = 0; i < args.size(); i++) {
			AceEventMentionArgument arg = (AceEventMentionArgument) args.get(i);
			if(!a.mention.event.subtype.equals(arg.mention.event.subtype))
				continue;
			//Span sp = a.value.extent;
			AceEventArgumentValue value = arg.value.getParent();
			if (a.value instanceof AceEntityMention && value instanceof AceEntity){
				AceEntityMention m1 = (AceEntityMention)a.value;
				for (int j=0;j<((AceEntity)value).mentions.size();j++){
					AceEntityMention m = (AceEntityMention)((AceEntity)value).mentions.get(j);
					
					if (m1.head.equals(m.head)){
						return arg;
					}
				}
			}
			if (a.value instanceof AceTimexMention && value instanceof AceTimex){
				for (int j=0;j<((AceTimex)value).mentions.size();j++){
					AceTimexMention m = (AceTimexMention)((AceTimex)value).mentions.get(j);
					if (a.value.extent.equals(m.extent)){
						return arg;
					}
				}
			}
			if (a.value instanceof AceValueMention && value instanceof AceValue){
				for (int j=0;j<((AceValue)value).mentions.size();j++){
					AceValueMention m = (AceValueMention)((AceValue)value).mentions.get(j);
					if (a.value.extent.equals(m.extent)){
						return arg;
					}
				}
			}
			
			/*if (a.value instanceof AceEntityMention && arg.value instanceof AceEntityMention){
				AceEntityMention m1 = (AceEntityMention)a.value;
				AceEntityMention argm = (AceEntityMention)(arg.value);
				if (m1.head.equals(argm.head)){
					return arg;
				}
			}
			if (a.value instanceof AceTimexMention && arg.value instanceof AceTimexMention){
				
				if (a.value.extent.equals(arg.value.extent)){
					return arg;
				}
				
			}
			if (a.value instanceof AceValueMention && arg.value  instanceof AceValueMention){
				if (a.value.extent.equals(arg.value.extent)){
					return arg;
				}
			}*/
			
		}
		return null;
	}
	
	public ArrayList<AceEventMentionArgument> findAttachedArgs(ArrayList<AceEventMentionArgument> args,AceEventMention m){
		ArrayList<AceEventMentionArgument> temp = new ArrayList<AceEventMentionArgument> (); 
		for (int i=0;i<args.size();i++){
			AceEventMentionArgument arg =  args.get(i);
			if (arg.mention.equals(m))
				temp.add(arg);
		}
		return temp;
	}
	
	public ArrayList<AceEventMention> findEventMention(Span sp, ArrayList events ){
		 ArrayList<AceEventMention> candidates = new  ArrayList<AceEventMention>();
		 for (int i = 0; i < events.size(); i++) {
				AceEvent event = (AceEvent) events.get(i);
				ArrayList mentions = event.mentions;
				for (int j = 0; j < mentions.size(); j++) {
					AceEventMention m = (AceEventMention) mentions.get(j);
					if (m.anchorExtent.within(sp))
						candidates.add(m);
				}
		 }
		 return candidates;
	}
	public void evaluateBypair(Document doc, AceDocument testDoc,
			AceDocument keyDoc) throws IOException {
		
		String s;
		ArrayList testEvents = testDoc.events;
		ArrayList keyEvents = keyDoc.events;
		
		Vector<Annotation> sentences = doc.annotationsOfType("sentence");
		for (int i=0; i<sentences.size();i++){
			Annotation ann =  sentences.get(i);
			ArrayList<AceEventMention> correctEventList = new ArrayList<AceEventMention>();
			ArrayList<AceEventMention> typeErrorEventList = new ArrayList<AceEventMention>();
			ArrayList<AceEventMention> spuriousEventList = new ArrayList<AceEventMention>();
			ArrayList<AceEventMention> missEventList = new ArrayList<AceEventMention>();
			
			ArrayList<AceEventMention> testMentions = findEventMention(ann.span(),testEvents);
			ArrayList<AceEventMention> keyMentions = findEventMention(ann.span(),keyEvents);
			for (int j=0;j<testMentions.size();j++){
				AceEventMention m = (AceEventMention) testMentions.get(j);
				/*if (m.arguments.size()==0)
					continue;*/
				AceEventMention keym = findStrictEventMention(m,keyMentions);
				if (keym == null) {
					spuriousEventList.add(m);
				} else if (keym.arguments.size()==0){
					continue;
				} else if(m.event.subtype.equals(keym.event.subtype)) {
					correctEventList.add(m);
				} else {
					typeErrorEventList.add(m);
				}
			}
			for (int j=0;j<keyMentions.size();j++){
				AceEventMention m = (AceEventMention) keyMentions.get(j);
				if (m.arguments.size()==0)
					continue;
				AceEventMention testm = findStrictEventMention(m,testMentions);
				if (testm == null) {
					missEventList.add(m);
				}
			}
		
			for (int j=0;j<correctEventList.size();j++){
				AceEventMention m = correctEventList.get(j);
				correctEvents++;
				s = colorTrigger(m, doc, "blue");
				log.append("<li>Correct Event "
						+ Integer.toString(correctEvents) + "(C:"
						+ m.event.subtype + ")<br>" + s + "</li>");
			}
			for (int j=0;j<typeErrorEventList.size();j++){
				AceEventMention m = typeErrorEventList.get(j);
				typeErrorEvents++;
				s= colorTrigger(m, doc, "gold");
				log.append("<li>Type Error "
						+ Integer.toString(typeErrorEvents) + "(F:"
						+ m.event.subtype + ")<br>" + s + "</li>");
			}	
			for (int j=0;j<spuriousEventList.size();j++){
				AceEventMention m = spuriousEventList.get(j);
				spuriousEvents++;
				s = colorTrigger(m, doc, "red");
				log.append("<li>False Identified "
						+ Integer.toString(spuriousEvents) + "(F:"
						+ m.event.subtype + ")<br>" + s + "</li>");
			}
			for (int j=0;j<missEventList.size();j++){
				AceEventMention m = missEventList.get(j);
				missingEvents++;
				Annotation sentence = findContainingSentence(doc,
						m.anchorExtent);
				s = colorTrigger1(sentence.span(), m, doc, "purple");
				log.append("<li>Miss Event "
						+ Integer.toString(missingEvents) + "(C:"
						+ m.event.subtype + ")<br>" + s + "</li>");
			}
			
			
			ArrayList testArguments = getArguments(testMentions);
			ArrayList keyArguments= getArguments(keyMentions);
			
			
			for (int k=0;k <testMentions.size();k++){
				AceEventMention m = testMentions.get(k);
				ArrayList correctArguments = new ArrayList();
				ArrayList spuriousArguments = new ArrayList();
				ArrayList roleErrorArguments = new ArrayList();
				for (int j=0;j<m.arguments.size();j++){
					AceEventMentionArgument arg= (AceEventMentionArgument) m.arguments.get(j);
					AceEventMentionArgument keyArg = findArgument(arg,keyArguments);
					if (keyArg == null) {
						spuriousArguments.add(arg);
					} else if (arg.role.equals(keyArg.role)) {
						correctArguments.add(arg);
					} else {
						roleErrorArguments.add(arg);
					}
				}
				correctArgs += correctArguments.size();
				spuriousArgs += spuriousArguments.size();
				roleErrorArgs += roleErrorArguments.size();
				if (correctArguments.size()>0){
					s = colorArg(ann.span(), doc, correctArguments, "blue");
					log.append("<li>Correct Arg "
							+ Integer.toString(correctArgs
									- correctArguments.size() + 1) + "-"
							+ Integer.toString(correctArgs) + "<br>" + s
							+ "</li>");
				}
				if (roleErrorArguments.size()>0){
					s = colorArg(ann.span(), doc, roleErrorArguments, "gold");
					log.append("<li>RoleError Arg "
							+ Integer.toString(roleErrorArgs
									- roleErrorArguments.size() + 1) + "-"
							+ Integer.toString(roleErrorArgs) + "<br>" + s
							+ "</li>");
				}
				
				if (spuriousArguments.size()>0){
					s = colorArg(ann.span(), doc, spuriousArguments, "red");
					log.append("<li>Error Arg "
							+ Integer.toString(spuriousArgs
									- spuriousArguments.size() + 1) + "-"
							+ Integer.toString(spuriousArgs) + "<br>" + s
							+ "</li>");
				}
			}
			for (int k=0;k <keyMentions.size();k++){
				AceEventMention m = keyMentions.get(k);
				ArrayList missArguments = new ArrayList();
				for (int j=0;j<m.arguments.size();j++){
					AceEventMentionArgument arg = (AceEventMentionArgument) m.arguments.get(j);
					AceEventMentionArgument testArg = findArgument(arg,testArguments);
					if (testArg == null) {
						missArguments.add(arg);
					}
				}
				missingArgs += missArguments.size();
				if (missArguments.size()>0){
					s = colorArg(ann.span(), doc, missArguments, "purple");
					log.append("<li>Miss Arg "
							+ Integer.toString(missingArgs
									- missArguments.size() + 1) + "-"
							+ Integer.toString(missingArgs) + "<br>" + s
							+ "</li>");
				}
			}
		}
		
		//log.append("</ul>");
	}
	
	/*public void evaluateBypair2(Document doc, AceDocument testDoc,
			AceDocument keyDoc) throws IOException {
		ArrayList<AceEventMention> correctEventList = new ArrayList<AceEventMention>();
		ArrayList<AceEventMention> typeErrorEventList = new ArrayList<AceEventMention>();
		ArrayList<AceEventMention> spuriousEventList = new ArrayList<AceEventMention>();
		ArrayList<AceEventMention> missEventList = new ArrayList<AceEventMention>();
		String s;
		ArrayList testEvents = testDoc.events;
		ArrayList keyEvents = keyDoc.events;
		for (int i = 0; i < testEvents.size(); i++) {
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			ArrayList testMentions = testEvent.mentions;
			for (int j = 0; j < testMentions.size(); j++) {
				AceEventMention m = (AceEventMention) testMentions.get(j);
				AceEventMention keym = findStrictEventMention(m.anchorExtent,
						keyEvents);
				if (keym == null) {
					spuriousEventList.add(m);
				} else if (testEvent.subtype.equals(baseEvent.subtype)) {
					correctEventList.add(m);
				} else {
					typeErrorEventList.add(m);
				}
			}
		}
		for (int i = 0; i < keyEvents.size(); i++) {
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			ArrayList keyMentions = keyEvent.mentions;
			for (int j = 0; j < keyMentions.size(); j++) {
				AceEventMention m = (AceEventMention) keyMentions.get(j);
				if (m.arguments.size()==0)
					continue;
				AceEventMention testm = findStrictEventMention(m.anchorExtent,
						testEvents);
				if (testm == null) {
					missEventList.add(m);
				
				}
			}
		}
		for (int i=0;i<correctEventList.size();i++){
			AceEventMention m = correctEventList.get(i);
			correctEvents++;
			s = colorTrigger(m, doc, "blue");
			log.append("<li>Correct Event "
					+ Integer.toString(correctEvents) + "(C:"
					+ m.event.subtype + ")<br>" + s + "</li>");
		}
		for (int i=0;i<typeErrorEventList.size();i++){
			AceEventMention m = typeErrorEventList.get(i);
			typeErrorEvents++;
			s= colorTrigger(m, doc, "pink");
			log.append("<li>Type Error "
					+ Integer.toString(typeErrorEvents) + "(F:"
					+ m.event.subtype + ")<br>" + s + "</li>");
		}	
		for (int i=0;i<spuriousEventList.size();i++){
			AceEventMention m = spuriousEventList.get(i);
			spuriousEvents++;
			s = colorTrigger(m, doc, "red");
			log.append("<li>False Identified "
					+ Integer.toString(spuriousEvents) + "(F:"
					+ m.event.subtype + ")<br>" + s + "</li>");
		}
		for (int i=0;i<missEventList.size();i++){
			AceEventMention m = missEventList.get(i);
			missingEvents++;
			Annotation sentence = findContainingSentence(doc,
					m.anchorExtent);
			s = colorTrigger1(sentence.span(), m, doc, "cyan");
			log.append("<li>Miss Event "
					+ Integer.toString(missingEvents) + "(C:"
					+ m.event.subtype + ")<br>" + s + "</li>");
		}
		
		ArrayList arguments= getArguments(testDoc);
		ArrayList keyArguments= getArguments(keyDoc);
		
		ArrayList correctArguments = new ArrayList(arguments);
		correctArguments.retainAll(keyArguments);
		ArrayList spuriousArguments = new ArrayList(arguments);
		spuriousArguments.removeAll(keyArguments);
		ArrayList missingArguments = new ArrayList(keyArguments);
		missingArguments.removeAll(arguments);
		ArrayList args;
		for (int i = 0; i < testEvents.size(); i++) {
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			ArrayList testMentions = testEvent.mentions;
			for (int j = 0; j < testMentions.size(); j++) {
				AceEventMention m = (AceEventMention) testMentions.get(j);
				args = findAttachedArgs(correctArguments,m);
				correctArgs += args.size();
				if (args.size()>0){
					s = colorArg(m, doc, args, "green");
					log.append("<li>Correct Arg "
							+ Integer.toString(correctArgs
									- args.size() + 1) + "-"
							+ Integer.toString(correctArgs) + "<br>" + s
							+ "</li>");
				}
				args = findAttachedArgs(spuriousArguments,m);
				spuriousArgs += args.size();
				if (args.size()>0){
					s = colorArg(m, doc, args, "red");
					log.append("<li>Error Arg "
							+ Integer.toString(spuriousArgs
									- args.size() + 1) + "-"
							+ Integer.toString(spuriousArgs) + "<br>" + s
							+ "</li>");
				}
				
			}
		}
		for (int i = 0; i < keyEvents.size(); i++) {
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			ArrayList keyMentions = keyEvent.mentions;
			for (int j = 0; j < keyMentions.size(); j++) {
				AceEventMention m = (AceEventMention) keyMentions.get(j);
				args = findAttachedArgs(missingArguments,m);
				missingArgs += args.size();
				if (args.size()>0){
					s = colorArg(m, doc, args, "purple");
					log.append("<li>Miss Arg "
							+ Integer.toString(missingArgs
									- args.size() + 1)
							+ "-" + Integer.toString(missingArgs)
							+ "<br>" + s + "</li>");
				}
				
			}
		}
		//log.append("</ul>");
	}*/
	
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
			System.err.println(doc.text());
			System.err
					.println("findContainingSentence:  can't find sentence with span");
			return null;
		}
		return sentence;
	}

	private String colorArg(AceEventMention m, Document doc,
			ArrayList arguments, String color) {
		String sentence = doc.text(m.jetExtent);
		String s = sentence;
		int pos1 = m.extent.start();
		ArrayList newArguments = new ArrayList(arguments);
		for (int i = 0; i < newArguments.size() - 1; i++) {
			for (int j = i + 1; j < newArguments.size(); j++) {
				AceEventMentionArgument argi = (AceEventMentionArgument) newArguments
						.get(i);
				Span extendi = argi.value.extent;
				AceEventMentionArgument argj = (AceEventMentionArgument) newArguments
						.get(j);
				Span extendj = argj.value.extent;
				if (extendj.start() > extendi.end()
						|| (extendj.start() <= extendi.start() && extendj.end() >= extendi
								.end())) {
					newArguments.set(i, argj);
					newArguments.set(j, argi);
				}
			}
		}
		for (int i = 0; i < newArguments.size(); i++) {
			AceEventMentionArgument a = (AceEventMentionArgument) newArguments
					.get(i);
			int pos2 = a.value.extent.start();
			int pos = s.indexOf(a.value.text, pos2 - pos1);
			if (pos == -1) {
				weirdArgs++;
				System.out.println(sentence);
				System.out.println(a.value.text);
				s = "Weird: argument = " + a.value.text + "but sentence="
						+ sentence;
			} else
				s = s.substring(0, pos) + "<font color=\"" + color + "\">["
						+ a.value.text + "]</font>"
						+ s.substring(pos + a.value.text.length());
		}
		
		int pos2 = s.indexOf(m.anchorText,m.anchorExtent.start()-pos1);
		if (pos2 == -1)
			s = "Weird: anchorText = " + m.anchorText + "but sentence="
			+ sentence;
		else
			s = s.substring(0,pos2)+'<'+m.anchorText+'>'+s.substring(pos2+m.anchorExtent.end()-m.anchorExtent.start()+1);
		s = s.replace("\n", " ");
		return s;
	}

	private String colorArg(Span sp, Document doc,ArrayList arguments, String color) {
		String sentence = doc.text(sp);
		String s = sentence;
		int pos1 = sp.start();
		ArrayList newArguments = new ArrayList(arguments);
		for (int i = 0; i < newArguments.size() - 1; i++) {
			for (int j = i + 1; j < newArguments.size(); j++) {
				AceEventMentionArgument argi = (AceEventMentionArgument) newArguments
						.get(i);
				Span extendi = argi.value.extent;
				AceEventMentionArgument argj = (AceEventMentionArgument) newArguments
						.get(j);
				Span extendj = argj.value.extent;
				if (extendj.start() > extendi.end()
						|| (extendj.start() <= extendi.start() && extendj.end() >= extendi
								.end())) {
					newArguments.set(i, argj);
					newArguments.set(j, argi);
				}
			}
		}
		for (int i = 0; i < newArguments.size(); i++) {
			AceEventMentionArgument a = (AceEventMentionArgument) newArguments
					.get(i);
			int pos2 = a.value.extent.start();
			int pos = s.indexOf(a.value.text, pos2 - pos1);
			if (pos == -1) {
				weirdArgs++;
				System.out.println(sentence);
				System.out.println(a.value.text);
				s = "Weird: argument = " + a.value.text + "but sentence="
						+ sentence;
			} else
				s = s.substring(0, pos) + "<font color=\"" + color + "\">["
						+ a.value.text + "]</font>"
						+ s.substring(pos + a.value.text.length());
		}
		AceEventMentionArgument arg = (AceEventMentionArgument)arguments.get(0);
		int pos2 = s.indexOf(arg.mention.anchorText,arg.mention.anchorExtent.start()-pos1);
		if (pos2 == -1)
			s = "Weird: anchorText = " + arg.mention.anchorText + "but sentence="
			+ sentence;
		else
			s = s.substring(0,pos2)+'<'+arg.mention.anchorText+'>'+s.substring(pos2+arg.mention.anchorExtent.end()-arg.mention.anchorExtent.start()+1);
		s = s.replace("\n", " ");
		return s;
	}
	
	private String colorTrigger(AceEventMention m, Document doc,
			String color) {
		String extentText = doc.text(m.jetExtent);
		int pos1 = m.extent.start();
		int pos2 = m.anchorExtent.start();
		int pos = extentText.indexOf(m.anchorText, pos2 - pos1);
		String s;
		if (pos>=0){
			s = extentText.substring(0, pos) + "<font color=\"" + color
					+ "\">" + m.anchorText + "</font>"
					+ extentText.substring(pos + m.anchorText.length());
		}
		else
			s = "Weird: anchor = " + m.anchorText + "but sentence="+ extentText;
		s = s.replace("\n", " ");
		return s;
	}

	private String colorTrigger1(Span extent, AceEventMention m,
			Document doc, String color) {
		String extentText = doc.text(extent);
		int pos1 = extent.start();
		int pos2 = m.anchorExtent.start();
		int pos = extentText.indexOf(m.anchorText, pos2 - pos1);
		String s ;
		if (pos>=0){
			s = extentText.substring(0, pos) + "<font color=\"" + color
				+ "\">" + m.anchorText + "</font>"
				+ extentText.substring(pos + m.anchorText.length());
		}
		else
			s = "Weird: anchor = " + m.anchorText + "but sentence="+ extentText;
		s = s.replace("\n", " ");

		return s;
	}

	private AceEventMention findStrictEventMention(Span anchorExtent,
			ArrayList events) {
		for (int i = 0; i < events.size(); i++) {
			baseEvent = (AceEvent) events.get(i);
			ArrayList mentions = baseEvent.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention mention = (AceEventMention) mentions.get(j);
				Span keyAnchorExtent = mention.anchorExtent;
				/* strict match */
				if (anchorExtent.equals(keyAnchorExtent))
					return mention;
			}
		}
		return null;
	}

	private AceEventMention findStrictEventMention(AceEventMention m, ArrayList mentions) {
		for (int i = 0; i < mentions.size(); i++) {
			AceEventMention mention = (AceEventMention) mentions.get(i);
			if (m.anchorExtent.equals(mention.anchorExtent))
				return mention;
		}
		return null;
	}
	
	
	
	private AceEventMention findLooseEventMention(Span anchorExtent,
			ArrayList events) {
		for (int i = 0; i < events.size(); i++) {
			baseEvent = (AceEvent) events.get(i);
			ArrayList mentions = baseEvent.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention mention = (AceEventMention) mentions.get(j);
				Span keyAnchorExtent = mention.anchorExtent;
				/* loose match */
				if (anchorExtent.overlap(keyAnchorExtent)
						|| keyAnchorExtent.overlap(anchorExtent))
					return mention;
			}
		}
		return null;
	}
}

