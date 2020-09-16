package cuny.blender.englishie.nlp.tool;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEntity;
import cuny.blender.englishie.ace.AceEntityMention;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.ace.AceEventMention;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


public class CHEntityCount {
	static String home = "C:/Users/blender/workspace5/TAC/corpus/";
	//static final String home = "/jar/KBP/data/LDC/KBPEntity/";
	//static String encoding = "UTF-8";
	static String fileList = home+"docid_to_file_mapping.tab";
	static String dataDir = home+"../TAC_2010_KBP_Web_Text_from_New_Collection/data/wb/";
	static String apfDir = home+"newweboutput/";
	static String perFileName = home+"per";
	static String orgFileName = home+"org";
	static String gpeFileName = home+"gpe";
	static String txtFileList= home+"1";
	
	public static void main4(String[] args) throws Exception {

		TreeMap<String,Integer> freqMap = new TreeMap<String,Integer>();
		TreeMap<String,HashSet<String>> docMap = new TreeMap<String,HashSet<String>>();
		String line;
		String combinedFile = home+"org_final";
		FileWriter fw = new FileWriter(combinedFile);
		
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int count = 0;
		while ((line = reader.readLine()) != null) {
			
			count ++;
			System.out.println(count);
			String [] str=line.split("\t");
			String head=str[0];
			String freq=str[1]; 
			
			String docs = str[2];
			if (docs.charAt(0)=='"'){
				docs = docs.substring(1, docs.length()-1);
				
			}
			else{
				
				
			}
				
			
			String new_line = head + "\t"+freq+"\t"+docs+"\n";
			fw.append(new_line);
			
		}
		reader.close();
		
		
		fw.close();
		
	}
	
