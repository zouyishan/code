package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;
import cuny.blender.englishie.nlp.tipster.ExternalDocument;
import cuny.blender.englishie.nlp.tipster.Span;




/**
 * EventEval evaluates an event tagger using simple metrics of events and
 * arguments found, missing, and spurious.
 */

public class RelationEval2 {
	int correctRels, typeErrorRels, missingRels, spuriousRels, orderErrorRels;

	double RIP,RIR,RIF;
	double RCP,RCR,RCF;

	StringBuffer log = new StringBuffer();
	static String encoding = "UTF-8";
	//static final String home = "C:/Users/blender/GALE/ACE05/";
	static String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	static String keyDir = home+"source/";
	static String testDir = home+"tagged/";
	static String devFileList = home + "devfilelist";
	static String logFileName = home + "log/log.html";
	static String testFileList = home + "testfilelist";
	private static AceEvent baseEvent;

	public static void main(String[] args) throws IOException {
		RelationEval2 re = new RelationEval2();
		re.evaluate(testFileList,testDir);
		String logName=logFileName+"10.html";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logName), "UTF-8"));
		
		bw.write(re.log.toString());
		bw.close();
	}

	
	/**
	 * evaluate the event tagger using the documents list in file
	 * <CODE>fileList</CODE>.
	 */

	public void evaluate(String fileList,String inDir) throws IOException {
		correctRels = 0;
		typeErrorRels = 0;
		missingRels = 0;
		spuriousRels = 0;
		orderErrorRels = 0;

		log = new StringBuffer();
		log.append("<html><body>");
		log.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
		log.append("</head>");
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			String textFile = keyDir + currentDocPath;
			String xmlFile = keyDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String testFile = inDir
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();

			AceDocument keyDoc = new AceDocument(textFile, xmlFile);
			AceDocument testDoc = new AceDocument(textFile, testFile);
			
			evaluateBypair(doc, testDoc, keyDoc);
		}
		reader.close();

		RIP =  (double)(correctRels+orderErrorRels+typeErrorRels)/(orderErrorRels+correctRels+typeErrorRels+spuriousRels);
		RIR = (double)(correctRels+orderErrorRels+typeErrorRels)/(orderErrorRels+correctRels+typeErrorRels+missingRels);
		RIF = 2*RIP*RIR/(RIP+RIR);
	
		RCP = (double)(correctRels+orderErrorRels)/(orderErrorRels+correctRels+typeErrorRels+spuriousRels);
		RCR = (double)(correctRels+orderErrorRels)/(orderErrorRels+correctRels+typeErrorRels+missingRels);
		RCF = 2*RCP*RCR/(RCP+RCR);
		System.out.println(correctRels+"//"+typeErrorRels+"//"+spuriousRels+"//"+missingRels+"//"+orderErrorRels);
		System.out.println("Relation Identification ");
		System.out.println("Precision: "+Double.toString(RIP));
		System.out.println("Recall: "+Double.toString(RIR));
		System.out.println("F Measure: "+Double.toString(RIF));
		System.out.println("Relation Identification + Relation Classification");
		System.out.println("Precision: "+Double.toString(RCP));
		System.out.println("Recall: "+Double.toString(RCR));
		System.out.println("F Measure: "+Double.toString(RCF));
		
		log.append("</body></html>");
	}
	
	public AceRelationMention findRelation(AceRelationMention m,AceDocument aceDoc){
		for (int i=0;i<aceDoc.relations.size();i++){
			AceRelation r = aceDoc.relations.get(i);
			for (int j=0;j<r.mentions.size();j++){
				AceRelationMention rm = (AceRelationMention)r.mentions.get(j);
				if ((rm.arg1.equals(m.arg1)&&rm.arg2.equals(m.arg2))/*||
						(rm.arg1.equals(m.arg2)&&rm.arg2.equals(m.arg1))*/){
					return rm;
				}
			}
		}
		return null;
	}
	
	public AceRelationMention findReverseRelation(AceRelationMention m,AceDocument aceDoc){
		for (int i=0;i<aceDoc.relations.size();i++){
			AceRelation r = aceDoc.relations.get(i);
			for (int j=0;j<r.mentions.size();j++){
				AceRelationMention rm = (AceRelationMention)r.mentions.get(j);
				if (rm.arg1.equals(m.arg2)&&rm.arg2.equals(m.arg1)){
					return rm;
				}
			}
		}
		return null;
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

	
	public void evaluateBypair(Document doc, AceDocument testDoc,
			AceDocument keyDoc) throws IOException {
		
		ArrayList testRels = testDoc.relations;
		ArrayList keyRels = keyDoc.relations;
		
		ArrayList<AceEntityMention> allEntityMentionsList = new ArrayList();
		getAllEntityMentions(testDoc,allEntityMentionsList);
		AceEntityCompare comp = new AceEntityCompare();
		Collections.sort(allEntityMentionsList, comp);
		
		for (int i=0;i<testRels.size();i++){
			AceRelation testR=(AceRelation)testRels.get(i);
			if (!testR.subtype.equals("Investor-Shareholder"))
				continue;
			for (int j=0;j<testR.mentions.size();j++){
				
				AceRelationMention testRm= (AceRelationMention)testR.mentions.get(j);
				AceRelationMention keyRm=findRelation(testRm,keyDoc);
				int start = testRm.arg1.extent.start()<testRm.arg2.extent.start()?testRm.arg1.extent.start():testRm.arg2.extent.start();
				int end = testRm.arg1.extent.end()>testRm.arg2.extent.end()?testRm.arg1.extent.end():testRm.arg2.extent.end();
				
				String str=doc.text(new Span(start,end+1));
				str=str.replace("\n", " ");
				str = str.replaceAll("\\b\\s{2,}\\b", " ");
				
				String str1=testRm.arg1.headText;
				String str2=testRm.arg2.headText;
				str1=str1.replace("\n", " ");
				str1 = str1.replaceAll("\\b\\s{2,}\\b", " ");
				str2=str2.replace("\n", "");
				str2 = str2.replaceAll("\\b\\s{2,}\\b", " ");
				int structure = checkPosStructure(testRm.arg1,testRm.arg2, allEntityMentionsList);
				log.append(testDoc.docID+"//");
				if (keyRm == null) {
					spuriousRels++;
					AceRelationMention keyRRm=findReverseRelation(testRm,keyDoc);
					if (keyRRm!=null){
						orderErrorRels++;
						log.append("Order Error Relation "
								+ Integer.toString(orderErrorRels) + "("+Integer.toString(structure)+")" + "("+testR.subtype+"//"+str+")"+ ":("
								+ str1 + ")-("+str2+")<br>");
					
					}
					else{
						log.append("Spurious Relation "
							+ Integer.toString(spuriousRels) + "("+Integer.toString(structure)+")"+"("+testR.subtype+"//"+str+")"+ ":("
							+ str1 + ")-("+str2+")<br>");
					}
				} 
				else if (testR.subtype.equals(keyRm.relation.subtype)){
					correctRels++;
					log.append("Correct Relation "
							+ Integer.toString(correctRels) + "("+Integer.toString(structure)+")"+"("+testR.subtype+"//"+str+")"+ ":("
							+ str1 + ")-("+str2+")<br>");
				}
				else{
					typeErrorRels++;
					log.append("Type Error Relation "
							+ Integer.toString(typeErrorRels) +"("+Integer.toString(structure)+")"+
							"(W:"+testR.subtype+",T:"+keyRm.relation.subtype+ ")"+"("+testR.subtype+"//"+str+")"+":("
							+ str1 + ")-("+str2+")<br>");
			
				}
					
			}
		}
		
		
		for (int i=0;i<keyRels.size();i++){
			AceRelation keyR=(AceRelation)keyRels.get(i);
			//if (!categoryTable.contains(keyR.subtype))
			if (!keyR.subtype.equals("Investor-Shareholder"))
				continue;
			for (int j=0;j<keyR.mentions.size();j++){
				log.append(testDoc.docID+"//");
				
				AceRelationMention keyRm= (AceRelationMention)keyR.mentions.get(j);
				AceRelationMention testRm=findRelation(keyRm,testDoc);
				
				int start = keyRm.arg1.extent.start()<keyRm.arg2.extent.start()?keyRm.arg1.extent.start():keyRm.arg2.extent.start();
				int end = keyRm.arg1.extent.end()>keyRm.arg2.extent.end()?keyRm.arg1.extent.end():keyRm.arg2.extent.end();
				
				String str=doc.text(new Span(start,end+1));
				str=str.replace("\n", " ");
				str = str.replaceAll("\\b\\s{2,}\\b", " ");
				
				String str1=keyRm.arg1.headText;
				String str2=keyRm.arg2.headText;
				str1=str1.replace("\n", "");
				str1 = str1.replaceAll("\\b\\s{2,}\\b", " ");
				str2=str2.replace("\n", "");
				str2 = str2.replaceAll("\\b\\s{2,}\\b", " ");
				int structure = checkPosStructure(keyRm.arg1,keyRm.arg2, allEntityMentionsList);
				
				
				if (testRm == null) {
					missingRels++;
					log.append("Miss Relation "
							+ Integer.toString(missingRels)  +"("+Integer.toString(structure)+")"+
							"("+keyR.subtype+"//"+str+")"+ ":("
							+ str1 + ")-("+str2+")<br>");
				} 
			}
		}
		
	}
	
	private String findContainingSentence(Document doc, Span span) {
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
		return doc.text(sentence.span());
	}
	
}

