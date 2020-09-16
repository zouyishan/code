package cuny.blender.englishie.ace;


import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lisp.FeatureSet;
import cuny.blender.englishie.nlp.parser.StanfordParser;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;
import cuny.blender.englishie.nlp.tipster.ExternalDocument;
import cuny.blender.englishie.nlp.tipster.Span;
import cuny.blender.englishie.nlp.util.Tree;
import cuny.blender.englishie.nlp.util.Trees.PennTreeReader;

import edu.stanford.nlp.trees.TypedDependency;

import opennlp.maxent.*;
import opennlp.maxent.io.*;

//english relation tagger using dependency features
/**
 * assigns ACE events to a Document, given the entities, times, and values.
 */

public class DependencyParse {
	static String encoding = "UTF-8";
	static String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	static String fileList = home + "totalfilelist1";
	static String parseDir = home+"parsed/";

	static String dataDir = home + "source/";
	
	public void parse(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("Process file " + docCount + ":" + currentDocPath);
		
			String textFile = dataDir + currentDocPath;
			/*String xmlFile = dataDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			*/String docId = currentDocPath.replaceFirst(".sgm", "");

			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			Ace.monocase = Ace.allLowerCase(doc);
			System.out.println (">>> Monocase is " + Ace.monocase);
			Control.processDocument (doc, null, docCount < 0, docCount);
			//AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			//parse(doc, aceDoc, docId);
			parse(doc, docId);
		}
		reader.close();
	}

	
	private void parse(Document doc, AceDocument aceDoc,
			String docId) throws IOException {
		
		Vector sentences = doc.annotationsOfType("sentence");
		
		ArrayList<AceEntityMention> allEntityMentionsList = new ArrayList();
		getAllEntityMentions(aceDoc,allEntityMentionsList);
		AceEntityCompare comp = new AceEntityCompare();
		Collections.sort(allEntityMentionsList, comp);
		
		String parsefile = parseDir+docId+".parse";
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(parsefile), "UTF-8"));
		
		for (int i = 0; i < sentences.size(); i++) {
			Annotation ann = (Annotation) sentences.get(i);
			
			Vector<AceEntityMention> ms = findEntityInSentence(doc,aceDoc,ann);
			if (ms.size()<=1)	
				continue;
			String s = doc.text(ann);
			s=s.replace("\n", " ");
			s=s.replaceAll("\\b\\s{2,}\\b", " ");
			
			if (s.length()>500)
				continue;
			System.out.println("Sentence "+(i+1)+"/"+sentences.size());
			List<TypedDependency> tdl = StanfordParser.parse2(s);
			if (tdl==null)
				continue;
			String dep = "";
			for (TypedDependency td: tdl){
				dep=dep+td.toString()+",";
			}
			dep = dep.substring(0,dep.length()-1);
			fw.append(i+"\t"+dep+"\n");
			String parse = StanfordParser.parse(s);
			fw.append(i+"\t"+parse+"\n");
		}
		fw.close();
	}

	private void parse(Document doc, String docId) throws IOException {
		Vector sentences = doc.annotationsOfType("sentence");
		
		String parsefile = parseDir+docId+".parse";
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(parsefile), "UTF-8"));
		
		for (int i = 0; i < sentences.size(); i++) {
			Annotation ann = (Annotation) sentences.get(i);
			
			String s = doc.text(ann);
			s=s.replace("\n", " ");
			s=s.replaceAll("\\b\\s{2,}\\b", " ");
			
			if (s.length()>500)
				continue;
			//System.out.println(s);
			System.out.println("Sentence "+(i+1)+"/"+sentences.size());
			List<TypedDependency> tdl = StanfordParser.parse2(s);
			if (tdl==null)
				continue;
			String dep = "";
			for (TypedDependency td: tdl){
				dep=dep+td.toString()+",";
			}
			if (dep.length()>0)
				dep = dep.substring(0,dep.length()-1);
			fw.append(i+"\t"+dep+"\n");
			String parse = StanfordParser.parse(s);
			fw.append(i+"\t"+parse+"\n");
		}
		fw.close();
	}
	
	public static void getAllEntityMentions(AceDocument aceDoc,ArrayList<AceEntityMention> allEntityMentionsList){
		for (int i=0; i<aceDoc.entities.size(); i++) {
			AceEntity entity = (AceEntity) aceDoc.entities.get(i);
			
			allEntityMentionsList.addAll(entity.mentions);
		}
	}
	
	
	public static int findPos(ArrayList<AceEntityMention> allEntityMentionsList,AceEntityMention m2){
		for (int i=0;i<allEntityMentionsList.size();i++){
			if (allEntityMentionsList.get(i).equals(m2)){
				return i;
			}
		}
		return -1;
	}
	
	
	private Vector<AceEntityMention> findEntityInSentence(Document doc, AceDocument aceDoc, Annotation s) {
		Vector<AceEntityMention> candidates = new Vector<AceEntityMention>();
		Span sp = doc.convertSpan(s.span());
		ArrayList mentions = aceDoc.getAllMentions();
		for (int j=0; j<mentions.size(); j++) {
			AceMention m = (AceMention) mentions.get(j);
			if (m instanceof AceEntityMention && m.extent.within(sp)) {
				candidates.add((AceEntityMention) m);
			}
		}
		
		return candidates;
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
	
	
	public static void main(String[] args) throws Exception {
		StanfordParser.initialize();
		String  propertyFile= home + "../../props/tfidf.properties";
		// initialize Jet
		JetTest.initializeFromConfig (propertyFile);
		
		Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
		
		
		DependencyParse p = new DependencyParse();
		
		
		p.parse(fileList);
				
	
		
	}
	
	

}