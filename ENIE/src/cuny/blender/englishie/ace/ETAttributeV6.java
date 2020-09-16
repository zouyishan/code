//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkit
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.evaluation.event.BCubeCorefEval;
import cuny.blender.englishie.evaluation.event.CEAFCorefEval;
import cuny.blender.englishie.evaluation.event.MUCCorefEval;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.PorterStemmer;
import cuny.blender.englishie.nlp.parser.*;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;
import cuny.blender.englishie.nlp.wordnet.similarity.SimilarityAssessor;


import opennlp.maxent.*;
import opennlp.maxent.io.*;
import opennlp.model.EventStream;


/**
 * assigns ACE events to a Document, given the entities, times, and values.
 */

public class ETAttributeV6 {

	static int BAG_NO= 30;
	static int TEST_BAG_NO = 30;
	// parameters
	// use statistical model to gather arguments
	static boolean useArgumentModel = true;
	// events below this probability are dropped
	static double EVENT_PROBABILITY_THRESHOLD = 0.10;
	// args below this prob are never added to an event, and not used
	// in estimating event probability
	static double MIN_ARG_PROBABILITY = 0.10;
	// args below this probability are dropped from the final event
	static double ARGUMENT_PROBABILITY_THRESHOLD = 0.35;
	// minimal confidence for an arg to be used for coref determination
	static double COREF_CONFIDENCE = 0.10;
	// minimal probability for merging two events
	static double COREF_THRESHOLD = 0.35;

	static int FEATURE_SET = 0;
	// mapping from an anchor to a list of EventPatterns
	TreeMap<String, List> anchorMap;

	static StringBuffer log = new StringBuffer();
	
	PrintStream eventFeatureWriter;
	// PrintStream evTypeFeatureWriter;
	PrintStream argFeatureWriter;
	PrintStream roleFeatureWriter;
	PrintStream corefFeatureWriter;

	static GISModel eventModel;
	// static GISModel evTypeModel;
	static GISModel argModel;
	static GISModel roleModel;
	static GISModel corefModel;
	//static final String home = "/jar/workspace/attr/corpus/ACE05/";
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	//static final String home ="C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home = "/home/zheng/Application/Coref/ex4/ACE05/";
	// static final String home = "/home/zheng/Application/SRL/ACE05/";
	static final String ace = home + "sources/";
	static final String aceModels =
	// home + "ace 05/models/robust/";
	// home + "ace 05/models/non-robust/";
	// home + "Ace 05/models/no-sense/"; // final 2005 eval set
	// home + "ace 05/models/sense/";
	// home + "ace 05/models/noPA/"; // no PNB
	// home + "ace 05/models/noNB/"; // no NB
	// home + "ace 05/models/chunk/"; // chunk (no parse)
	// home + "ace 05/models/noGLARF/"; // relations from parse (no GLARF)
	// home + "Ace 05/models/Dec4/";
	// --- split (chunk / syntax / GLARF) patterns
	// with parse and GLARF
	// home + "Ace 05/models/splitPatterns/";
	// with chunk and GLARF
	// home + "Ace 05/models/chunkGLARF/";
	// with just chunk
	home + "models/bags/";
	static String triplesDir =
	// home + "Ace 05/eval/tuples/"; // eval files
	// ace + "Nov1405-ntuples/"; // no-sense tuples
	// xxx ace + "Nov1105-ntuples/"; // non-robust tuples
	// xxx ace + "Nov1205-ntuples/"; // robust tuples
	// ace + "Dec4-stuples/"; // new Charniak parser
	// ace + "010306-slow-tuples/";
	ace + "011306-fast-tuples/"; // new Charniak parser, fast mode
	static String triplesSuffix =
	// ".sent.txt.ace-n-tuple90";
	// ".sent.txt.ns-ace-n-tuple90";
	// ".sent.txt.ns-2005-ace-n-tuple90";
	// ".sent.txt.ns-2005a-ace-n-tuple92";
	".sent.txt.ns-2005-fast-ace-n-tuple92";
	// ".sgm.sent.ns-2005-fast-ace-n-tuple92"; // for GALE files

	// use predicate-argument roles from GLARF
	static boolean usePA = false;
	// use parser (pre-parsed text), else use chunker
	static boolean useParser = false;
	static boolean processOnlyOddDocuments = false;
	static boolean processOnlyEvenDocuments = false;

	public ETAttributeV6() {
		anchorMap = new TreeMap<String, List>();
	}

	/**
	 * trains an event tagger from a set of text and APF files.
	 * 
	 * @param fileList
	 *            a list of text file names, one per line. The APF file names
	 *            are obtained by replacing 'sgm' by 'apf.xml'.
	 */

	public void train(String fileList, int pass, int runs) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			// if (docCount > 10) break;
			if (processOnlyOddDocuments && docCount % 2 == 0)
				continue;
			if (processOnlyEvenDocuments && docCount % 2 == 1)
				continue;
			System.out.println("\nTrain file " + Integer.toString(docCount)
					+ ":" + currentDocPath);
			String textFile = ace + (useParser ? "perfect-parses/" : "")
					+ currentDocPath;
			String xmlFile = ace
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
		//	AceJet.Ace.monocase = AceJet.Ace.allLowerCase(doc);
		//	System.out.println(">>> Monocase is " + AceJet.Ace.monocase);

