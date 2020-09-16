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
import opennlp.model.EventStream;

//english relation tagger using dependency features
/**
 * assigns ACE events to a Document, given the entities, times, and values.
 */

public class RelationTagger5 {
	static String encoding = "UTF-8";
	static String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	
	static String fileList = home + "totalfilelist";
	static String trainfileList = home + "trainfilelist";
	static String devfileList = home + "devfilelist";
	static String testfileList = home + "testfilelist";
	
	static String parseDir = home +"map/";
	static String logDir = home+"log/";
	static String exDir = logDir+"ex7/";
	static String logFileName = home+"relation_error.html";

	static String tagDir = home+"output/relation2/";
	static String aceModels = home +"model/relation4/";

	static GISModel relationIdenModel;
	static GISModel relationModel;
	
	static String relationIdenFeatureFileName = aceModels + "relationIdenFeatureFile.log.sampled";
	static String relationIdenModelFileName = aceModels + "relationIdenModel.log";
	static String relationFeatureFileName =  aceModels + "relationFeatureFile.log";
	static String relationModelFileName = aceModels + "relationModel.log";
	
	static PrintStream relationIdenWriter;
	static PrintStream relationFeatureWriter;
	static String dataDir = home + "source/";
	static String mapDir = home + "map/";
	

	public void transfer(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			String file = mapDir + currentDocPath.replace(".sgm", ".parse");
			BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), "UTF-8"));
			
			
			System.out.println("Process file " + docCount + ":" + currentDocPath);
			String parsefile = parseDir + currentDocPath.replace(".sgm", ".parse");
			BufferedReader reader2 = new BufferedReader(new FileReader(parsefile));
			String line;
			while ((line = reader2.readLine()) != null) {
				line = line.replaceAll("\\),","\\)##");
				fw.append(line+"\n");
				line = reader2.readLine();
				fw.append(line+"\n");
			}
			reader2.close();
			fw.close();
		}
		reader.close();
	}
	
	public void train(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("Train file " + docCount + ":" + currentDocPath);
		
			String textFile = dataDir + currentDocPath;
			String xmlFile = dataDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm", "");

			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			Ace.monocase = Ace.allLowerCase(doc);
			System.out.println (">>> Monocase is " + Ace.monocase);
			Control.processDocument (doc, null, docCount < 0, docCount);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			trainRelationClassifier(doc, aceDoc, docId);
		}
		reader.close();
	}

	private static void selectFeaturefile(String file) throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		String file2= file+".sampled";
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file2), "UTF-8"));
		
		Vector<String> pos = new Vector<String>();
		Vector<String> neg = new Vector<String>();
		while ((line = reader.readLine()) != null) {
			String [] str = line.split("\\s+");
			if (str[str.length-1].equals("yes"))
				pos.add(line);
			else if (str[str.length-1].equals("no")){
				neg.add(line);
			}
		}
		reader.close();
		Collections.shuffle(neg);
		for (int i=0;i<pos.size();i++){
			fw.append(pos.get(i)+"\n");
		}
		for (int i=0;i<pos.size()*1.5;i++){
			fw.append(neg.get(i)+"\n");
		}
		fw.close();
		
	}
	

	private void trainRelationClassifier(Document doc, AceDocument aceDoc,
			String docId) throws IOException {
		
		Vector sentences = doc.annotationsOfType("sentence");
		
		ArrayList<AceEntityMention> allEntityMentionsList = new ArrayList();
		getAllEntityMentions(aceDoc,allEntityMentionsList);
		AceEntityCompare comp = new AceEntityCompare();
		Collections.sort(allEntityMentionsList, comp);
		
		String [] dep = new String[sentences.size()];
		String [] parse = new String[sentences.size()];
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(parseDir+docId+".parse"));
		while (( line= reader.readLine()) != null) {
			if (line.indexOf('\t')!=line.length()-1){
				String[] arr = line.split("\t");
				int id = Integer.parseInt(arr[0]);
				dep[id] = arr[1];
			}
			line= reader.readLine();
			if (line.indexOf('\t')!=line.length()-1){
				String[]arr = line.split("\t");
				int id = Integer.parseInt(arr[0]);
				parse[id] = arr[1];
			}
		}
		reader.close();
		
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
			
			String depResult = dep[i];
			String parseResult = parse[i];
			for (int j = 0; j<ms.size();j++){
				AceEntityMention m1 = ms.get(j);
				for (int k=0;k<ms.size();k++){
					if (k==j)
						continue;
					AceEntityMention m2 = ms.get(k);
					
					FeatureSet f = relationFeatures(depResult,parseResult,doc,aceDoc,ann.span(),m1,m2, allEntityMentionsList);
					if (f.size()==0)
						continue;
					AceRelation r = findRelation(aceDoc,m1,m2);
					
					if (r!=null){
						if (categoryTable.contains(r.subtype)){
							f.setOutcome("yes");
							relationIdenWriter.println(f.toString());
							f.setOutcome(r.subtype);
							relationFeatureWriter.println(f.toString());
						}
						else{
							f.setOutcome("no");
							relationIdenWriter.println(f.toString());
						}
					}
					else{
						f.setOutcome("no");
						relationIdenWriter.println(f.toString());
					}
				}
			}
		}
	}

	
	private AceRelation findRelation(AceDocument aceDoc, AceEntityMention m1, AceEntityMention m2){
		ArrayList relations = aceDoc.relations;
	
		if (relations.size()==0)
		{
			return null;
		}
		
		for (int i = 0; i < relations.size(); i++) {
			AceRelation relation = (AceRelation) relations.get(i);
			for (int j = 0; j<relation.mentions.size();j++){
				AceRelationMention rm = (AceRelationMention)relation.mentions.get(j);
				if (rm.arg1.equals(m1)&&rm.arg2.equals(m2))
					return relation;
			}
		}
		return null;
	}
	
	private FeatureSet relationFeatures(String depResult, String parseResult, Document doc, AceDocument aceDoc, Span sentence, AceEntityMention m1,AceEntityMention m2,ArrayList<AceEntityMention> allEntityMentionsList){
		FeatureSet f = new FeatureSet();
		
		/**************lexical features*******************/
		//bag-of-words in M1
		String text1=m1.text;
		String text2=m2.text;
		String head1=m1.headText;
		String head2=m2.headText;
		
		text1=text1.replace("\n", " ");
		text1 = text1.replaceAll("\\b\\s{2,}\\b", " ");
		text2=text2.replace("\n", " ");
		text2 = text2.replaceAll("\\b\\s{2,}\\b", " ");
		head1=head1.replace("\n", " ");
		head1 = head1.replaceAll("\\b\\s{2,}\\b", " ");
		head2=head2.replace("\n", " ");
		head2 = head2.replaceAll("\\b\\s{2,}\\b", " ");
		
		if (head1.contains(head2)||head2.contains(head1))
			return f;
		f.addFV("hm1",head1);
		f.addFV("hm2",head2);
		String hm12= head1+":"+head2;
		f.addFV("hm12",hm12);
		
		/*StringTokenizer st = new StringTokenizer(head1);
		int i = 0;
		while (st.hasMoreTokens()) {
			i++;
			f.addFV("hm1"+i,st.nextToken());
		}
		
		st = new StringTokenizer(head2);
		i=0;
		while (st.hasMoreTokens()) {
			i++;
			f.addFV("hm2"+i,st.nextToken());
		}*/
			

		Span  s1,s2,s3;
		if (m1.head.start()<m2.head.start()){
			s1=new Span(sentence.start(),m1.head.start());
			s2=new Span(m1.head.end()+1,m2.head.start());
			s3=new Span(m2.head.end()+1,sentence.end());
		}
		else{
			s1=new Span(sentence.start(),m2.head.start());
			s2=new Span(m2.head.end()+1,m1.head.start());
			s3=new Span(m1.head.end()+1,sentence.end());
		}
		
		String str = doc.text(s1).toLowerCase();
		str=str.replace("\n", " ");
		str = str.replaceAll("\\b\\s{2,}\\b", " ");
		StringTokenizer st = new StringTokenizer(str);
		ArrayList<String> arr = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		int i = 0;
		if (arr.size()>=2){
			f.addFV("b0",arr.get(arr.size()-1));
			f.addFV("b1",arr.get(arr.size()-2));
		}
		else if (arr.size()==1){
			f.addFV("b0",arr.get(arr.size()-1));
		}
			
		str = doc.text(s2).toLowerCase();
		str=str.replace("\n", " ");
		str = str.replaceAll("\\b\\s{2,}\\b", " ");
		st = new StringTokenizer(str);
		arr = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		for (int j=0;j<arr.size();j++){
			f.addFV("m"+j,arr.get(j));
		}
		
		str = doc.text(s3).toLowerCase();
		str=str.replace("\n", " ");
		str = str.replaceAll("\\b\\s{2,}\\b", " ");
		st = new StringTokenizer(str);
		arr = new ArrayList<String>();
		while (st.hasMoreTokens()) {
			arr.add(st.nextToken());
		}
		if (arr.size()>=2){
			f.addFV("a0",arr.get(0));
			f.addFV("a1",arr.get(1));
		}
		else if (arr.size()==1){
			f.addFV("a0",arr.get(0));
		}
		
		
		/**************entity features*******************/
		String et1= m1.entity.subtype;
		String et2= m2.entity.subtype;
		String et12= m1.entity.type+":"+m2.entity.type;
		String set12 = m1.entity.subtype+":"+m2.entity.subtype;
		f.addFV("type1",et1);
		f.addFV("type2",et2);
		
		f.addFV("type12",et12);
		f.addFV("subtype12",set12);
		f.addFV("mtype12",m1.type+":"+m2.type);
		//f.addFV("ML12", m1.type+":"+m2.type);
		
		
		/**************structure features*******************/
		if (m1.extent.within(m2.extent)){
			f.addFV("s1","1");
		}
		else if (m2.extent.within(m1.extent)){
			f.addFV("s1","2");
		}
		else if (m1.extent.overlap(m2.extent)){
			f.addFV("s1","3");
		}
		else if (m1.extent.start()<m2.extent.start())
			f.addFV("s1","4");
		else
			f.addFV("s1","5");
		
		if (m1.head.start()<m2.head.start())
			f.addFV("s3","1");
		else
			f.addFV("s3","2");
		
		int structure = checkPosStructure(m1,m2,allEntityMentionsList);
		f.addFV("s2",Integer.toString(structure));
		int pos1= findPos(allEntityMentionsList,m1);
		int pos2= findPos(allEntityMentionsList,m2);
		int distance = Math.abs(pos2-pos1);
		f.addFV("dist",Integer.toString(distance));
		
		/**************syntax features*******************/
		//dependency features
		
		//System.out.println(head1+":"+head2);
		
	
		/*String s = doc.text(sentence);
		String path = StanfordParser.getPath(s,depResult, head1, head2);
		if (depResult.isEmpty()||path.isEmpty())
		{
			f = new FeatureSet();
			return f;
		}
		String dep1 = StanfordParser.getDepWord1();
		String dep2 = StanfordParser.getDepWord2();
		String rel1 = StanfordParser.getDepRelation1();
		String rel2 = StanfordParser.getDepRelation2();
		//System.out.println(path);
		f.addFV("deppath",path);
		if (!dep1.isEmpty())
			f.addFV("d1",dep1);
		if (!dep2.isEmpty())
			f.addFV("d2",dep2);
		if (!rel1.isEmpty())
			f.addFV("r1",rel1);
		if (!rel2.isEmpty())
			f.addFV("r2",rel2);
	
		//parse tree features
		
        PennTreeReader reader = new PennTreeReader(new StringReader(parseResult));
        Tree<String> tree = reader.getRoot();
        ArrayList<Integer> list1 = StanfordParser.findPos(head1);
        ArrayList<Integer> list2 = StanfordParser.findPos(head2);
        int start1=list1.get(0)-1;
        int end1=list1.get(1)-1;
        int start2=list2.get(0)-1;
        int end2=list2.get(1)-1;
        List<Tree<String>> leafTrees = tree.getTerminals();
        Tree<String> t1 = leafTrees.get(start1);
        Tree<String> t2 = leafTrees.get(end1);
        Tree<String> g1 = tree.findGrandTree(t1,t2);
        
        String postag1 = "";
        for (int j=start1;j<=end1;j++){
        	Tree<String> t = leafTrees.get(j);
        	postag1+=t.getParent().getLabel();
        }
        String postag2 = "";
        for (int j=start1;j<=end1;j++){
        	Tree<String> t = leafTrees.get(j);
        	postag2+=t.getParent().getLabel();
        }
        
        if (!postag1.isEmpty()){
        	f.addFV("p1",postag1);
        }
        if (!postag2.isEmpty()){
        	f.addFV("p2",postag2);
        }
        
        int startpos,midpos1,midpos2,endpos;
        if (m1.head.start()<m2.head.start()){
        	startpos = start1-1;
        	midpos1=end1+1;
        	midpos2=start2-1;
        	endpos=end2+1;
        }
        else{
        	startpos = start2-1;
        	midpos1=end2+1;
        	midpos2=start1-1;
        	endpos=end1+1;
        }
        for (int j=0;j<=1&&startpos-j>=0;j++){
        	Tree<String> t = leafTrees.get(startpos-j);
        	f.addFV("bp"+j,t.getParent().getLabel());
        }
        for (int j=0;j<=midpos2-midpos1;j++){
        	Tree<String> t = leafTrees.get(midpos1+j);
        	f.addFV("mp"+j,t.getParent().getLabel());
        }
        for (int j=0;j<=1&&endpos+j<leafTrees.size();j++){
        	Tree<String> t = leafTrees.get(j);
        	f.addFV("ap"+j,t.getParent().getLabel());
        }
        if (g1.isLeaf()){
        	f.addFV("syn1",g1.getParent().getLabel());
        }
        else{
        	f.addFV("syn1",g1.getLabel());
        }
        	
        Tree<String> t3 = leafTrees.get(start2);
        Tree<String> t4 = leafTrees.get(end2);
        Tree<String> g2 = tree.findGrandTree(t3,t4);

        if (g2.isLeaf()){
        	f.addFV("syn2",g2.getParent().getLabel());
        }
        else{
        	f.addFV("syn2",g2.getLabel());
        }
        
        Tree<String> g3 = tree.findGrandTree(g1,g2);
        f.addFV("csyn",g3.getLabel());
        
        String minpath = g1.getMinPath(g2);
        f.addFV("synpath",minpath);*/
		return f;
	}
	
	
	
	
	public static void getAllEntityMentions(AceDocument aceDoc,ArrayList<AceEntityMention> allEntityMentionsList){
		for (int i=0; i<aceDoc.entities.size(); i++) {
			AceEntity entity = (AceEntity) aceDoc.entities.get(i);
			
			allEntityMentionsList.addAll(entity.mentions);
		}
	}
	
	public static int checkPosStructure(AceEntityMention m1,AceEntityMention m2,ArrayList<AceEntityMention> allEntityMentionsList){

		int pos1= findPos(allEntityMentionsList,m1);
		int pos2= findPos(allEntityMentionsList,m2);
		
		if (pos1==-1 || pos2==-1)
			return 10;
		if (m1.extent.within(m2.extent)){
			if (checkEmbed(allEntityMentionsList,pos2)==null){
				AceEntityMention embedm1 = checkEmbed(allEntityMentionsList,pos1);
				if (embedm1!=null && embedm1.equals(m2))
					return 1; //nested+non-embedded
				else if (embedm1!=null && !embedm1.equals(m2)){
					//System.out.println(m1.text+":"+m2.text);
					return 2; //nested+one-embedded
				}
			}
			else {
				return 3; //nested+both-embedded
			}	
		}
		else if (m2.extent.within(m1.extent)){
			if (checkEmbed(allEntityMentionsList,pos1)==null){
				AceEntityMention embedm2 = checkEmbed(allEntityMentionsList,pos2);
				if (embedm2!=null && embedm2.equals(m1))
					return 1; //nested+non-embedded
				else if (embedm2!=null && !embedm2.equals(m1)){
					return 2; //nested+one-embedded
				}
			}
			else {
				return 3; //nested+both-embedded
			}	
		}
		else if (checkEmbed(allEntityMentionsList,pos1)==null&&checkEmbed(allEntityMentionsList,pos2)==null&&Math.abs(pos2-pos1)==1){
			return 4; // adjacent + none-embedded
		}
		else if (checkEmbed(allEntityMentionsList,pos1)==null&&checkEmbed(allEntityMentionsList,pos2)==null&&Math.abs(pos2-pos1)>1){
			return 7; // separated + none-embedded
		}
		else if (checkEmbed(allEntityMentionsList,pos1)!=null && checkEmbed(allEntityMentionsList,pos2)==null ){
			int pos;
			AceEntityMention m = null;
			if (pos1<pos2){
				m=getOutlier(allEntityMentionsList,pos1);
				if (m!=null)
					m=findNext(allEntityMentionsList,m);
			}
			else
				m=allEntityMentionsList.get(pos2+1);
			if (pos1<pos2){
				if (m.equals(m2))
					return 5; // adjacent+one-embedded
				else
					return 8; //separated+one-embedded
			}
			else {
				if (m1.extent.within(m.extent))
					return 5; // adjacent+one-embedded
				else
					return 8; //separated+one-embedded
			}
		}
		else if (checkEmbed(allEntityMentionsList,pos1)==null && checkEmbed(allEntityMentionsList,pos2)!=null ){
			int pos;
			AceEntityMention m;
			if (pos1<pos2){
				m=allEntityMentionsList.get(pos1+1);
			}
			else{
				m=getOutlier(allEntityMentionsList,pos2);
				if (m!=null)
					m=findNext(allEntityMentionsList,m);
			}
			if (pos1<pos2){
				if (m2.extent.within(m.extent)){
					return 5;// adjacent+one-embedded
				}
				else
					return 8;//separated+one-embedded
			}
			else{
				if (m.equals(m1)){
					return 5;// adjacent+one-embedded
				}
				else
					return 8;//separated+one-embedded
			}	
		}
		else if (checkEmbed(allEntityMentionsList,pos1)!=null && checkEmbed(allEntityMentionsList,pos2)!=null ){
			int pos;
			AceEntityMention outlier1;
			AceEntityMention outlier2;
			outlier1=getOutlier(allEntityMentionsList,pos1);
			outlier2=getOutlier(allEntityMentionsList,pos2);
			
			if (pos1<pos2){
				outlier1=findNext(allEntityMentionsList,outlier1);
				if (outlier1!=null&&outlier1.equals(outlier2)){
					return 6;// adjacent+both-embedded
				}
				else
					return 9;//separated+both-embedded
			}
			else{
				outlier2=findNext(allEntityMentionsList,outlier2);
				if (outlier2!=null&&outlier2.equals(outlier1)){
					return 6;// adjacent+both-embedded
				}
				else
					return 9;//separated+both-embedded
			}
		}
		return 0;
	}
	
	public static AceEntityMention checkEmbed(ArrayList<AceEntityMention> allEntityMentionsList,int pos){
		AceEntityMention m1= allEntityMentionsList.get(pos);
		while (true){
			AceEntityMention m2;
			if (pos==0)
				return null;
			else {
				while (pos-1>=0){
					m2 = allEntityMentionsList.get(pos-1);
					pos= pos-1;
					if (m1.extent.within(m2.extent))
						return m2;
				}
				
				return null;
			}
			
		}
	}
	
	public static AceEntityMention findNext(ArrayList<AceEntityMention> allEntityMentionsList,AceEntityMention m1){
		for (int i=0;i<allEntityMentionsList.size();i++){
			AceEntityMention m=  allEntityMentionsList.get(i);
			if (m.extent.start()>m1.extent.end())
				return m;
		}
		return null;
	}
	
	public static AceEntityMention getOutlier(ArrayList<AceEntityMention> allEntityMentionsList,int pos){
		AceEntityMention m1= allEntityMentionsList.get(pos);
		
		for (int i=0;i<pos;i++){
			AceEntityMention m=  allEntityMentionsList.get(i);
			if (m1.extent.within(m.extent)){
				return m;
			}
		}
		return null;
		
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
	
	
	/*
	 * get splitCount files for fileList2
	 */
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
				trainWriter.println(currentDocPath);//testWriter.println(currentDocPath);
			}
			else
			{
				testWriter.println(currentDocPath);
			}
			
				
			pos++;
		}
		reader.close();
	}
	
	static void splitCorpus(int fold, ArrayList v) throws IOException {
		PrintStream trainWriter = new PrintStream(new FileOutputStream(
				trainfileList));
		PrintStream testWriter = new PrintStream(new FileOutputStream(
				testfileList));
		for (int i = 0; i < v.size(); i++) {
			String str = (String) v.get(i);
			
			if (i >= fold * (v.size()/10) && i < (fold + 1) * (v.size()/10))
				testWriter.println(str);
			else
				trainWriter.println(str);
		}
		trainWriter.close();
		testWriter.close();
	}
	
	public static void main(String[] args) throws Exception {
		/*RelationTagger2 tagger = new RelationTagger2();
		tagger.transfer(fileList);*/
		//selectFeaturefile(aceModels+"relationIdenFeatureFile.log");
		StanfordParser.initialize();
		String  propertyFile= home + "../../props/tfidf.properties";
		// initialize Jet
		JetTest.initializeFromConfig (propertyFile);
		
		Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
		
		ArrayList<String> v = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			v.add(currentDocPath);
		}
		reader.close();
		
		RelationTagger5 tagger = new RelationTagger5();
		
		for (int runs = 0; runs < 1; runs++) {
			System.out.println("runs= " + Integer.toString(runs+1));
			Collections.shuffle(v);
			for (int fold = 0; fold < 1; fold++) {
				//splitCorpus(fold,v);
				
				System.out.println("folds= " + Integer.toString(fold+1));
				tagger.trainRelationModel();
				
				tagger.loadModels();
	
				tagger.Tag(testfileList,tagDir);
				RelationEval2 re = new RelationEval2();
				re.evaluate(testfileList, tagDir);
			}
		}
		/*fw.write(re.RIP + "\t" + re.RIR + "\t" + re.RIF + "\t"
					+ re.RCP + "\t" + re.RCR + "\t" + re.RCF + "\n");
		}
		fw.close();*/
		/*BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logFileName), "UTF-8"));
		fw.flush();
		fw.write(re.log.toString());
		fw.close();*/
		
	}
	
	
	private void trainRelationModel() throws IOException {
		/*relationIdenWriter = new PrintStream(new FileOutputStream(
				relationIdenFeatureFileName));
		
		relationFeatureWriter= new PrintStream(new FileOutputStream(
						relationFeatureFileName));
		
		train(trainfileList);
		
		relationIdenWriter.close();
		relationFeatureWriter.close();*/
		
		buildClassifierModel(relationIdenFeatureFileName,relationIdenModelFileName);
		buildClassifierModel(relationFeatureFileName,relationModelFileName);
		
	}
	
	public void loadModels(){
		relationIdenModel = loadClassifierModel(relationIdenModelFileName);
		relationModel=loadClassifierModel(relationModelFileName);
	}
	
	//static String home1 = "C:/Users/blender/GALE/CH/";
	
	public void Tag(String fileList, String output) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			try{
				docCount++;
				System.out.println("Tag file " + docCount+":"+currentDocPath);
				String textFile = home + "source/" + currentDocPath;
				String xmlFile = home+"source/"+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
				String outputFile = output
				+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
				
				ExternalDocument doc = new ExternalDocument("sgml", textFile);
				
				doc.setAllTags(true);
				doc.open();
				doc.stretchAll();
				Resolve.ACE = true;
				Ace.monocase = Ace.allLowerCase(doc);
				System.out.println (">>> Monocase is " + Ace.monocase);
				Control.processDocument (doc, null, docCount < 0, docCount);
				
				AceDocument aceDoc = new AceDocument(textFile, xmlFile);
				aceDoc.relations.clear();
				
				Tag(doc,aceDoc, currentDocPath);
				
				aceDoc.write(new BufferedWriter(new FileWriter(outputFile)), doc);
			}
			catch(Exception e) {
		      e.printStackTrace();
		    }
		}
		reader.close();
	}
	
	static double MIN_RELATION_IDEN_THRESHOLD = 0.2;
	static double MIN_RELATION_THRESHOLD = 0;
	
	static HashMap subtypeTable = new HashMap();
	static Vector categoryTable = new Vector();
	static {
		categoryTable.add("Citizen-Resident-Religion-Ethnicity");
		categoryTable.add("Student-Alum");
		categoryTable.add("Membership");
		categoryTable.add("Family");
		categoryTable.add("Subsidiary");
		categoryTable.add("Founder");
		categoryTable.add("Investor-Shareholder");
	}

	static {
		subtypeTable.put("User-Owner-Inventor-Manufacturer",  "ART");
		subtypeTable.put("Citizen-Resident-Religion-Ethnicity", "GEN-AFF");
		subtypeTable.put("Org-Location", "GEN-AFF");
		subtypeTable.put("Employment", "ORG-AFF");
		subtypeTable.put("Founder", "ORG-AFF");
		subtypeTable.put("Investor-Shareholder", "ORG-AFF");
		subtypeTable.put("Membership", "ORG-AFF");
		subtypeTable.put("Ownership", "ORG-AFF");
		subtypeTable.put("Sports-Affiliation", "ORG-AFF");
		subtypeTable.put("Student-Alum", "ORG-AFF");
		subtypeTable.put("Artifact", "PART-WHOLE");
		subtypeTable.put("Geographical", "PART-WHOLE");
		subtypeTable.put("Subsidiary", "PART-WHOLE");
		subtypeTable.put("Business", "PER-SOC");
		subtypeTable.put("Family", "PER-SOC");
		subtypeTable.put("Lasting-Personal", "PER-SOC");
		subtypeTable.put("Located", "PHYS");
		subtypeTable.put("Near", "PHYS");
	}
	
	static HashMap relationTable = new HashMap();

	static {
		Vector v =new Vector();
		v.add("Citizen-Resident-Religion-Ethnicity");
		v.add("Business");
		v.add("Family");
		v.add("Lasting-Personal");
		
		v.add( "Employment" );
		v.add( "Membership");
		v.add( "Ownership");
		relationTable.put("PER:PER",v);
		
		
		v =new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		v.add("Citizen-Resident-Religion-Ethnicity");
		v.add("Employment");
		v.add("Founder");
		v.add("Investor-Shareholder");
		v.add("Sports-Affiliation");
		v.add("Student-Alum");
		v.add("Business");
		v.add("Ownership");
		
		v.add( "Subsidiary");
		v.add( "Membership");
		v.add( "Located");
		relationTable.put("PER:ORG",v);
		
		v=new Vector();
		v.add( "Citizen-Resident-Religion-Ethnicity");
		v.add( "Employment");
		v.add( "Located");
		v.add("Near");
		
		v.add("Membership");
		v.add( "Org-Location");
		relationTable.put("PER:GPE",v);
		
		v =new Vector();
		v.add( "Citizen-Resident-Religion-Ethnicity");
		v.add( "Located");
		v.add( "Near");
		
		v.add( "Org-Location");
		relationTable.put("PER:LOC",v);
		
		v =new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		v.add("Located");
		v.add("Near");
		v.add("Employment");
		v.add("Membership");
		relationTable.put("PER:FAC",v);
		
		v= new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		relationTable.put("PER:WEA",v);
		
		v= new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		v.add("Located");
		relationTable.put("PER:VEH", v);
		
		v=new Vector();
		v.add("Investor-Shareholder");
		v.add("Membership");
		v.add("Subsidiary");
		
		v.add( "User-Owner-Inventor-Manufacturer");
		v.add( "Employment");
		v.add( "Founder");
		v.add( "Geographical");
		relationTable.put("ORG:ORG", v);
		
		v=new Vector();
		v.add("Org-Location");
		v.add("Investor-Shareholder");
		v.add("Subsidiary");
		v.add( "Citizen-Resident-Religion-Ethnicity");
		v.add( "Employment");
		v.add( "Geographical");
		v.add( "Located");
		v.add( "Near");
		relationTable.put("ORG:GPE", v);
		
		v=new Vector();
		v.add("Org-Location");
		v.add( "Subsidiary");
		relationTable.put("ORG:LOC", v);
		
		v=new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		v.add("Located");
		relationTable.put("ORG:FAC", v);
		
		v=new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		relationTable.put("ORG:WEA", v);
		
		v=new Vector();
		v.add("User-Owner-Inventor-Manufacturer");
		relationTable.put("ORG:VEH", v);
		
		v=new Vector();
		v.add("Investor-Shareholder");
		v.add( "Membership");
		relationTable.put("GPE:ORG", v);
		
		v=new Vector();
		v.add("Geographical");
		v.add("Near");
		v.add("Citizen-Resident-Religion-Ethnicity");
		v.add("Subsidiary");
		relationTable.put("GPE:GPE", v);
		
		v=new Vector();
		v.add( "Geographical");
		v.add( "Near");
		relationTable.put("GPE:LOC", v);
		
		v=new Vector();
		v.add( "User-Owner-Inventor-Manufacturer");
		v.add( "Geographical");
		v.add( "Near");
		
		relationTable.put("GPE:FAC", v);
		
		v=new Vector();
		v.add( "User-Owner-Inventor-Manufacturer");
		relationTable.put("GPE:WEA", v);
		
		v=new Vector();
		v.add( "User-Owner-Inventor-Manufacturer");
		relationTable.put("GPE:VEH", v);
		
		v=new Vector();
		v.add("Geographical");
		v.add("Near");
		relationTable.put("LOC:GPE", v);
		
		v=new Vector();
		v.add( "Geographical");
		v.add( "Near");
		relationTable.put("LOC:LOC", v);
		
		v=new Vector();
		v.add( "Geographical");
		v.add( "Near");
		relationTable.put("LOC:FAC", v);
		
		
		v=new Vector();
		v.add( "Geographical");
		v.add( "Near");
		v.add( "Org-Location");
		v.add( "Subsidiary");
		relationTable.put("FAC:GPE", v);
		
		v=new Vector();
		v.add( "Geographical");
		v.add( "Near");
		v.add("Org-Location");
		relationTable.put("FAC:LOC", v);
		
		
		v=new Vector();
		v.add( "Subsidiary");
		relationTable.put("FAC:ORG", v);
		
		v=new Vector();
		v.add( "Geographical");
		v.add( "Near");
		relationTable.put("FAC:FAC", v);
		
		v=new Vector();
		v.add( "Artifact");
		relationTable.put("WEA:WEA", v);
		
		v=new Vector();
		v.add( "Artifact");
		relationTable.put("WEA:VEH", v);
		
		v=new Vector();
		v.add( "Artifact");
		relationTable.put("VEH:VEH", v);
		/*relationTable.put("FAC:GPE", "Org-Location");
		relationTable.put("FAC:GPE", "Subsidiary");
		relationTable.put("FAC:LOC", "Org-Location");
		relationTable.put("LOC:VEH", "User-Owner-Inventor-Manufacturer");
		relationTable.put("GPE:FAC", "Geographical");
		relationTable.put("GPE:FAC", "Located");
		relationTable.put("GPE:FAC", "Near");
		relationTable.put("GPE:LOC", "Located");
		relationTable.put("GPE:GPE", "Citizen-Resident-Religion-Ethnicity");
		relationTable.put("GPE:GPE", "Org-Location");
		relationTable.put("GPE:GPE", "Subsidiary");
		relationTable.put("GPE:ORG", "User-Owner-Inventor-Manufacturer");
		relationTable.put("GPE:ORG", "Employment");
		relationTable.put("ORG:FAC", "Org-Location");
		relationTable.put("ORG:FAC", "Located");
		relationTable.put("ORG:FAC", "Near");
		relationTable.put("ORG:LOC", "Citizen-Resident-Religion-Ethnicity");
		relationTable.put("ORG:LOC", "Subsidiary");
		relationTable.put("ORG:LOC", "Located");
		relationTable.put("ORG:GPE", "Citizen-Resident-Religion-Ethnicity");
		relationTable.put("ORG:GPE", "Employment");
		relationTable.put("ORG:GPE", "Geographical");
		relationTable.put("ORG:GPE", "Located");
		relationTable.put("ORG:ORG", "User-Owner-Inventor-Manufacturer");
		relationTable.put("ORG:ORG", "Employment");
		relationTable.put("ORG:ORG", "Founder");
		relationTable.put("PER:VEH", "Located");
		relationTable.put("PER:LOC", "Employment");
		relationTable.put("PER:GPE", "Membership");
		relationTable.put("PER:GPE", "Org-Location");
		relationTable.put("PER:ORG", "Subsidiary");
		relationTable.put("PER:ORG", "Membership");
		relationTable.put("PER:ORG", "Located");
		relationTable.put("PER:ORG", "Near");
		relationTable.put("PER:PER", "Employment" );
		relationTable.put("PER:PER", "Membership");
		relationTable.put("PER:PER", "Located");*/
	}
	
	
	public void Tag(Document doc, AceDocument aceDoc, String currentDocPath) throws IOException{
		int aceRelationNo = 1;

		ArrayList<AceEntityMention> allEntityMentionsList = new ArrayList();
		getAllEntityMentions(aceDoc,allEntityMentionsList);
		AceEntityCompare comp = new AceEntityCompare();
		Collections.sort(allEntityMentionsList, comp);
		Vector<Annotation> sentences = doc.annotationsOfType("sentence");
		
		String [] dep = new String[sentences.size()];
		String [] parse = new String[sentences.size()];
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(parseDir+currentDocPath.replaceAll(".sgm",".parse")));
		while (( line= reader.readLine()) != null) {
			if (line.indexOf('\t')!=line.length()-1){
				String[] arr = line.split("\t");
				int id = Integer.parseInt(arr[0]);
				dep[id] = arr[1];
			}
			line= reader.readLine();
			if (line.indexOf('\t')!=line.length()-1){
				String[]arr = line.split("\t");
				int id = Integer.parseInt(arr[0]);
				parse[id] = arr[1];
			}
		}
		reader.close();
		
		for (int i=0; i<sentences.size();i++){
			Annotation ann =  sentences.get(i);
			Vector<AceEntityMention> ms = findEntityInSentence(doc,aceDoc,ann);
			if (ms.size()<=1)
				continue;
			String s = doc.text(ann);
			s=s.replace("\n", " ");
			s=s.replaceAll("\\b\\s{2,}\\b", " ");
			
			if (s.length()>500)
				continue;
			
			System.out.println("Sentence "+(i+1)+"/"+sentences.size());
			
			String depResult = dep[i];
			String parseResult = parse[i];
			for (int j = 0; j<ms.size()-1;j++){
				AceEntityMention m1 = ms.get(j);
				for (int k=j+1;k<ms.size();k++){
					AceEntityMention m2 = ms.get(k);
					FeatureSet f1 = relationFeatures(depResult,parseResult,doc,aceDoc,ann.span(),m1,m2,allEntityMentionsList);
					if (f1.size()==0)
						continue;
					FeatureSet f2 = relationFeatures(depResult,parseResult,doc,aceDoc,ann.span(),m2,m1,allEntityMentionsList);
					
					double relationProb1 = relationIdenModel.eval(f1.toArray())[relationIdenModel.getIndex("yes")];
					double relationProb2 = relationIdenModel.eval(f2.toArray())[relationIdenModel.getIndex("yes")];
					
					double relationProb;
					int structure;
					FeatureSet f;
					AceEntityMention arg1;
					AceEntityMention arg2;
					if (relationProb1>relationProb2){
						relationProb = relationProb1;
						f= f1;
						arg1=m1;
						arg2=m2;
					}
					else{
						relationProb = relationProb2;
						f=f2;
						arg1=m2;
						arg2=m1;
					}
					
					if (relationProb>=MIN_RELATION_IDEN_THRESHOLD){
						String rsubtype = relationModel.getBestOutcome(relationModel.eval(f.toArray())).intern();
						double rsubtypeProb = relationModel.eval(f.toArray())[relationModel.getIndex(rsubtype)];
						if (rsubtypeProb>=MIN_RELATION_THRESHOLD){
							/*************subtype check****************/
							String typesubtype = arg1.entity.type+":"+ arg2.entity.type;
							if (relationTable.containsKey(typesubtype)){
								Vector relations = (Vector)relationTable.get(typesubtype);
								if (!relations.contains(rsubtype))
									continue;
							}
							else
								continue;
							String rId = currentDocPath.replaceFirst(".sgm", "")+ "-R" + aceRelationNo;
						
							AceRelation r = new AceRelation (rId, (String)subtypeTable.get(rsubtype), rsubtype, "EXPLICIT",arg1.entity, arg2.entity);
							String rmId = rId+"-1";
							AceRelationMention rm =new AceRelationMention (rmId, arg1, arg2, doc);
							rm.confidence = rsubtypeProb;
							r.addMention(rm);
							aceDoc.addRelation(r);
							aceRelationNo++;
						}
					}
				}
			}
		}
		
		rpostProcessing(allEntityMentionsList,aceDoc,doc);
		System.err.println (aceDoc.relations.size() + " relations");
		
	}
	
	static double FIX_THRESHOLD = 0.8;
	private void rpostProcessing(ArrayList<AceEntityMention> allEntityMentionsList,AceDocument aceDoc,Document doc){
		for (int i=0;i<allEntityMentionsList.size()-2;i++){
			AceEntityMention m= allEntityMentionsList.get(i);
			AceEntityMention m1= allEntityMentionsList.get(i+1);
			AceEntityMention m2= allEntityMentionsList.get(i+2);
			if (m1.extent.within(m.extent)&&m2.extent.within(m.extent)&&m1.entity.type.equals(m2.entity.type)&&!m1.extent.overlap(m2.extent)){
				AceRelation r1=findRelation(aceDoc,m,m1);
				AceRelation r2=findRelation(aceDoc,m,m2);
				if (r1==null &&r2==null)
					continue;
				if (r1!=null&&r2!=null){
					if (r1.subtype.equals(r2.subtype))
						continue;
					AceRelationMention rm1=(AceRelationMention)r1.mentions.get(0);
					AceRelationMention rm2=(AceRelationMention)r2.mentions.get(0);
					if (rm1.confidence>rm2.confidence){
						r2.subtype=r1.subtype;
						rm2.confidence=rm1.confidence;
					}
					else{
						r1.subtype=r2.subtype;
						rm1.confidence=rm2.confidence;
					}
				}
				
			}
		}
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