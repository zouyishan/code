package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.ExternalDocument;
import cuny.blender.englishie.nlp.tipster.Span;




public class RelationStatistics {
	static String encoding = "UTF-8";
	static String home ="C:/Users/blender/workspace/blender/corpus/ACE05/";
	static String fileList = home+"totalfilelist";
	static String dataDir = home+"source/";
	
	static String outputfile = home+"rs";
	

	
	public static void main(String[] args) throws Exception {
		RelationStatistics rs = new RelationStatistics();
		rs.statistics6();
	}
	
	public static void statistics4 () throws Exception{
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int count1 = 0; // Number of documents containing no relations 
		int count2 = 0; //Number of documents containing at least one relation 
		String currentDoc;
		
		int count3 = 0; //number of relations
		int count4 = 0; //number of relation mentions
		int count5 = 0; //number of relations containing more than one relation mention
		
		int count6 = 0; //number of relation mentions in which arg1 is before arg2
		int count7 = 0; //number of relation mentions in which arg1 is after arg2
		int count8 = 0; //number of relation mentions in which arg1 overlaps arg2
		int count9 = 0; //number of relation mentions in which arg1 has the same start with arg2
		int count10 =0;
		int count11 =0;
		TreeMap relationMap = new TreeMap();
		TreeMap argTable = new TreeMap();
		
		String type[] = {"PER","ORG","GPE","LOC","FAC","WEA","VEH"};

		StringBuffer buf = new StringBuffer();
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			//start analyzing each event and event mention
			ArrayList relations = aceDoc.relations;
			if (relations.size()==0)
			{
				count1++;
				continue;
			}
			count2++;
			count3+=relations.size();
			for (int i = 0; i < relations.size(); i++) {
				
				AceRelation relation = (AceRelation) relations.get(i);
				//count relation mentions
				count4+=relation.mentions.size();
				//count relations with more than one mentions
				if (relation.mentions.size()>1){
					count5++;
				}
				for (int j=0;j<relation.mentions.size();j++){
					AceRelationMention rm = (AceRelationMention) relation.mentions.get(j);
					if (rm.arg1.extent.end()<rm.arg2.extent.start()){
						count6++;
					}
					if (rm.arg1.extent.start()>rm.arg2.extent.end()){
						count7++;
					}
					if (rm.arg1.extent.overlap(rm.arg2.extent)){
						count8++;
					}
					if (rm.arg1.extent.overlap(rm.arg2.extent)&&(rm.arg1.extent.start()>rm.arg2.extent.start()||
							(rm.arg1.extent.start()==rm.arg2.extent.start()&&rm.arg1.extent.end()<rm.arg2.extent.end()))){
						count9++;
					}
					if (rm.arg1.extent.within(rm.arg2.extent)){
						count10++;
					}
					if (rm.arg2.extent.within(rm.arg1.extent)){
						count11++;
					}
					//construct type subtype map
					if (!argTable.containsKey(rm.arg1.entity.type+":"+rm.arg2.entity.type)){
						argTable.put(rm.arg1.entity.type+":"+rm.arg2.entity.type, new TreeMap());
					}
					TreeMap typeMap =(TreeMap) argTable.get(rm.arg1.entity.type+":"+rm.arg2.entity.type);
					if (!typeMap.containsKey(relation.type+"."+relation.subtype)){
						typeMap.put(relation.type+"."+relation.subtype, new Integer(0));
					}
					Integer typeCount= (Integer)typeMap.get(relation.type+"."+relation.subtype);
					typeMap.put(relation.type+"."+relation.subtype,  new Integer(typeCount.intValue()+1));
				}
				if (!relationMap.containsKey(relation.type+"."+relation.subtype))
					relationMap.put(relation.type+"."+relation.subtype,  new Integer(0));
				Integer countI = (Integer)relationMap.get(relation.type+"."+relation.subtype);
				relationMap.put(relation.type+"."+relation.subtype,  new Integer(countI.intValue()+relation.mentions.size()));
			}
		}
		System.out.print("\t");
		for (int i=0;i<7;i++){
			System.out.print(type[i]+"\t\t");
		}
		System.out.print("\n");
		