			Resolve.ACE = true;
			Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			if (pass == 0)
				acquirePatterns(doc, aceDoc, docId,runs);
			
		}
		reader.close();
	}

	String[] patternTypeList = { "CHUNK", "SYNTAX", "PA" };

	/**
	 * trains the tagger from document 'doc' and corresponding AceDocument (APF
	 * file) aceDoc.
	 */

	public void acquirePatterns(Document doc, AceDocument aceDoc, String docId,int runs) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations(triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			ArrayList mentions = event.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention m = (AceEventMention) mentions.get(j);
				
				if (trainModModels) {
					trainModClassifier(event, m, doc, aceDoc, relations,runs);
				}
				if (trainPolModels) {
					trainPolClassifier(event, m, doc, aceDoc, relations,runs);
				}
				if (trainGenModels) {
					trainGenClassifier (event, m, doc, aceDoc, relations,runs);
				}
				if (trainTenModels) {
					trainTenClassifier (event, m, doc, aceDoc, relations,runs);
				}

			}
		}
	}

	
	private void trainModClassifier(AceEvent event,
			AceEventMention eventMention, Document doc, AceDocument aceDoc,
			SyntacticRelationSet relations,int runs) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
				doc, relations);
		Datum d = modFeatures(doc, anchor, event, eventMention);
		d.setOutcome(event.modality);
		modFeatureWriter[runs].println(d.toString());
	}

	private void trainPolClassifier(AceEvent event,
			AceEventMention eventMention, Document doc, AceDocument aceDoc,
			SyntacticRelationSet relations,int runs) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
				doc, relations);
		Datum d = polFeatures(doc, anchor, event, eventMention);
		d.setOutcome(event.polarity);
		polFeatureWriter[runs].println(d.toString());
	}

	private void trainGenClassifier(AceEvent event,
			AceEventMention eventMention, Document doc, AceDocument aceDoc,
			SyntacticRelationSet relations,int runs) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
				doc, relations);
		Datum d = genFeatures(doc, anchor, event, eventMention);
		d.setOutcome(event.genericity);
		genFeatureWriter[runs].println(d.toString());
	}

	private void trainTenClassifier(AceEvent event,
			AceEventMention eventMention, Document doc, AceDocument aceDoc,
			SyntacticRelationSet relations,int runs) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
				doc, relations);
		Datum d = tenFeatures(doc, anchor, event, eventMention);
		d.setOutcome(event.tense);
		tenFeatureWriter[runs].println(d.toString());
	}

	private Datum modFeatures(Document doc, String anchor, AceEvent event,
			AceEventMention eventMention) {
		Datum d = new Datum();
		// = anchor word
		d.addFV("anchor", anchor);
		// = event type
		d.addFV("evType", event.type);
		d.addFV("evSubtype", event.type + ":" + event.subtype);

		boolean time=false;
		boolean place=false;
		int argCount = 0;
		for (int k=0;k<eventMention.arguments.size();k++){
			if (eventMention.arguments.get(k).role.contains("Time")){
				//d.addFV("timestr",eventMention.arguments.get(k).role );
				time=true;
			}
			if (eventMention.arguments.get(k).role.contains("Place")){
				//d.addFV("timestr",eventMention.arguments.get(k).role );
				place=true;
			}
			else /*if (!eventMention.arguments.get(k).role.contains("Place"))*/
				argCount++;
		}
		
		int posn = eventMention.anchorExtent.start();
		String anchorCat="";
		Vector anchorConstit = doc.annotationsAt(posn, "constit");
		if (anchorConstit != null) {
			for (int j = 0; j < /*anchorConstit.size()*/1; j++) {
				Annotation constit = (Annotation) anchorConstit.get(j);
				anchorCat = (String) constit.get("cat");
				d.addFV("anchorpos", anchorCat);
			}
		}
		Annotation s = findContainingSentence(doc,eventMention.anchorExtent);
		if (s==null)
			return d;
		
		String[] bstr = new String[3];
		String[] astr = new String[3];
		String[] bpos = new String[3];
		String[] apos = new String[3];
		boolean bNegate = false;
		String verb = null;
		boolean bVerb = false;
		PorterStemmer ps = new PorterStemmer();
		String preCat = "";
		int i = 0;
		Annotation token;
		String lastverb = "";
		String lastverbpos = "";
		boolean bModword = false;
		int verbCount = 0;
		while (posn >= s.start()) {
			posn--;
			token = doc.tokenAt(posn);
			if (token != null) {
				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");
					}
				}

				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				//String word =SynFun.getHead(doc, token).toLowerCase(); 

				if (i <=2) {
					// only get the first left token
					bstr[i] = word;
					bpos[i] = cat;
				}
				
				if (word.equals("may")||word.equals("would")||
						word.equals("could")||word.equals("can")||word.contains("if") )
					bModword = true;
				if (word.equals(","))
					break;
				if (cat!=null){
					if (cat.equals("v") || cat.equals("tv")	|| cat.equals("ven")){
						//d.addFV("verb", word);
						d.addFV("verbpos"+Integer.toString(verbCount), cat);
						verbCount++;
						lastverb = word;
						lastverbpos = cat;
						//bVerb=true;
						//break;
					}
				}
				i++;
				preCat = cat;
			}
		}
		i = 0;
		posn = eventMention.anchorExtent.end();
		while (i < 3 && posn < s.end()) {
			posn++;
			token = doc.tokenAt(posn);
			if (token != null) {

				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");

					}
				}
				
				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				if (i<=2) {
					// only get the first left token
					astr[i] = word;
					apos[i] = cat;
				}
				if (word.equals(","))
					break;
				i++;
			}
		}

		for (int j=0;j<2;j++){
			if (bstr[j]!=null) {
				d.addFV("buni"+Integer.toString(j), bstr[j]);
			}
			if (astr[j]!=null) {
				d.addFV("auni"+Integer.toString(j), astr[j]);
			}
			if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), bpos[j]);
			}
			if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), apos[j]);
			}
	
			if (bstr[j]!=null&&bpos[j]!=null) {
				d.addFV("bunipos"+Integer.toString(j), bstr[j] + ":" + bpos[j]);
			}
			/*if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), anchorCat + ":" + bpos[j]);
			}*/
			if (astr[j]!=null&&apos[j]!=null) {
				d.addFV("aunipos"+Integer.toString(j), astr[j] + ":" + apos[j]);
			}
			/*if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), anchorCat + ":" + apos[j]);
			}*/
		}

		if (!lastverb.isEmpty()){
			d.addFV("lastverb",lastverb);
			d.addFV("lastpos",lastverbpos);
			d.addFV("lastverbpos",lastverb+":"+lastverbpos);
		}
		d.addFV("argno", Integer.toString(argCount));
		if (time){
			d.addFV("time", "T");
			//d.addFV("negate", "T:"+anchorCat);
		}
		else {
			d.addFV("time", "F");
		}
		if (place){
			d.addFV("place", "T");
			//d.addFV("negate", "T:"+anchorCat);
		}
		else {
			d.addFV("place", "F");
		}
		if (bModword){
			d.addFV("modword", "T");
			//d.addFV("negate", "T:"+anchorCat);
		}
		else {
			d.addFV("modword", "F");
		}
		return d;
	}
	
	private Datum genFeatures(Document doc, String anchor, AceEvent event,
			AceEventMention eventMention) {
		Datum d = new Datum();
		// = anchor word
		d.addFV("anchor", anchor);
		// = event type
		d.addFV("evType", event.type);
		d.addFV("evSubtype", event.type + ":" + event.subtype);

		boolean time=false;
		
		int argCount = 0;
		for (int k=0;k<eventMention.arguments.size();k++){
			if (eventMention.arguments.get(k).role.contains("Time")){
				d.addFV("timestr",eventMention.arguments.get(k).role );
				time=true;
			}
			else /*if (!eventMention.arguments.get(k).role.contains("Place"))*/
				argCount++;
		}
		
		int posn = eventMention.anchorExtent.start();
		String anchorCat="";
		Vector anchorConstit = doc.annotationsAt(posn, "constit");
		if (anchorConstit != null) {
			for (int j = 0; j < /*anchorConstit.size()*/1; j++) {
				Annotation constit = (Annotation) anchorConstit.get(j);
				anchorCat = (String) constit.get("cat");
				d.addFV("anchorpos", anchorCat);
			}
		}
		Annotation s = findContainingSentence(doc,eventMention.anchorExtent);
		if (s==null)
			return d;
		
		String[] bstr = new String[3];
		String[] astr = new String[3];
		String[] bpos = new String[3];
		String[] apos = new String[3];
		boolean bNegate = false;
		String verb = null;
		boolean bVerb = false;
		PorterStemmer ps = new PorterStemmer();
		String preCat = "";
		int i = 0;
		Annotation token;
		String lastverb = "";
		String lastverbpos = "";
		
		int verbCount = 0;
		while (posn >= s.start()) {
			posn--;
			token = doc.tokenAt(posn);
			if (token != null) {
				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");
					}
				}

				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				//String word =SynFun.getHead(doc, token).toLowerCase(); 

				if (i <=2) {
					// only get the first left token
					bstr[i] = word;
					bpos[i] = cat;
				}
				
				/*if ( i<=3&&(word.equals("without")||
						word.equals("cannot")||word.equals("not")||word.contains("n't") || word.equals("no")))
					bNegate = true;*/
				if (word.equals(","))
					break;
				if (cat!=null/*&&!bVerb*/){
					if (cat.equals("v") || cat.equals("tv")	|| cat.equals("ven")){
						//d.addFV("verb", word);
						d.addFV("verbpos"+Integer.toString(verbCount), cat);
						verbCount++;
						lastverb = word;
						lastverbpos = cat;
						//bVerb=true;
						//break;
					}
				}
				i++;
				preCat = cat;
			}
		}
		i = 0;
		posn = eventMention.anchorExtent.end();
		while (i < 3 && posn < s.end()) {
			posn++;
			token = doc.tokenAt(posn);
			if (token != null) {

				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");

					}
				}
				
				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				if (i<=2) {
					// only get the first left token
					astr[i] = word;
					apos[i] = cat;
				}
				i++;
			}
		}

		for (int j=0;j<2;j++){
			if (bstr[j]!=null) {
				d.addFV("buni"+Integer.toString(j), bstr[j]);
			}
			if (astr[j]!=null) {
				d.addFV("auni"+Integer.toString(j), astr[j]);
			}
			if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), bpos[j]);
			}
			if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), apos[j]);
			}
	
			if (bstr[j]!=null&&bpos[j]!=null) {
				d.addFV("bunipos"+Integer.toString(j), bstr[j] + ":" + bpos[j]);
			}
			/*if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), anchorCat + ":" + bpos[j]);
			}*/
			if (astr[j]!=null&&apos[j]!=null) {
				d.addFV("aunipos"+Integer.toString(j), astr[j] + ":" + apos[j]);
			}
			/*if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), anchorCat + ":" + apos[j]);
			}*/
		}

		if (!lastverb.isEmpty()){
			d.addFV("lastverb",lastverb);
			d.addFV("lastpos",lastverbpos);
			d.addFV("lastverbpos",lastverb+":"+lastverbpos);
		}
		d.addFV("argno", Integer.toString(argCount));
		/*if (time){
			d.addFV("time", "T");
			//d.addFV("negate", "T:"+anchorCat);
		}
		else {
			d.addFV("time", "F");
		}*/
		
		return d;
	}
	
	private Datum polFeatures(Document doc, String anchor, AceEvent event,
			AceEventMention eventMention) {
		Datum d = new Datum();
		// = anchor word
		d.addFV("anchor", anchor);
		// = event type
		d.addFV("evType", event.type);
		d.addFV("evSubtype", event.type + ":" + event.subtype);

		int posn = eventMention.anchorExtent.start();
		String anchorCat="";
		Vector anchorConstit = doc.annotationsAt(posn, "constit");
		if (anchorConstit != null) {
			for (int j = 0; j < /*anchorConstit.size()*/1; j++) {
				Annotation constit = (Annotation) anchorConstit.get(j);
				anchorCat = (String) constit.get("cat");
				d.addFV("anchorpos", anchorCat);
			}
		}
		Annotation s = findContainingSentence(doc,eventMention.anchorExtent);
		if (s==null)
			return d;
		
		String[] bstr = new String[3];
		String[] astr = new String[3];
		String[] bpos = new String[3];
		String[] apos = new String[3];
		boolean bNegate = false;
		String verb = null;
		boolean bVerb = false;
		PorterStemmer ps = new PorterStemmer();
		String preCat = "";
		int i = 0;
		Annotation token;
		
		while (posn >= s.start()) {
			posn--;
			token = doc.tokenAt(posn);
			if (token != null) {
				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");
						
					}
				}
		
				
				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				//String word =SynFun.getHead(doc, token).toLowerCase(); 

				if (i <=2) {
					// only get the first left token
					bstr[i] = word;
					bpos[i] = cat;
				}
				
				if ( i<=4&&(word.equals("without")||
						word.equals("cannot")||word.equals("not")||word.contains("n't") || word.equals("no")))
					bNegate = true;
				if (word.equals(","))
					break;
				if (cat!=null&&!bVerb){
					if (cat.equals("v") || cat.equals("tv")	|| cat.equals("ven")){
						d.addFV("verb", word);
						bVerb=true;
						//break;
					}
				}
				i++;
				preCat = cat;
			}
		}
		i = 0;
		posn = eventMention.anchorExtent.end();
		while (i < 3 && posn < s.end()) {
			posn++;
			token = doc.tokenAt(posn);
			if (token != null) {

				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");

					}
				}
				
				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				if (i<=2) {
					// only get the first left token
					astr[i] = word;
					apos[i] = cat;
				}
				i++;
			}
		}

		for (int j=0;j<2;j++){
			if (bstr[j]!=null) {
				d.addFV("buni"+Integer.toString(j), bstr[j]);
			}
			if (astr[j]!=null) {
				d.addFV("auni"+Integer.toString(j), astr[j]);
			}
			if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), bpos[j]);
			}
			if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), apos[j]);
			}
	
			if (bstr[j]!=null&&bpos[j]!=null) {
				d.addFV("bunipos"+Integer.toString(j), bstr[j] + ":" + bpos[j]);
			}
			/*if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), anchorCat + ":" + bpos[j]);
			}*/
			if (astr[j]!=null&&apos[j]!=null) {
				d.addFV("aunipos"+Integer.toString(j), astr[j] + ":" + apos[j]);
			}
			/*if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), anchorCat + ":" + apos[j]);
			}*/
		}

		if (bNegate){
			d.addFV("negate", "T");
			//d.addFV("negate", "T:"+anchorCat);
		}
		else {
			d.addFV("negate", "F");
		}
		
		return d;
	}

	private Datum tenFeatures(Document doc, String anchor, AceEvent event,
			AceEventMention eventMention) {
		Datum d = new Datum();
		// = anchor word
		d.addFV("anchor", anchor);
		// = event type
		d.addFV("evType", event.type);
		d.addFV("evSubtype", event.type + ":" + event.subtype);

		boolean time=false;
		
		for (int k=0;k<eventMention.arguments.size();k++){
			if (eventMention.arguments.get(k).role.contains("Time")){
				d.addFV("timestr",eventMention.arguments.get(k).role );
				time=true;
			}
		}
		
		int posn = eventMention.anchorExtent.start();
		String anchorCat="";
		Vector anchorConstit = doc.annotationsAt(posn, "constit");
		if (anchorConstit != null) {
			for (int j = 0; j < /*anchorConstit.size()*/1; j++) {
				Annotation constit = (Annotation) anchorConstit.get(j);
				anchorCat = (String) constit.get("cat");
				d.addFV("anchorpos", anchorCat);
			}
		}
		Annotation s = findContainingSentence(doc,eventMention.anchorExtent);
		if (s==null)
			return d;
		
		String[] bstr = new String[3];
		String[] astr = new String[3];
		String[] bpos = new String[3];
		String[] apos = new String[3];
		boolean bNegate = false;
		String verb = null;
		boolean bVerb = false;
		PorterStemmer ps = new PorterStemmer();
		String preCat = "";
		int i = 0;
		Annotation token;
		String lastverb = "";
		String lastverbpos = "";
		
		int verbCount = 0;
		while (posn >= s.start()) {
			posn--;
			token = doc.tokenAt(posn);
			if (token != null) {
				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");
						
					}
				}
		
				
				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				//String word =SynFun.getHead(doc, token).toLowerCase(); 

				if (i <=2) {
					// only get the first left token
					bstr[i] = word;
					bpos[i] = cat;
				}
				
				/*if ( i<=3&&(word.equals("without")||
						word.equals("cannot")||word.equals("not")||word.contains("n't") || word.equals("no")))
					bNegate = true;*/
				if (word.equals(","))
					break;
				if (cat!=null/*&&!bVerb*/){
					if (cat.equals("v") || cat.equals("tv")	|| cat.equals("ven")){
						//d.addFV("verb", word);
						d.addFV("verbpos"+Integer.toString(verbCount), cat);
						verbCount++;
						lastverb = word;
						lastverbpos = cat;
						//bVerb=true;
						//break;
					}
				}
				i++;
				preCat = cat;
			}
		}
		i = 0;
		posn = eventMention.anchorExtent.end();
		while (i < 3 && posn < s.end()) {
			posn++;
			token = doc.tokenAt(posn);
			if (token != null) {

				String cat = null;
				Vector constits = doc.annotationsAt(posn, "constit");
				if (constits != null) {
					for (int j = 0; j < 1; j++) {
						Annotation constit = (Annotation) constits.get(j);
						cat = (String) constit.get("cat");

					}
				}
				
				String word = doc.text(token).trim();
				if (word.length()>1)
					word = ps.stem(word).toLowerCase();
				if (i<=2) {
					// only get the first left token
					astr[i] = word;
					apos[i] = cat;
				}
				i++;
			}
		}

		for (int j=0;j<2;j++){
			if (bstr[j]!=null) {
				d.addFV("buni"+Integer.toString(j), bstr[j]);
			}
			if (astr[j]!=null) {
				d.addFV("auni"+Integer.toString(j), astr[j]);
			}
			if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), bpos[j]);
			}
			if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), apos[j]);
			}
	
			if (bstr[j]!=null&&bpos[j]!=null) {
				d.addFV("bunipos"+Integer.toString(j), bstr[j] + ":" + bpos[j]);
			}
			/*if (bpos[j]!=null) {
				d.addFV("bpos"+Integer.toString(j), anchorCat + ":" + bpos[j]);
			}*/
			if (astr[j]!=null&&apos[j]!=null) {
				d.addFV("aunipos"+Integer.toString(j), astr[j] + ":" + apos[j]);
			}
			/*if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), anchorCat + ":" + apos[j]);
			}*/
		}

		if (!lastverb.isEmpty()){
			d.addFV("lastverb",lastverb);
			d.addFV("lastpos",lastverbpos);
			d.addFV("lastverbpos",lastverb+":"+lastverbpos);
		}
		/*if (time){
			d.addFV("time", "T");
			//d.addFV("negate", "T:"+anchorCat);
		}
		else {
			d.addFV("time", "F");
		}*/
		
		return d;
	}
	


	private Annotation findContainingSentence(Document doc, Span span) {
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

	private int findSentenceId(Document doc, Span span) {
		Vector sentences = doc.annotationsOfType("sentence");
		if (sentences == null) {
			System.err.println("findContainingSentence:  no sentences found");
			return -1;
		}
		Annotation sentence = null;
		for (int i = 0; i < sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			if (span.within(s.span())) {
				return i;
			}
		}
		return -1;
	}

	static private void buildClassifierModel(String featureFileName,
			String modelFileName) {
		boolean USE_SMOOTHING = false;
		boolean PRINT_MESSAGES = true;
		double SMOOTHING_OBSERVATION = 0.1;
		try {
			FileReader datafr = new FileReader(new File(featureFileName));
			EventStream es = new BasicEventStream(
					new PlainTextByLineDataStream(datafr));
			GIS.SMOOTHING_OBSERVATION = SMOOTHING_OBSERVATION;
			GISModel model = GIS.trainModel(es, 100, 4, USE_SMOOTHING,
					PRINT_MESSAGES);

			File outputFile = new File(modelFileName);
			GISModelWriter writer = new SuffixSensitiveGISModelWriter(model,
					outputFile);
			writer.persist();
		} catch (Exception e) {
			System.err.print("Unable to create model due to exception: ");
			System.err.println(e);
		}
	}

	private static GISModel loadClassifierModel(String modelFileName) {
		try {
			File f = new File(modelFileName);
			GISModel m = (GISModel) new SuffixSensitiveGISModelReader(f).getModel();
			System.err.println("GIS model " + f.getName() + " loaded.");
			return m;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return null; // required by compiler
		}
	}

	

	public int findMax(int [] res){
		int max=0;
		int index=0;
		for (int i=0;i<res.length;i++){
			if (res[i]>max){
				max = res[i];
				index = i;
			}
		}
		return index;
	}
	
	public void tag1(Document doc, AceDocument aceDoc, String currentDocPath,
			String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations (triplesDir + currentDocPath + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = constructEvent(aceDoc, aceDoc.docID);
		for (int i=0; i<events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention eventMention = event.mentions.get(j);
				Span anchorExtent = eventMention.anchorJetExtent;
				String anchorText = eventMention.anchorText;
				String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
						doc, relations);
				if (trainPolModels){
					Datum d = polFeatures(doc, anchor, event, eventMention);
					int[] polResult = new int[2];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String pol = polModel[k].getBestOutcome(polModel[k].eval(d.toArray())).intern();
						
						double polProb = polModel[k].eval(d.toArray())[polModel[k].getIndex(pol)];
						//log.append(d.toString()+":"+pol+"("+Double.toString(polProb)+")\n");
						if (polProb<0.75&&d.getFV("negate").equals("T")){
							pol="Negative";
						}
						if (pol.equals("Positive"))
							polResult[0]++;
						else
							polResult[1]++;
					}
					
					int max = findMax(polResult);
					
					if (max==0){
						if (polResult[1]>=2)
							event.polarity="Negative";
						else
							event.polarity="Positive";
					}
					else
						event.polarity="Negative";
					//event.polProb = polProb;
				}
				if (trainModModels){
					Datum d = modFeatures(doc, anchor, event, eventMention);
					
					int[] modResult = new int[2];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String mod = modModel[k].getBestOutcome(modModel[k].eval(d.toArray())).intern();
						double modProb = modModel[k].eval(d.toArray())[modModel[k].getIndex(mod)];
						//log.append(d.toString()+":"+mod+"("+Double.toString(modProb)+")\n");
						if (modProb<0.75&&d.getFV("modword").equals("T")){
							mod="Other";
						}
						if (mod.equals("Asserted"))
							modResult[0]++;
						else
							modResult[1]++;
					}
					int max = findMax(modResult);
					/*if (max==0)
						event.modality="Asserted";
					else
						event.modality="Other";*/
					if (max==0){
						if (modResult[1]>=2)
							event.modality="Other";
						else
							event.modality="Asserted";
						
					}
					else
						event.modality="Other";
					//event.modProb = modProb;
				}
				if (trainGenModels){
					Datum d = genFeatures(doc, anchor, event, eventMention);
					int[] genResult = new int[2];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String gen = genModel[k].getBestOutcome(genModel[k].eval(d.toArray())).intern();
						double genProb = genModel[k].eval(d.toArray())[genModel[k].getIndex(gen)];
						//log.append(d.toString()+":"+gen+"("+Double.toString(genProb)+")\n");
						if (gen.equals("Specific"))
							genResult[0]++;
						else
							genResult[1]++;
					}
					int max = findMax(genResult);
					/*if (max==0)
						event.genericity="Specific";
					else
						event.genericity="Generic";*/
					if (max==0){
						if (genResult[1]>=2)
							event.genericity="Generic";
						else
							event.genericity="Specific";
						
					}
					else
						event.genericity="Generic";
					//event.genProb = genProb;
				}
				if (trainTenModels){
					Datum d = tenFeatures(doc, anchor, event, eventMention);
					int[] tenResult = new int[4];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String ten = tenModel[k].getBestOutcome(tenModel[k].eval(d.toArray())).intern();
						double tenProb = tenModel[k].eval(d.toArray())[tenModel[k].getIndex(ten)];
					//log.append(d.toString()+":"+ten+"("+Double.toString(tenProb)+")\n");
						if (ten.equals("Future"))
							tenResult[0]++;
						else if (ten.equals("Past"))
							tenResult[1]++;
						else if (ten.equals("Present"))
							tenResult[2]++;
						else 
							tenResult[3]++;
					}
					int max = findMax(tenResult);
					if (max==0)
						event.tense="Future";
					else if (max==1) 
						event.tense="Past";
					else if (max==2) 
						event.tense="Present";
					else if (max==3) 
						event.tense="Unspecified";
					
					//event.tenProb = tenProb;
				}
			}
		}
		aceDoc.events.clear();
		aceDoc.events=events;
	}
	
	/**
	 * identify ACE events in Document 'doc' and add them to 'aceDoc'.
	 */

	public void tag(Document doc, AceDocument aceDoc, String currentDocPath,
			String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations (triplesDir + currentDocPath + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = constructEvent(aceDoc, aceDoc.docID);
		for (int i=0; i<events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention eventMention = event.mentions.get(j);
				Span anchorExtent = eventMention.anchorJetExtent;
				String anchorText = eventMention.anchorText;
				String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
						doc, relations);
				if (trainPolModels){
					Datum d = polFeatures(doc, anchor, event, eventMention);
					int[] polResult = new int[2];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String pol = polModel[k].getBestOutcome(polModel[k].eval(d.toArray())).intern();
						
						double polProb = polModel[k].eval(d.toArray())[polModel[k].getIndex(pol)];
						//log.append(d.toString()+":"+pol+"("+Double.toString(polProb)+")\n");
						if (polProb<0.75&&d.getFV("negate").equals("T")){
							pol="Negative";
						}
						if (pol.equals("Positive"))
							polResult[0]++;
						else
							polResult[1]++;
					}
					
					int max = findMax(polResult);
					if (max==0)
						event.polarity="Positive";
					else
						event.polarity="Negative";
					//event.polProb = polProb;
				}
				if (trainModModels){
					Datum d = modFeatures(doc, anchor, event, eventMention);
					
					int[] modResult = new int[2];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String mod = modModel[k].getBestOutcome(modModel[k].eval(d.toArray())).intern();
						double modProb = modModel[k].eval(d.toArray())[modModel[k].getIndex(mod)];
						//log.append(d.toString()+":"+mod+"("+Double.toString(modProb)+")\n");
						if (modProb<0.75&&d.getFV("modword").equals("T")){
							mod="Other";
						}
						if (mod.equals("Asserted"))
							modResult[0]++;
						else
							modResult[1]++;
					}
					int max = findMax(modResult);
					if (max==0)
						event.modality="Asserted";
					else
						event.modality="Other";
					//event.modProb = modProb;
				}
				if (trainGenModels){
					Datum d = genFeatures(doc, anchor, event, eventMention);
					int[] genResult = new int[2];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String gen = genModel[k].getBestOutcome(genModel[k].eval(d.toArray())).intern();
						double genProb = genModel[k].eval(d.toArray())[genModel[k].getIndex(gen)];
						//log.append(d.toString()+":"+gen+"("+Double.toString(genProb)+")\n");
						if (gen.equals("Specific"))
							genResult[0]++;
						else
							genResult[1]++;
					}
					int max = findMax(genResult);
					if (max==0)
						event.genericity="Specific";
					else
						event.genericity="Generic";
					//event.genProb = genProb;
				}
				if (trainTenModels){
					Datum d = tenFeatures(doc, anchor, event, eventMention);
					int[] tenResult = new int[4];
					for (int k=0;k<TEST_BAG_NO;k++){ 
						String ten = tenModel[k].getBestOutcome(tenModel[k].eval(d.toArray())).intern();
						double tenProb = tenModel[k].eval(d.toArray())[tenModel[k].getIndex(ten)];
					//log.append(d.toString()+":"+ten+"("+Double.toString(tenProb)+")\n");
						if (ten.equals("Future"))
							tenResult[0]++;
						else if (ten.equals("Past"))
							tenResult[1]++;
						else if (ten.equals("Present"))
							tenResult[2]++;
						else 
							tenResult[3]++;
					}
					int max = findMax(tenResult);
					if (max==0)
						event.tense="Future";
					else if (max==1) 
						event.tense="Past";
					else if (max==2) 
						event.tense="Present";
					else if (max==3) 
						event.tense="Unspecified";
					
					//event.tenProb = tenProb;
				}
			}
		}
		aceDoc.events.clear();
		aceDoc.events=events;
	}


	ArrayList constructEvent(AceDocument aceDoc, String docId) {
		int aceEventNo = 1;
		ArrayList newEvents = new ArrayList();
		for (int i = 0; i < aceDoc.events.size(); i++) {
			AceEvent event = aceDoc.events.get(i);

			for (int j = 0; j < event.mentions.size(); j++) {
				AceEventMention mention = (AceEventMention) event.mentions
						.get(j);
				String eventId = docId + "-EV" + aceEventNo;
				AceEvent newEvent = new AceEvent(eventId, event.type,
						event.subtype, event.modality, event.polarity,
						event.genericity, event.tense);

				newEvent.addMention(mention);
				for (int iarg = 0; iarg < mention.arguments.size(); iarg++) {
					AceEventMentionArgument marg = (AceEventMentionArgument) mention.arguments
							.get(iarg);
					newEvent.addArgument(new AceEventArgument(marg.value
							.getParent(), marg.role));
				}
				newEvents.add(newEvent);
				aceEventNo++;
			}
		}
		return newEvents;
	}

	

	public void tag(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("\nTag file " + Integer.toString(docCount) + ":"
					+ currentDocPath);
			// String textFile = ace + currentDocPath;
			String textFile = ace + (useParser ? "perfect-parses/" : "")
					+ currentDocPath;
			String xmlFile = ace
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String outputFile = home + "output/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			// for evaluation --------------
			/*
			 * textFile = home + "Ace 05/eval/allparses/" + currentDocPath;
			 * xmlFile = home + "Ace 05/eval/ACE05_diagdata_v1/english/" +
			 * currentDocPath.replaceFirst(".sgm", ".entities.apf.xml");
			 * outputFile = home + "Ace 05/eval/output/" +
			 * currentDocPath.replaceFirst(".sgm", ".apf.xml");
			 */
			// ------------
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
		//	AceJet.Ace.monocase = AceJet.Ace.allLowerCase(doc);
		//	System.out.println(">>> Monocase is " + AceJet.Ace.monocase);
			Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			//aceDoc.events.clear();
			tag(doc, aceDoc, currentDocPath.replaceFirst(".sgm", ""),
					aceDoc.docID);
			aceDoc.write(new BufferedWriter(new FileWriter(outputFile)), doc);
		}
	}

	
	static final String fileListTrain = home + "trainfilelist";
	static final String[] splitListTrain=new String[BAG_NO];
	static final String fileListTest = home + "testfilelist";
	static String fileList = home + "totalfilelist";
