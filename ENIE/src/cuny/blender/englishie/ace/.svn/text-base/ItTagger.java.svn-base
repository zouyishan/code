
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
import cuny.blender.englishie.nlp.lex.PorterStemmer;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.parser.*;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


import opennlp.maxent.*;
import opennlp.maxent.io.*;
import opennlp.model.EventStream;



/**
 *  assigns ACE events to a Document, given the entities, times, and values.
 */

public class ItTagger {


	PrintStream itFeatureWriter;

	static GISModel itModel;


	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	
	static final String ace =
			home + "source/";
	static final String aceModels =
			home + "model/";
	
	static boolean processOnlyOddDocuments = false;
	static boolean processOnlyEvenDocuments = false;

	static StringBuffer out = new StringBuffer();
	
	public ItTagger () {
	
	}

	public void train (String fileList, int pass) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			// if (docCount > 10) break;
			if (processOnlyOddDocuments && docCount%2 == 0) continue;
			if (processOnlyEvenDocuments && docCount%2 == 1) continue;
			System.out.println ("\nTrain file " + docCount+":"+ currentDocPath);
			String textFile = ace  + currentDocPath;
			String xmlFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm","");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);

			Resolve.ACE = true;
			Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			if (pass == 0) {
				trainItClassifier(doc, aceDoc, currentDocPath);
			}
		}
		reader.close();
	}

	private void trainItClassifier(Document doc, AceDocument aceDoc,
			String currentDocPath) throws IOException {
		
		/*Vector<Span> itSpans = new Vector<Span>();
		for (int i=0;i<aceDoc.events.size();i++)
		{
			AceEvent event = (AceEvent) aceDoc.events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention em = (AceEventMention)event.mentions.get(j);
				String str = em.anchorText.replace("\n", " ").toLowerCase();
				if (str.equals("it")){
					itSpans.add(em.anchorExtent);
					Annotation s = findContainingSentence(doc,em.anchorExtent);
					if (s==null){
						continue;
					}
					Datum d = itFeatures(doc,em.anchorExtent,s);
					d.setOutcome("yes");
					itFeatureWriter.println(d.toString());
				}
			}
		}
		
		for (int i=0;i<aceDoc.entities.size();i++)
		{
			AceEntity entity = (AceEntity) aceDoc.entities.get(i);
			for (int j=0;j<entity.mentions.size();j++){
				AceEntityMention em = (AceEntityMention)entity.mentions.get(j);
				String str = em.headText.replace("\n", " ").toLowerCase();
				if (str.equals("it")){
					itSpans.add(em.head);
					Annotation s = findContainingSentence(doc,em.head);
					if (s==null){
						continue;
					}
					Datum d = itFeatures(doc,em.head,s);
					d.setOutcome("yes");
					itFeatureWriter.println(d.toString());
				}
			}
		}*/
		
		
		Vector sentences = doc.annotationsOfType("sentence");
		
		for (int i = 0; i < sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			
			Vector<Annotation> tokens = doc.annotationsOfType("token",s.span());
			if (tokens != null) {
				for (Annotation token : tokens) {
					doc.shrink(token);
					Span tokenSpan = token.span();
					String term = doc.text(tokenSpan).toLowerCase().trim();
					if (term.equals("it")/*&&!itSpans.contains(tokenSpan)*/){
						Datum d = null;//itFeatures(doc,tokenSpan,s);
						AceEntityMention em1 = findContainingEntity(aceDoc,tokenSpan);
						if (em1!=null){
							d.setOutcome("yes");
						}
						AceEventMention em2 = findContainingEvent(aceDoc,tokenSpan);
						if (em2!=null){
							d.setOutcome("yes");
						}
						if (em1==null && em2==null)
							d.setOutcome("no");
						itFeatureWriter.println(d.toString());
					}				
				}
			}
		}
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
	
	private boolean findIt(AceDocument aceDoc, Span s) {
		for (int i=0;i<aceDoc.events.size();i++)
		{
			AceEvent event = (AceEvent) aceDoc.events.get(i);
			for (int j=0;j<event.mentions.size();j++){
				AceEventMention em = (AceEventMention)event.mentions.get(j);
				if (em.anchorExtent.within(s))
					return true;
			}
		}
		
		for (int i=0;i<aceDoc.entities.size();i++)
		{
			AceEntity entity = (AceEntity) aceDoc.entities.get(i);
			for (int j=0;j<entity.mentions.size();j++){
				AceEntityMention em = (AceEntityMention)entity.mentions.get(j);
				if (em.head.within(s))
					return true;
			}
		}
		
		return false;
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
			System.err.println("findContainingSentence:  can't find sentence with span");
			return null;
		}
		return sentence;
	}
		
	static Stemmer ps = Stemmer.getDefaultStemmer();
	
	private Datum itFeatures(AceDocument aceDoc, Document doc, Span span, Annotation s) {
		Datum d = new Datum();
		
		if (s==null)
			return d;
		
		int posn = span.start();
		String[] bstr = new String[4];
		String[] astr = new String[4];
		String[] bpos = new String[4];
		String[] apos = new String[4];
		
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
		
				String word = doc.text(token).trim().toLowerCase();
				if (word.length()>1&&cat!=null)
					word = ps.getStem(word, cat);
				
				if (i <=3) {
					bstr[i] = word;
					bpos[i] = cat;
				}
				else{
					break;
				}
				
				
				if (word.equals(","))
					break;
				
				i++;
			}
		}
		i = 0;
		posn = span.end();
		while (posn < s.end()) {
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
				
				String word = doc.text(token).trim().toLowerCase();
				if (word.length()>1&&cat!=null)
					word = ps.getStem(word, cat);
				
				if (i<=3) {
					// only get the first left token
					astr[i] = word;
					apos[i] = cat;
				}
				else{
					break;
				}
				i++;
			}
		}

		for (int j=0;j<4;j++){
			if (bstr[j]!=null) {
				d.addFV("bw"+Integer.toString(j), bstr[j]);
			}
			if (astr[j]!=null) {
				d.addFV("aw"+Integer.toString(j), astr[j]);
			}
			if (bpos[j]!=null) {
				d.addFV("bp"+Integer.toString(j), bpos[j]);
			}
			if (apos[j]!=null) {
				d.addFV("ap"+Integer.toString(j), apos[j]);
			}
	
			if (bstr[j]!=null&&bpos[j]!=null) {
				d.addFV("bwbp"+Integer.toString(j), bstr[j] + ":" + bpos[j]);
			}
			
			if (astr[j]!=null&&apos[j]!=null) {
				d.addFV("awap"+Integer.toString(j), astr[j] + ":" + apos[j]);
			}
			/*if (apos[j]!=null) {
				d.addFV("apos"+Integer.toString(j), anchorCat + ":" + apos[j]);
			}*/
		}
		
		

		return d;
	}


	public void tag (String fileList) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println ("\nTag file " + docCount+":"+currentDocPath);
		 
			String textFile = ace +  currentDocPath;
			String xmlFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");
			//String outputFile = home + "output/" + currentDocPath.replaceFirst(".sgm", ".apf");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
			
			Resolve.ACE = true;
			Control.processDocument (doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			//aceDoc.events.clear();
			tag (doc, aceDoc, currentDocPath.replaceFirst(".sgm",""), aceDoc.docID);
			//aceDoc.write(new PrintWriter (new FileWriter (outputFile)),doc);
		}
	}

	static StringBuffer buf = new StringBuffer();
	static StringBuffer log = new StringBuffer();
	double COREF_THRESHOLD = 0.6;
	public void tag(Document doc, AceDocument aceDoc, String currentDocPath,String docId) {
		Vector sentences = doc.annotationsOfType("sentence");
		
		out.append(aceDoc.docID+".sgm\n");
		for (int i = 0; i < sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			
			Vector<Annotation> tokens = doc.annotationsOfType("token",s.span());
			if (tokens != null) {
				for (Annotation token : tokens) {
					doc.shrink(token);
					Span tokenSpan = token.span();
					String term = doc.text(tokenSpan).toLowerCase().trim();
					if (term.equals("it")){
						Datum d = null;//itFeatures(doc,tokenSpan,s);

						String itRes = itModel.getBestOutcome(itModel.eval(d.toArray())).intern();
						double itProb = itModel.eval(d.toArray())[itModel.getIndex(itRes)];
						/*if (itProb<COREF_THRESHOLD){
							itRes="no";
							itProb= 1-itProb;
						}*/
						log.append(d.toString()+":"+itRes+"("+Double.toString(itProb)+")\n");
						out.append(tokenSpan.start()+"\t"+tokenSpan.end()+"\t"+itRes+"\t"+itProb+"\n");
					}				
				}
			}
		}
	}

	static void splitCorpus(int fold, ArrayList v,ArrayList v1, int N) throws IOException {
		PrintStream trainWriter = new PrintStream(new FileOutputStream(
				fileListTrain));
		PrintStream testWriter = new PrintStream(new FileOutputStream(
				fileListTest));
		for (int i = 0; i<v1.size();i++){
			String str = (String) v1.get(i);
			testWriter.println(str);
		}
				
		int count = 0;
		for (int i = 0; i < v.size(); i++) {
			String str = (String) v.get(i);

			
			/*if (i >= fold * v.size()/3 && i < (fold + 1) * v.size()/3)*/
			if (v1.contains(str))
				continue;
			if (count<N){
				testWriter.println(str);
				count++;
			}
			else
				trainWriter.println(str);
		}
		trainWriter.close();
		testWriter.close();
	}
	
	static final String fileList = home + "totalfilelist";
	static final String itfileList = home + "itfilelist";
	static final String fileListTrain = home + "trainfilelist";
	static final String fileListTest = home + "testfilelist";
	static final boolean training = true;
	static final boolean evaluate = true;
	static final String outFileName = home +"itoutput";
	static final String logFileName = home +"itlog";
	static final String bufFileName = home +"itbuf.html";
	public static void main (String[] args) throws IOException {
		
		JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		Pat.trace = false;
		Resolve.trace = false;
		AceDocument.ace2005 = true;
		ItTagger et = new ItTagger();
		
		/*ArrayList<String> v = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			v.add(currentDocPath);
		}
		reader.close();
		
		ArrayList<String> v1 = new ArrayList<String>();
		reader = new BufferedReader(new FileReader(itfileList));
		while ((currentDocPath = reader.readLine()) != null) {
			v1.add(currentDocPath);
		}
		reader.close();
		
		Collections.shuffle(v);
		splitCorpus(0, v,v1,100);*/
		if (training) {
			et.trainItModel();
		}
		if (evaluate) {
			et.loadAllModels(aceModels);
			
			et.tag(fileListTest);
			FileWriter fw = new FileWriter(outFileName);
			fw.append(out.toString());
			fw.close();
			
			fw = new FileWriter(logFileName);
			fw.append(log.toString());
			fw.close();
			et.evaluate(outFileName);
		}
		
		
	}

	
	static int correctEvents, missingEvents, spuriousEvents;
	static int correctEntities, missingEntities;
	
	private void evaluate(String tagfile) throws IOException {
		//BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		BufferedReader reader = new BufferedReader(new FileReader(tagfile));
		String line;
		int correct = 0;
		int missing = 0;
		int spurious = 0;
		ExternalDocument doc=null;
		AceDocument aceDoc=null;
		int count = 0;
		while ((line = reader.readLine()) != null) {
			if (line.endsWith(".sgm")){
				doc=null;
				aceDoc=null;
				docCount++;
				System.out.println ("\nEvaluate file " + docCount+":"+line);
			 
				String textFile = ace +  line;
				String xmlFile = ace + line.replaceFirst(".sgm", ".apf.xml");
				//String outputFile = home + "output/" + currentDocPath.replaceFirst(".sgm", ".apf");
				
				doc = new ExternalDocument("sgml", textFile);
				doc.setAllTags(true);
				doc.open();
				doc.stretchAll();
				cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
				System.out.println(">>> Monocase is " + cuny.blender.englishie.ace.Ace.monocase);
				
				Resolve.ACE = true;
				Control.processDocument (doc, null, false, 0);
				aceDoc = new AceDocument(textFile, xmlFile);
				buf.append("+++++++++++++++++++++++<br>");
				buf.append(line+"<br>");
			}
			else
			{
				if (aceDoc==null ||doc==null)
					return;
				count++;
				String [] items =  line.split("\\s+");
				int start = Integer.parseInt(items[0]); 
				int end = Integer.parseInt(items[1]);
				String tag = items[2];
				Span tokenSpan = new Span(start,end);
				Annotation s = findContainingSentence(doc,tokenSpan);
				String str1 = doc.text(s);
				String str2=str1.substring(0,tokenSpan.start()-s.span().start());
				String str3=str1.substring(tokenSpan.end()+1-s.span().start());
				String str4 = doc.text(new Span(tokenSpan.start(),tokenSpan.end()+1));
				
				String str ="";
				boolean bFound = findIt(aceDoc,tokenSpan);
				if (tag.equals("yes")&&bFound){
					correct++;
					str="C"+correct+":"+ str2+"<font color=\"green\">"+str4+"</font>"+str3;
					
				}
				else if (tag.equals("no")&&bFound){
					missing++;
					str="M"+missing+":"+ str2+"<font color=\"purple\">"+str4+"</font>"+str3;
					}
				else if (tag.equals("yes")&&!bFound){
					spurious++;
					str="W"+spurious+":"+ str2+"<font color=\"red\">"+str4+"</font>"+str3;
				}
				else{
					correct++;
					str="C"+correct+":"+ str2+"<font color=\"blue\">"+str4+"</font>"+str3;
				}
				str=str.replace("\n", " ");
				str = str.replaceAll("\\b\\s{2,}\\b", " ");
				buf.append(str+"<br>");
			}
		}
		int total = 703;
		int yes = 135;
		System.out.println("total="+total);
		System.out.println("correct="+correct);
		System.out.println("spurious="+spurious);
		System.out.println("missing="+missing);
		double p=(double)correct/total;
		double r=(double)(yes-missing)/yes;
		double f = 2*p*r/(p+r);
		System.out.println("P="+Double.toString(p));
		System.out.println("r="+Double.toString(r));
		System.out.println("f="+Double.toString(f));
		reader.close();
		
		FileWriter fw = new FileWriter(bufFileName);
		fw.append(buf.toString());
		fw.close();
	}
	
	
	
	static String itFeatureFileName = aceModels + "itFeatureFile.log";
	static String itModelFileName = aceModels + "itModel.log";
	
	private void trainItModel() throws IOException {
		itFeatureWriter = new PrintStream(new FileOutputStream(
				itFeatureFileName));
		
		
		train(fileListTrain,0);
		
		itFeatureWriter.close();
		buildClassifierModel(itFeatureFileName, itModelFileName);
		
	}

	private static void evaluateEventTagger (ItTagger et)  throws IOException {
		et.loadAllModels (aceModels);
		// et.assessPatterns (fileListTest);
		et.tag (fileListTest);
	}

	public void loadAllModels (String modelDir) throws IOException {
		itModel = loadClassifierModel(modelDir + "itModel.log");
		
	}

	private void buildClassifierModel(String featureFileName,
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
	
	private GISModel loadClassifierModel(String modelFileName) {
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

}
