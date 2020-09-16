//Description: This file generates a list of terms, and tf, idf information in the corpus

package cuny.blender.englishie.algorithm.tfidf;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.parser.SynFun;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;

import cern.colt.matrix.*;

public class TfIdf {
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	//static final String home = "/jar/workspace/graph/corpus/ACE05/";
	static final String ace = home + "source/";
	static String fileList = home + "totalfilelist";
	static String logFile = home + "tfidf";
	static String termFile = home +"terms";
	
	static String stopwordFile = home + "stoplist";
	public static Vector<String> stopList = new Vector<String>();
	
	//list of tokens
	static Vector<String> terms = new Vector<String>();
	public static TreeMap<String,Integer>  termMap = new TreeMap<String,Integer>();
	public static TreeMap<String,Integer>  docMap = new TreeMap<String,Integer>();
	static int termNo = 11854;
	static int docNo = 599;
	
	//doc id and token number pair
	static Vector<Integer> docTermNo = new Vector<Integer>();
	
	static boolean useStem = true;
	static Stemmer ps = Stemmer.getDefaultStemmer();
	
	public static DoubleMatrix2D wordFreq;
	
	//static DoubleMatrix2D 
	public static void main(String[] args) throws IOException {
		JetTest.initializeFromConfig("props/MEace06.properties");
		Pat.trace = false;
		Resolve.trace = false;
		loadStopwordFile();
			
		termCount(fileList);
		
		FileWriter fw = new FileWriter(logFile);
		StringBuffer buf = new StringBuffer();
		
		buf.append("Total terms:"+terms.size()+"\n\n");
		for (int i=0;i<terms.size();i++)
		{
			buf.append(terms.get(i)+"\n");
		}
		fw.flush();
		fw.write(buf.toString());
		fw.close();
	}
	
	public static boolean isNumber(String s) {
		/*try {
			Double.parseDouble(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
			return true;*/
		for (int i=0;i<s.length();i++){
			if (Character.isDigit(s.charAt(i)))
				return true;
		}
		return false;
	}
	
	static protected boolean useSparseMatrix = true;
	public static void main1(String[] args) throws IOException {
		//JetTest.initializeFromConfig("props/tfidf.properties");
		JetTest.initializeFromConfig("props/MEace06.properties");
		loadStopwordFile();
		loadTermFile();
		if (useSparseMatrix)
			wordFreq = DoubleFactory2D.sparse.make(termNo, docNo);
		else
			wordFreq = DoubleFactory2D.dense.make(termNo, docNo);
		
		wordFreqCount(fileList);
		
		StringBuffer buf = new StringBuffer();
		for (int i=0;i<termNo;i++){
			int docFreq = 0;
			for (int j =0 ;j<docNo;j++){
				if (wordFreq.get(i, j)>0)
					docFreq++;
			}
			for (int j=0;j<docNo;j++){
				System.out.println(wordFreq.get(i,j)+":"+docTermNo.get(j)+":"+docFreq);
				double weight = (wordFreq.get(i,j)/docTermNo.get(j))*Math.log((double)docNo/docFreq);
				wordFreq.set(i,j,weight);
			}
			//buf.append(i+":"+docFreq+"\n");
		}
		
		/*FileWriter fw = new FileWriter(logFile);
		
		fw.flush();
		fw.write(wordFreq.toString());
		fw.close();*/
	}
	
	public static void genTfIdf() throws IOException {
		JetTest.initializeFromConfig("props/tfidf.properties");
		loadStopwordFile();
		loadTermFile();
		if (useSparseMatrix)
			wordFreq = DoubleFactory2D.sparse.make(termNo, docNo);
		else
			wordFreq = DoubleFactory2D.dense.make(termNo, docNo);
		
		wordFreqCount(fileList);
		
		StringBuffer buf = new StringBuffer();
		for (int i=0;i<termNo;i++){
			int docFreq = 0;
			for (int j =0 ;j<docNo;j++){
				if (wordFreq.get(i, j)>0)
					docFreq++;
			}
			for (int j=0;j<docNo;j++){
				//System.out.println(wordFreq.get(i,j)+":"+docTermNo.get(j)+":"+docFreq);
				double weight = (wordFreq.get(i,j)/docTermNo.get(j))*Math.log((double)docNo/docFreq);
				wordFreq.set(i,j,weight);
			}
			//buf.append(i+":"+docFreq+"\n");
		}
		
		/*FileWriter fw = new FileWriter(logFile);
		
		fw.flush();
		fw.write(wordFreq.toString());
		fw.close();*/
	}
	
	
	