/*	static String mucFileName = home + "coref_muc";
	static String bcubeFileName = home + "coref_bcube";
	static String eacfFileName = home + "coref_eacf";
	static String fileList = home + "totalfilelist";*/

	PrintStream[] modFeatureWriter=new PrintStream[BAG_NO];
	PrintStream[] polFeatureWriter=new PrintStream[BAG_NO];
	PrintStream[] genFeatureWriter=new PrintStream[BAG_NO];
	PrintStream[] tenFeatureWriter=new PrintStream[BAG_NO];

	static GISModel[] modModel = new GISModel[BAG_NO];
	static GISModel[] polModel= new GISModel[BAG_NO];
	static GISModel[] genModel= new GISModel[BAG_NO];
	static GISModel[] tenModel= new GISModel[BAG_NO];

	static final String[] modFeatureFileName = new String[BAG_NO];
	static final String[] modModelFileName = new String[BAG_NO];
	static final String[] polFeatureFileName = new String[BAG_NO];
	static final String[] polModelFileName = new String[BAG_NO];
	static final String[] genFeatureFileName = new String[BAG_NO];
	static final String[] genModelFileName = new String[BAG_NO];
	static final String[] tenFeatureFileName = new String[BAG_NO];
	static final String[] tenModelFileName = new String[BAG_NO];

	static final boolean trainModModels = true;
	static final boolean trainPolModels = true;
	static final boolean trainGenModels = true;
	static final boolean trainTenModels = true;

	
	static void splitCorpus(int fold, ArrayList v) throws IOException {
		PrintStream trainWriter = new PrintStream(new FileOutputStream(
				fileListTrain));
		PrintStream testWriter = new PrintStream(new FileOutputStream(
				fileListTest));
		for (int i = 0; i < v.size(); i++) {
			String str = (String) v.get(i);

			if (i >= fold * 60 && i < (fold + 1) * 60)
				testWriter.println(str);
			else
				trainWriter.println(str);
		}
		trainWriter.close();
		testWriter.close();
	}
	
	private void splitCorpus(String corpus, int splitCount, String fileList1,String fileList2) throws IOException{
		int docCount = 599;
		BufferedReader reader = new BufferedReader(new FileReader(corpus));
		String currentDocPath;
		
		int i;
		boolean[] flag= new boolean[docCount];
		for (i=0; i<docCount; i++)
			flag[i] = false;
		Random rand = new Random();
		for (i=0;i<splitCount;i++)
		{
			int index = Math.abs(rand.nextInt())%docCount;
			if (!flag[index])
				flag[index]= true;
			else
				i--;
		}
		//reader = new BufferedReader(new FileReader(corpus));
		int pos = 0;
		PrintStream trainWriter = new PrintStream (new FileOutputStream (fileList1));
		PrintStream testWriter = new PrintStream (new FileOutputStream (fileList2));
		
		while ((currentDocPath = reader.readLine()) != null) {
			if (flag[pos])
			{
				trainWriter.println(currentDocPath);
			}
			else
			{
				testWriter.println(currentDocPath);
			}
			
			pos++;
		}
		reader.close();
	}

	private void randomSampling( ArrayList v, String splitFile, int size) throws IOException{
		
		PrintStream trainWriter = new PrintStream (new FileOutputStream (splitFile));
		Random rand = new Random();
		for (int i=0;i<size;i++){
			int index = Math.abs(rand.nextInt())%v.size();
		
			trainWriter.println(v.get(index));
		}
			
		trainWriter.close();
	}
	
	static String logFileName = home+"event_attr";
	static boolean evaluate = true;
	public static void main(String[] args) throws IOException {
		if (useParser)
			JetTest.initializeFromConfig("props/ace use parses.properties");
		else
			JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		Pat.trace = false;
		Resolve.trace = false;
		AceDocument.ace2005 = true;
		
		ArrayList<String> v = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			v.add(currentDocPath);
		}
		reader.close();
		
		FileWriter fw = new FileWriter(logFileName);
		fw.close();
		for (int runs = 0; runs < 10; runs++) {
			//shuffle the set of files first
			Collections.shuffle(v);
			fw = new FileWriter(logFileName,true);
			fw.write("runs=" + Integer.toString(runs) + "\n");
			fw.close();
			for (int fold = 0; fold < 10; fold++) {
				//split corpus first
				fw = new FileWriter(logFileName,true);
				fw.write("folds=" + Integer.toString(fold) + "\n");
				splitCorpus(fold,v);
				
				ETAttributeV1 et1 = new ETAttributeV1();
				et1.run();
				EventAttrEval ea = new EventAttrEval();
				ea.evalEvents(fileListTest);
				fw.write("SINGLE"+"\n");
				/*fw.write(Double.toString(ea.modP)+"\t"+Double.toString(ea.modR)+"\t"+Double.toString(ea.modF)+"\n");
				fw.write(Double.toString(ea.polP)+"\t"+Double.toString(ea.polR)+"\t"+Double.toString(ea.polF)+"\n");
				fw.write(Double.toString(ea.genP)+"\t"+Double.toString(ea.genR)+"\t"+Double.toString(ea.genF)+"\n");
				fw.write(Double.toString(ea.tenP)+"\t"+Double.toString(ea.tenR)+"\t"+Double.toString(ea.tenF)+"\n");
				*/
				fw.write(Integer.toString(ea.correctMod)+"\t"+Integer.toString(ea.spuriousMod)+"\t"+Integer.toString(ea.missingMod)+"\t"+Double.toString(ea.modP)+"\t"+Double.toString(ea.modR)+"\t"+Double.toString(ea.modF)+"\n");
				fw.write(Integer.toString(ea.correctPol)+"\t"+Integer.toString(ea.spuriousPol)+"\t"+Integer.toString(ea.missingPol)+"\t"+Double.toString(ea.polP)+"\t"+Double.toString(ea.polR)+"\t"+Double.toString(ea.polF)+"\n");
				fw.write(Integer.toString(ea.correctGen)+"\t"+Integer.toString(ea.spuriousGen)+"\t"+Integer.toString(ea.missingGen)+"\t"+Double.toString(ea.genP)+"\t"+Double.toString(ea.genR)+"\t"+Double.toString(ea.genF)+"\n");
				fw.write(Integer.toString(ea.correctTen)+"\t"+Integer.toString(ea.spuriousTen)+"\t"+Integer.toString(ea.missingTen)+"\t"+Double.toString(ea.tenP)+"\t"+Double.toString(ea.tenR)+"\t"+Double.toString(ea.tenF)+"\n");
				
				ETAttributeV6 et = new ETAttributeV6();
				
				ArrayList<String> v1 = new ArrayList<String>();
				reader = new BufferedReader(new FileReader(fileListTrain));
			
				while ((currentDocPath = reader.readLine()) != null) {
					v1.add(currentDocPath);
				}
				reader.close();
				
				//et.splitCorpus(fileList,150,fileListTest,fileListTrain);
				for (int i=0;i<BAG_NO;i++){
					
					if (trainPolModels){
						polFeatureFileName[i] = aceModels + "polFeatureFile"+i;
						et.polFeatureWriter[i] = new PrintStream (new FileOutputStream (polFeatureFileName[i]));
					}
					if (trainTenModels){
						tenFeatureFileName[i]=aceModels + "tenFeatureFile"+i;
						et.tenFeatureWriter[i] = new PrintStream (new FileOutputStream (tenFeatureFileName[i]));
						
					}
					if (trainGenModels){
						genFeatureFileName[i]=aceModels + "genFeatureFileName"+i;
						et.genFeatureWriter[i] = new PrintStream (new FileOutputStream (genFeatureFileName[i]));
						
					}
					if (trainModModels){
						modFeatureFileName[i]=aceModels + "modFeatureFileName"+i;
						et.modFeatureWriter[i] = new PrintStream (new FileOutputStream (modFeatureFileName[i]));
					}
				
					splitListTrain[i]=home + "trainfilelist"+i;
					et.randomSampling(v1,splitListTrain[i],v1.size());
					et.train(splitListTrain[i], 0,i);
					
					if (trainPolModels){
						polModelFileName[i]= aceModels + "polModel.log"+i;
						buildClassifierModel(polFeatureFileName[i], polModelFileName[i]);
					}
					if (trainTenModels){
						tenModelFileName[i]= aceModels + "tenModel.log"+i;
						buildClassifierModel(tenFeatureFileName[i], tenModelFileName[i]);
					}
					if (trainGenModels){
						genModelFileName[i]= aceModels + "genModel.log"+i;
						buildClassifierModel(genFeatureFileName[i], genModelFileName[i]);
					}
					if (trainModModels){
						modModelFileName[i]= aceModels + "modModel.log"+i;
						buildClassifierModel(modFeatureFileName[i], modModelFileName[i]);
					}
				}
				if (evaluate) {
					TEST_BAG_NO=30;
					et.loadAllModels(aceModels);
					for (TEST_BAG_NO=5;TEST_BAG_NO<=30;TEST_BAG_NO=TEST_BAG_NO+5){
						evaluateEventTagger(et);
						
						ea = new EventAttrEval();
						ea.evalEvents(fileListTest);
						fw.write("BAG"+TEST_BAG_NO+"\n");
						/*fw.write(Double.toString(ea.modP)+"\t"+Double.toString(ea.modR)+"\t"+Double.toString(ea.modF)+"\n");
						fw.write(Double.toString(ea.polP)+"\t"+Double.toString(ea.polR)+"\t"+Double.toString(ea.polF)+"\n");
						fw.write(Double.toString(ea.genP)+"\t"+Double.toString(ea.genR)+"\t"+Double.toString(ea.genF)+"\n");
						fw.write(Double.toString(ea.tenP)+"\t"+Double.toString(ea.tenR)+"\t"+Double.toString(ea.tenF)+"\n");
						*/
						fw.write(Integer.toString(ea.correctMod)+"\t"+Integer.toString(ea.spuriousMod)+"\t"+Integer.toString(ea.missingMod)+"\t"+Double.toString(ea.modP)+"\t"+Double.toString(ea.modR)+"\t"+Double.toString(ea.modF)+"\n");
						fw.write(Integer.toString(ea.correctPol)+"\t"+Integer.toString(ea.spuriousPol)+"\t"+Integer.toString(ea.missingPol)+"\t"+Double.toString(ea.polP)+"\t"+Double.toString(ea.polR)+"\t"+Double.toString(ea.polF)+"\n");
						fw.write(Integer.toString(ea.correctGen)+"\t"+Integer.toString(ea.spuriousGen)+"\t"+Integer.toString(ea.missingGen)+"\t"+Double.toString(ea.genP)+"\t"+Double.toString(ea.genR)+"\t"+Double.toString(ea.genF)+"\n");
						fw.write(Integer.toString(ea.correctTen)+"\t"+Integer.toString(ea.spuriousTen)+"\t"+Integer.toString(ea.missingTen)+"\t"+Double.toString(ea.tenP)+"\t"+Double.toString(ea.tenR)+"\t"+Double.toString(ea.tenF)+"\n");
						
					}
					
				}
				fw.close();
			}
		}
	}



	
	private static void evaluateEventTagger(ETAttributeV6 et) throws IOException {
		// et.assessPatterns (fileListTest);
		et.tag(fileListTest);
	}

	public void loadAllModels(String modelDir) throws IOException {
	/*	eventModel = loadClassifierModel(modelDir + "eventModel.log");
		// evTypeModel = loadClassifierModel(evTypeModelFileName);
		argModel = loadClassifierModel(modelDir + "argModel.log");
		roleModel = loadClassifierModel(modelDir + "roleModel.log");
		corefModel = loadClassifierModel(modelDir + "corefModel.log");*/
		for (int i=0;i<TEST_BAG_NO;i++){
			if (trainPolModels)
				polModel[i]=loadClassifierModel(polModelFileName[i]);
			if (trainModModels)
				modModel[i]=loadClassifierModel(modModelFileName[i]);
			if (trainGenModels)
				genModel[i]=loadClassifierModel(genModelFileName[i]);
			if (trainTenModels)
				tenModel[i]=loadClassifierModel(tenModelFileName[i]);
		}
		
	}

}