		for (int i=0;i<7;i++){
			//System.out.print(type[i]+"\t");
			for (int j=0;j<7;j++){
				String typeSubtype=type[i]+":"+type[j];
				if (argTable.containsKey(typeSubtype)){
					TreeMap typeMap =(TreeMap) argTable.get(typeSubtype);
					//System.out.print(typeSubtype+":\t"+typeMap.toString()+"\n");
					System.out.print(typeSubtype+"\n");
					Iterator it1 = typeMap.keySet().iterator();
					while (it1.hasNext()) {
						String subtype = (String) it1.next();
						System.out.print(subtype+"\n");
						
					}
					System.out.print("\n");
				}
				
			}
			
				
		}
		
		System.out.println("count1="+count1);
		System.out.println("count2="+count2);
		System.out.println("count3="+count3);
		System.out.println("count4="+count4);
		System.out.println("count5="+count5);
		System.out.println("count6="+count6);
		System.out.println("count7="+count7);
		System.out.println("count8="+count8);
		System.out.println("count9="+count9);
		System.out.println("count10="+count10);
		System.out.println("count11="+count11);
		System.out.println(argTable.toString());		
		System.out.println(relationMap.toString());		
	}
	
	public static void statistics2 () throws Exception{
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int rCount = 0;
		String currentDoc;
		int eCount =0;
		
		TreeMap relationMap = new TreeMap();
		
		StringBuffer buf = new StringBuffer();
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			//start analyzing each event and event mention
			ArrayList relations = aceDoc.relations;
			if (relations.size()==0)
			{
				continue;
			}
			eCount++;
			for (int i = 0; i < relations.size(); i++) {
				rCount++;
				AceRelation relation = (AceRelation) relations.get(i);
				
				if (!relationMap.containsKey(relation.subtype))
					relationMap.put(relation.subtype,  new Integer(0));
				Integer countI = (Integer)relationMap.get(relation.subtype);
				relationMap.put(relation.subtype,  new Integer(countI.intValue()+1));
			}
		}
		
		Iterator it1 = relationMap.keySet().iterator();
		int count=0;
		while (it1.hasNext()) {
			String subtype = (String) it1.next();
			Integer c = (Integer)relationMap.get(subtype);
			count+=c;
			
		}
		System.out.println(eCount);
		System.out.println(count);
		System.out.println(rCount);
		System.out.println(relationMap.toString());		
	}

	public static String logFileName = home+"ACE05Relation2";
	
	public static void statistics6 () throws Exception{
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int rCount = 0;
		String currentDoc;
		int eCount =0;
		
		TreeMap<String, ArrayList<String>> relationMap = new TreeMap<String, ArrayList<String>>();
		
		StringBuffer buf = new StringBuffer();
		
		String  propertyFile= home + "../../props/tfidf.properties";

		// initialize Jet
		JetTest.initializeFromConfig (propertyFile);
		
		Resolve.ACE = true;
		Pat.trace = false;
		Resolve.trace = false;
		
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
			
			Ace.monocase = Ace.allLowerCase(doc);
			System.out.println (">>> Monocase is " + Ace.monocase);
			Control.processDocument (doc, null, docCount < 0, docCount);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			//start analyzing each event and event mention
			ArrayList relations = aceDoc.relations;
			if (relations.size()==0)
			{
				continue;
			}
			eCount++;
			for (int i = 0; i < relations.size(); i++) {
				rCount++;
				AceRelation relation = (AceRelation) relations.get(i);
				if (relation.subtype.isEmpty())
					continue;
				if (!relationMap.containsKey(relation.subtype))
					relationMap.put(relation.subtype,  new ArrayList<String>());
				ArrayList<String> lists=  relationMap.get(relation.subtype);
				for (int j=0;j<relation.mentions.size();j++){
					AceRelationMention rm = (AceRelationMention)relation.mentions.get(j);
					if (rm.arg1.head.overlap(rm.arg2.head)){
						System.out.println(rm.arg1.headText+"//"+rm.arg1.headText);
						continue;
					}
					int start = rm.arg1.head.end()<rm.arg2.head.start()? rm.arg1.head.end():rm.arg2.head.end();
					int end = rm.arg1.head.end()<rm.arg2.head.start()? rm.arg2.head.start():rm.arg1.head.start();
					String txt = doc.text(new Span(start+1,end));
					txt = txt.trim().replace("\n", " ");
					if (txt.isEmpty())
						continue;
					/*if (rm.arg1.head.end()<rm.arg2.head.start()){
						lists.add(txt+"||"+rm.arg1.headText.replace("\n", " ")+" + "+txt+" + "+rm.arg2.headText.replace("\n", " "));
					}
					else{
						lists.add(txt+"||"+rm.arg2.headText.replace("\n", " ")+" + "+txt+" + "+rm.arg1.headText.replace("\n", " "));
					}*/
					lists.add(txt);
				}
				
				relationMap.put(relation.subtype,  lists);
			}
		}
		
		BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logFileName), "UTF-8"));
		fw.flush();
		
		
		Iterator it1 = relationMap.keySet().iterator();
		int count=0;
		while (it1.hasNext()) {
			String subtype = (String) it1.next();
			fw.write("=====================\n");
			fw.write(subtype+"\n");
			ArrayList <String> lists = relationMap.get(subtype);
			for (int i = 0; i<lists.size();i++){
				fw.write(lists.get(i)+"\n");
			}
		}
		fw.close();
	}
	
	
	public static void statistics1 () throws Exception{
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int rCount = 0;
		String currentDoc;
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		
		TreeMap relationMap = new TreeMap();
		
		StringBuffer buf = new StringBuffer();
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir +currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			//start analyzing each event and event mention
			ArrayList relations = aceDoc.relations;
			if (relations.size()==0)
			{
				continue;
			}
			
			for (int i = 0; i < relations.size(); i++) {
				rCount++;
				AceRelation relation = (AceRelation) relations.get(i);
				
				if (!relationMap.containsKey(relation.subtype))
					relationMap.put(relation.subtype,  new TreeMap());
				
				TreeMap argumentMap = (TreeMap)relationMap.get(relation.subtype);
				
				String argTypes = relation.arg1.subtype+":"+relation.arg2.subtype;
				if (!argumentMap.containsKey(argTypes))
					argumentMap.put(argTypes, new Integer(0));
				
				Integer countI = (Integer)argumentMap.get(argTypes);
				argumentMap.put(argTypes, new Integer(countI.intValue()+1));
				
			}
		}
		System.out.println(rCount);
		System.out.println(relationMap.toString());
		Iterator it1 = relationMap.keySet().iterator();
		while (it1.hasNext()) {
			String subtype = (String) it1.next();
			TreeMap argumentMap = (TreeMap)relationMap.get(subtype);
			buf.append(subtype+"="+argumentMap.toString()+"\n");
			
		}
		bw.write(buf.toString());
		bw.close();
	}
	
	public static void statistics3 () throws Exception{
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int rCount = 0;
		String currentDoc;
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		
		TreeMap relationMap = new TreeMap();
		
		StringBuffer buf = new StringBuffer();
		
		int [] count = new int[11]; 
		for (int i=0;i<count.length;i++){
			count[i]=0;
		}
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir + currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			ArrayList<AceEntityMention> allEntityMentionsList = new ArrayList();
			getAllEntityMentions(aceDoc,allEntityMentionsList);
			AceEntityCompare comp = new AceEntityCompare();
			Collections.sort(allEntityMentionsList, comp);
			//start analyzing each event and event mention
			ArrayList relations = aceDoc.relations;
			if (relations.size()==0)
			{
				continue;
			}
			
			for (int i = 0; i < relations.size(); i++) {
				
				AceRelation relation = (AceRelation) relations.get(i);
				for (int j = 0; j<relation.mentions.size();j++){
					rCount++;
					AceRelationMention rm = (AceRelationMention)relation.mentions.get(j);
					count[checkPosStructure(rm, allEntityMentionsList)]++;
				}
			}
		} 
		System.out.println("Total:"+rCount);
		for (int i=0;i<count.length;i++){
			System.out.println("structure "+i+":"+count[i]);
		}
	}
	
	public static void getAllEntityMentions(AceDocument aceDoc,ArrayList<AceEntityMention> allEntityMentionsList){
		for (int i=0; i<aceDoc.entities.size(); i++) {
			AceEntity entity = (AceEntity) aceDoc.entities.get(i);
			
			allEntityMentionsList.addAll(entity.mentions);
		}
	}
	
	public static int checkPosStructure(AceRelationMention relationMention,ArrayList<AceEntityMention> allEntityMentionsList){
		AceEntityMention m1= relationMention.arg1;
		AceEntityMention m2= relationMention.arg2;
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
					System.out.println(m1.text+":"+m2.text);
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
	
	public static void statistics5 () throws Exception{
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		int rCount = 0;
		String currentDoc;
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (outputfile), encoding));
		
		TreeMap relationMap = new TreeMap();
		
		StringBuffer buf = new StringBuffer();
		
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			
			System.out.println("Processing document " + docCount + " : " + currentDoc);
			String textFile = dataDir + currentDoc;
			String xmlFile = dataDir + currentDoc.replaceFirst(".sgm", ".apf.xml");
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			ArrayList<AceEntityMention> allEntityMentionsList = new ArrayList();
			getAllEntityMentions(aceDoc,allEntityMentionsList);
			AceEntityCompare comp = new AceEntityCompare();
			Collections.sort(allEntityMentionsList, comp);
			
			for (int i=0;i<allEntityMentionsList.size()-2;i++){
				AceEntityMention m= allEntityMentionsList.get(i);
				AceEntityMention m1= allEntityMentionsList.get(i+1);
				AceEntityMention m2= allEntityMentionsList.get(i+2);
				
				String str = m.text;
				str=str.replace("\n", "");
				str = str.replaceAll("\\b\\s{2,}\\b", "");
				
				String str1 = m1.text;
				str1=str1.replace("\n", "");
				str1 = str1.replaceAll("\\b\\s{2,}\\b", "");
				
				String str2 = m2.text;
				str2=str2.replace("\n", "");
				str2 = str2.replaceAll("\\b\\s{2,}\\b", "");
				String subtype1=findRelation(aceDoc,m,m1);
				String subtype2=findRelation(aceDoc,m,m2);
				if (m1.extent.within(m.extent)&&m2.extent.within(m.extent)&&
						subtype1!=null&&subtype1!=null&&subtype1.equals(subtype2)){
					buf.append(subtype1+":"+str+"//"+str1+"//"+str2+"\n");
				}
			}
		}
		bw.write(buf.toString());
		bw.close();
	}
	

	static String findRelation(AceDocument aceDoc, AceEntityMention m1, AceEntityMention m2){
		ArrayList relations = aceDoc.relations;
	
		if (relations.size()==0)
		{
			return null;
		}
		
		for (int i = 0; i < relations.size(); i++) {
			AceRelation relation = (AceRelation) relations.get(i);
			for (int j = 0; j<relation.mentions.size();j++){
				AceRelationMention rm = (AceRelationMention)relation.mentions.get(j);
				if ((rm.arg1.equals(m1)&&rm.arg2.equals(m2)) ||(rm.arg2.equals(m1)&&rm.arg1.equals(m2)))
					return relation.subtype;
			}
		}
		return null;
	}
}