	static public void wordFreqCount(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			
			
			System.out.println("\nProcess file " + Integer.toString(docCount)
					+ ":" + currentDocPath);
			String textFile = ace + currentDocPath;
			String xmlFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			
			Control.processDocument(doc, null, false, 0);
			wordFreqCount(doc,docId,docCount);
			docMap.put(docId, docCount);
			docCount++;
			
		}
		reader.close();
	}
	
	static public void termCount(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			
			System.out.println("\nProcess file " + Integer.toString(docCount)
					+ ":" + currentDocPath);
			String textFile = ace + currentDocPath;
			String xmlFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			
			Control.processDocument(doc, null, false, 0);
			termCount(doc,docId);
		}
		reader.close();
	}

	static void termCount (Document doc,String docId) {
		Vector constituents = doc.annotationsOfType("constit");
		int tokenNo = 0;
		if (constituents != null) {
			for (int i=0; i<constituents.size(); i++) {
				Annotation constit = (Annotation) constituents.get(i);
				String cat = (String) constit.get("cat");
				if (cat == "n" ||cat == "v" || cat == "tv" || cat == "ven" ||
				    cat == "ving" /*|| cat == "adj"*/) {
					String term=SynFun.getHead(doc, constit).toLowerCase();
				
					if (stopList.contains(term))
						continue;
					//System.out.println(term+":"+term.length());
					/*if (term.length()<=2)
						continue;
					if (isNumber(term))
						continue;
					term = term.replace(".", "");*/
					tokenNo++;
					if (useStem)
					{
						term = ps.getStem(term);
					}
					if (!terms.contains(term)){
						terms.add(term);
					}
				}
			}
			docTermNo.add(tokenNo);
		}
			
	}
	
	static void termCount1 (Document doc,String docId) {
		Vector<Annotation> tokens = doc.annotationsOfType ("token");
		int tokenNo = 0;
		
		if (tokens != null) {
			for (Annotation token : tokens) {
				doc.shrink(token);
				Span tokenSpan = token.span();
				String term = doc.text(tokenSpan).toLowerCase().trim();
				
				if (stopList.contains(term))
					continue;
				//System.out.println(term+":"+term.length());
				if (term.length()<=2)
					continue;
				if (isNumber(term))
					continue;
				term = term.replace(".", "");
				tokenNo++;
				if (useStem)
				{
					term = ps.getStem(term);
				}
				if (!terms.contains(term)){
					terms.add(term);
				}
			}
			docTermNo.add(tokenNo);
		}
			
	}

	static void wordFreqCount (Document doc,String docId, int docCount) {
		Vector<Annotation> tokens = doc.annotationsOfType ("token");
		int tokenNo = 0;
		
		if (tokens != null) {
			for (Annotation token : tokens) {
				doc.shrink(token);
				Span tokenSpan = token.span();
				String term = doc.text(tokenSpan).toLowerCase();
				if (stopList.contains(term))
					continue;
				//System.out.println(term+":"+term.length());
				if (term.length()<=2)
					continue;
				if (isNumber(term))
					continue;
				term = term.replace(".", "");
				tokenNo++;
				if (useStem)
				{
					term = ps.getStem(term);
				}
				if (termMap.containsKey(term)){
					int row = termMap.get(term);
					double freq = wordFreq.get(row, docCount);
					freq++;
					wordFreq.set(row,docCount,freq);
				}
				else{
					System.out.println("error");
				}
			}
			docTermNo.add(tokenNo);
		}
			
	}
	
	static public void loadStopwordFile() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(stopwordFile)));
		String stopword;
		while ((stopword = reader.readLine()) != null) {
			stopList.add(stopword);
		}
		reader.close();
	}
	
	static public void loadTermFile() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(termFile)));
		String term;
		int termNo = 0;
		while ((term = reader.readLine()) != null) {
			termMap.put(term,termNo);
			termNo++;
		}
		reader.close();
	}
}