	static void splitCorpus() throws IOException {
		
		ArrayList<String> v = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			v.add(currentDocPath);
		}
		reader.close();
		PrintStream txtWriter;
		int fold = 0;
		txtWriter = new PrintStream(new FileOutputStream(
				txtFileList));
		for (int i = 0; i < v.size(); i++) {
			String str = (String) v.get(i);
			if (i%(v.size()/4)==0){
				fold++;
				txtFileList = home + fold;
				txtWriter.close();
				txtWriter = new PrintStream(new FileOutputStream(
						txtFileList));
			}
			txtWriter.append(str+"\n"); 
		}
		txtWriter.close();
	}
	
	public static void main6(String[] args) throws Exception {
		String home = "C:/Users/blender/Desktop/";//args[0];
		String type = "gpe";//args[1];
		String unfilteredFile = home+type+"_unfiltered";
		String line;
		String combinedFile = home+type+"_filtered";
		BufferedReader reader = new BufferedReader (new FileReader(unfilteredFile));
		FileWriter fw = new FileWriter(combinedFile);
		
		while ((line = reader.readLine()) != null) {
			String [] str=line.split("\t");
			String head=str[0];
			String freq=str[1]; 
			String docs=str[2];
			String []tokens = head.split("\\s+");
			boolean bFound = false;
			for (int i=0;i<tokens.length;i++){
				if (Character.isLowerCase(tokens[i].charAt(0))){
					bFound = true;
					break;
				}
				String token = tokens[i];
				boolean bLetter = true;
				for (int j=0;j<token.length();j++){
					if (!Character.isLetter(token.charAt(j))){
						bLetter = false;
						break;
					}
				}
				if (!bLetter){
					bFound = true;
					break;
				}
			}
			if (bFound){
				continue;
			}
			fw.append(head+"\t"+freq+"\t"+docs+"\n");
		}
		fw.close();
	}
	
	public static void main2(String[] args) throws Exception {
		splitCorpus();
	}
	
	public static void main7(String[] args) throws Exception {
		String home = args[0];
		String type = args[1];
		String start = args[2];
		String end = args[3];
		TreeMap<String,Integer> freqMap = new TreeMap<String,Integer>();
		TreeMap<String,HashSet<String>> docMap = new TreeMap<String,HashSet<String>>();
		String line;
		int line_count = 0;
		for (int i=Integer.parseInt(start);i<=Integer.parseInt(end);i++){
			String fileList = home+type+i;
			System.out.println("Combining file "+type+i);
			BufferedReader reader = new BufferedReader (new FileReader(fileList));
			while ((line = reader.readLine()) != null) {
				line_count++;
				//System.out.println("line "+line_count);
				String [] str=line.split("\t");
				String head=str[0];
				String freq=str[1]; 
				
				String docs = str[2];
				//docs = docs.substring(1, docs.length()-1);
				String [] docArray=docs.split(",");
				List doclist = Arrays.asList(docArray);
				HashSet docSet = new HashSet(doclist);
				
				if (!freqMap.containsKey(head)){
					freqMap.put(head, new Integer(0));
					docMap.put(head, new HashSet<String>());
				}
				
				int count;
				Integer countI = (Integer)freqMap.get(head);
				if (countI == null)
					count = 0;
				else
					count = countI.intValue();
				freqMap.put(head, new Integer(count+Integer.parseInt(freq)));
				HashSet<String> doc= docMap.get(head);
				doc.addAll(docSet);
				docMap.put(head, doc);
			}
			reader.close();
		}
		
		String combinedFile = home+type+start+"-"+end;
		FileWriter fw = new FileWriter(combinedFile);
		Iterator it1 = freqMap.keySet().iterator();
		while (it1.hasNext()) {
			String head = (String) it1.next();
			HashSet doc = docMap.get(head);
			String str="";
			Iterator docIt = doc.iterator();
	        
	        while (docIt.hasNext()) {
				str = str + docIt.next() +",";
			}
			str = str.substring(0,str.length()-1);
			fw.append(head+"\t"+freqMap.get(head)+"\t"+doc.toString()+"\n");
		}
		fw.close();
	}
	
	public static void main(String[] args) throws Exception {
		home = args[0];
		fileList = home+args[1];
		dataDir = home+args[2];
		apfDir = home+args[3];
		perFileName = home+"perFreq";
		orgFileName = home+"orgFreq";
		gpeFileName = home+"gpeFreq";
		
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		String currentDocPathBase;
		
		TreeMap<String,Integer> perFreq = new TreeMap<String,Integer>();
		TreeMap<String,Integer> orgFreq = new TreeMap<String,Integer>();
		TreeMap<String,Integer> gpeFreq = new TreeMap<String,Integer>();
		
		TreeMap<String,Vector<String>> perDoc = new TreeMap<String,Vector<String>>();
		TreeMap<String,Vector<String>> orgDoc = new TreeMap<String,Vector<String>>();
		TreeMap<String,Vector<String>> gpeDoc = new TreeMap<String,Vector<String>>();
		
		long t0 = System.currentTimeMillis();
		
		int count1 = 0;
		int count2 = 0;
		while ((currentDocPath = reader.readLine()) != null) {
			//String [] str=currentDocPath.split("\\s+");
			//currentDocPath = currentDocPath.substring(2);
			currentDocPath = currentDocPath.replace(".apf.xml", ".sgm");
			currentDocPathBase=currentDocPath.replace(".sgm", "");
			//currentDocPath=str[1];
			docCount++;
			if (docCount%1000==0){
				long t1 = System.currentTimeMillis();
				long t= (t1-t0)/1000;
				long hour = t/3600;
				long min = t%3600/60;
				long sec = t%3600%60;
				System.out.println("time passed:"+hour+"hours"+min+"mins"+sec+"sec");
			}
				
			System.out.println("doc " + docCount + " : " + currentDocPathBase);
			String textFile = dataDir + currentDocPath;
			String xmlFile = apfDir +currentDocPathBase+".apf.xml";
			
			/*ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Resolve.ACE = true;
			AceJet.Ace.monocase = AceJet.Ace.allLowerCase(doc);
			Control.processDocument (doc, null, false, 0);*/
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			
			ArrayList<AceEntity> entities = aceDoc.entities;
			
			for (int i=0;i<entities.size();i++)
			{
				AceEntity e1 = (AceEntity)entities.get(i);
				for (int j=0;j<e1.mentions.size();j++){
					AceEntityMention em1 = (AceEntityMention)e1.mentions.get(j);
					if (!em1.type.equals("NAM"))
						continue;
					if (e1.type.equals("PERSON")){
						String head = em1.getHeadText().replace("\n", " ");
						head = head.replace("\\s+", " ");
						head = head.replace("\t", " ");
						/*if (!((head.charAt(0)>='a'&&head.charAt(0)<='z')||(head.charAt(0)>='A'&&head.charAt(0)<='Z')))
							continue;*/
						if (!perFreq.containsKey(head)){
							perFreq.put(head, new Integer(0));
							perDoc.put(head, new Vector<String>());
						}
						
						int count;
						Integer countI = (Integer)perFreq.get(head);
						if (countI == null)
							count = 0;
						else
							count = countI.intValue();
						perFreq.put(head, new Integer(count+1));
						Vector<String> doc= perDoc.get(head);
						if (!doc.contains(currentDocPathBase)){
							doc.add(currentDocPathBase);
							perDoc.put(head, doc);
						}
					}
					if (e1.type.equals("ORGANIZATION")){
						String head = em1.getHeadText().replace("\n", " ");
						head = head.replace("\\s+", " ");
						head = head.replace("\t", " ");
						/*if (!((head.charAt(0)>='a'&&head.charAt(0)<='z')||(head.charAt(0)>='A'&&head.charAt(0)<='Z')))
							continue;*/
						if (!orgFreq.containsKey(head)){
							orgFreq.put(head, new Integer(0));
							orgDoc.put(head, new Vector<String>());
						}
						int count;
						Integer countI = (Integer)orgFreq.get(head);
						if (countI == null)
							count = 0;
						else
							count = countI.intValue();
						orgFreq.put(head, new Integer(count+1));
						Vector<String> doc= orgDoc.get(head);
						if (!doc.contains(currentDocPathBase)){
							doc.add(currentDocPathBase);
							orgDoc.put(head, doc);
						}
						
					}
					if (e1.type.equals("GPE")){
						String head = em1.getHeadText().replace("\n", " ");
						head = head.replace("\\s+", " ");
						head = head.replace("\t", " ");
						/*if (!((head.charAt(0)>='a'&&head.charAt(0)<='z')||(head.charAt(0)>='A'&&head.charAt(0)<='Z')))
							continue;*/
						if (!gpeFreq.containsKey(head)){
							gpeFreq.put(head, new Integer(0));
							gpeDoc.put(head, new Vector<String>());
						}
						
						int count;
						Integer countI = (Integer)gpeFreq.get(head);
						if (countI == null)
							count = 0;
						else
							count = countI.intValue();
						gpeFreq.put(head, new Integer(count+1));
						Vector<String> doc= gpeDoc.get(head);
						if (!doc.contains(currentDocPathBase)){
							doc.add(currentDocPathBase);
							gpeDoc.put(head, doc);
						}
						
					}
						
				}
			}
			
		}
		reader.close();
		
		FileWriter fw = new FileWriter(perFileName);
		Iterator it1 = perFreq.keySet().iterator();
		while (it1.hasNext()) {
			String head = (String) it1.next();
			Vector<String> doc = perDoc.get(head);
			String str="";
			for (int i=0;i<doc.size();i++){
				str = str + doc.get(i) +",";
			}
			str = str.substring(0,str.length()-1);
			fw.append(head+"\t"+perFreq.get(head)+"\t"+str+"\n");
		}
		fw.close();
		
		fw = new FileWriter(orgFileName);
		it1 = orgFreq.keySet().iterator();
		while (it1.hasNext()) {
			String head = (String) it1.next();
			Vector<String> doc = orgDoc.get(head);
			String str="";
			for (int i=0;i<doc.size();i++){
				str = str + doc.get(i) +",";
			}
			str = str.substring(0,str.length()-1);
			fw.append(head+"\t"+orgFreq.get(head)+"\t"+str+"\n");
		}
		fw.close();
		
		fw = new FileWriter(gpeFileName);
		it1 = gpeFreq.keySet().iterator();
		while (it1.hasNext()) {
			String head = (String) it1.next();
			Vector<String> doc = gpeDoc.get(head);
			String str="";
			for (int i=0;i<doc.size();i++){
				str = str + doc.get(i) +",";
			}
			str = str.substring(0,str.length()-1);
			fw.append(head+"\t"+gpeFreq.get(head)+"\t"+str+"\n");
		}
		fw.close();
	}
}