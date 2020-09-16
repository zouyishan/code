//Author:       Zheng Chen
//Date:         Oct. 05, 2008
package cuny.blender.englishie.nlp.tool;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *  procedures for generating ACE output for a Jet document.
 */

public class MakeFileList {
	static String encoding = "UTF-8";
	//static final String home ="C:/Users/Public/cygwin/home/Berkeley/";
	//static final String home = "C:/Users/blender/GALE/ACE05/";
	//static String home = "C:/Users/blender/GALE/ACE05/";
	//static final String home = "C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/SRL/";
	//static String home = "C:/Users/blender/Workspace/GALE/software/Corpus/ACE/";
	//static String home = "C:/Users/blender/workspace5/TAC/corpus/kbpEntity/";
	static String home = "C:/Users/blender/Desktop/";
	static String parserDir = "H:/Project/GALE/ACE05/parser/";
	static String fixparserDir = "H:/Project/GALE/ACE05/fixparser/";
	static String devfileList = home+"testfilelist";
	static String fileList1 = home+"totalfilelist1";
	static String fileList2 = home+"totalfilelist2";
	static String dataDir = home + "sentences/";
	static String outDir = home + "fixed/";
	
	static String totalFileList = home+"totalfilelist";
	static String trainFileList = home+"trainfilelist";
	static String testFileList = home+"testfilelist";
	public static void main(String[] args) throws IOException {
		//String strPath = "C:/Users/Public/cygwin/home/Berkeley/sentences/";
		String strPath = home +"tac09/";
		//ProcessDir(strPath);
		//ComposeRel(strPath);
		//String strPath ="C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/sources/";
		//String strPath1 = home + "timex2norm/";
		//genU8File(home+"testfilelist",home+"apf/",home+"new/");
		//fixSegmorePunc(fileList);
		//fixParsePunc(fileList);
		//fixsentence(fileList);
		//makeList1(strPath1);
		//genData2(home+"coref_bcube_graph",home+"attr1");
		//genData2(home+"error",home+"out");
		//genData2(home+"event_attr1",home+"tense_system");
		/*String strPath2=strPath+"nw/";
		String strPath3=strPath+"wl/";
		String strPath4=strPath+"nw/";
		String strPath5=strPath+"un/";
		String strPath6=strPath+"wl/";*/
		makeList2("/m1/Data_2011/BOLT/p1r1-eng/filelist");
		//findlost();
		//genTrainFileList();
		//makeapfxml(home+"testfilelist2", home+"tac09_sents/",home+"tac09/");
		/*makeList(strPath2,"nw");
		makeList(strPath3,"wl");
		makeList(strPath4,"nw");
		makeList(strPath5,"un");
		makeList(strPath6,"wl");*/
		
		//File d1 = new File(dir1);
		//d1.mkdirs();
		//genU8File(devfileList,"1");
		//genU8File(devfileList,home+"apfxml/",home+"apfxml3/");
		//genSentence(devfileList,home+"boost_en/",home+"boost_en_sgm/");
		//makeapf(devfileList,home+"csgm/",home+"apfxml1/");
		/*String str ="I am good";
		int pos = str.indexOf("good");
		System.out.println(str.substring(pos+4));*/
		//System.out.println(Integer.toHexString('!'));
		
		/*ArrayList<String> v1 = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileList1));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			v1.add(currentDocPath);
		}
		reader.close();
		
		ArrayList<String> v2 = new ArrayList<String>();
		reader = new BufferedReader(new FileReader(fileList2));
		while ((currentDocPath = reader.readLine()) != null) {
			v2.add(currentDocPath);
		}
		reader.close();
		v2.retainAll(v1);
		PrintStream w = new PrintStream (new FileOutputStream (home+"totalfilelist"));
		for (int i = 0; i < v2.size(); i++) {
			String str = (String) v2.get(i);
			w.println(str);
		}
		w.close();*/
	}
	public static void makeList2(String filelist) throws IOException {
		PrintStream w = new PrintStream (new FileOutputStream ("/m1/Data_2011/BOLT/p1r1-eng/mapfile"));
		
		
		BufferedReader reader = new BufferedReader(new FileReader(filelist));
		String currentDocPath;
		int i=0;
		while ((currentDocPath = reader.readLine()) != null) {
			if (!currentDocPath.endsWith(".sgm"))
				continue;
			int pos = currentDocPath.lastIndexOf('/');
			String docID = currentDocPath.substring(pos+1).replaceAll(".sgm", "");
			String path = currentDocPath;
			w.append(docID+"\t"+path+"\n");
			i++;
			System.out.println(i);
		}
		reader.close();
		
		w.close();
	}
	
	public static void findlost () throws IOException {
		String file_all1 = home +"sgmlist";
		String file_all2 = home +"donelist";
		String file_exist = home +"donelist";
		String file_lost = home + "filelost";
		String file_lost2 = home + "filelost2";
		
		String currentDocPathBase;
		ArrayList<String> v = new ArrayList<String>();
		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new FileReader(file_all1));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			//String [] str=currentDocPath.split("\\s+");
			currentDocPathBase=currentDocPath/*str[0]*/;
			currentDocPathBase= currentDocPathBase.substring(2);
			//currentDocPath=str[1];
			//map.put(currentDocPathBase, str[1]);
			v.add(currentDocPathBase);
		}
		reader.close();
		
		/*reader = new BufferedReader(new FileReader(file_all2));
		
		while ((currentDocPath = reader.readLine()) != null) {
			String [] str=currentDocPath.split("\\s+");
			currentDocPathBase=str[0];
			//currentDocPath=str[1];
			map.put(currentDocPathBase, str[1]);
			v.add(currentDocPathBase);
		}
		reader.close();*/
		
		HashMap<String, Integer> map2 = new HashMap<String, Integer>();
		
		//ArrayList<String> v1 = new ArrayList<String>();
		reader = new BufferedReader(new FileReader(file_exist));
		int j=0;
		while ((currentDocPath = reader.readLine()) != null) {
			//v1.add(currentDocPath);
			currentDocPath= currentDocPath.replace(".apf.xml", ".sgm");
			currentDocPath= currentDocPath.substring(2);
			j++;
			map2.put(currentDocPath, j);
		}
		reader.close();
		
		ArrayList<String> v2 = new ArrayList<String>(v);
		System.out.println(v2.size());
		for (int i=0;i<v.size();i++){
			
			System.out.println(i+"\t"+v2.size());
			if (map2.containsKey(v.get(i)))
				v2.remove(v.get(i));

		}
		//v.removeAll(v1);
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (file_lost), encoding));
		for (int i=0;i<v2.size();i++){
			bw.append(v2.get(i)/*+"\t"+map.get(v2.get(i))*/+"\n");
		}
		
		bw.close();
	}
		
	public static void genData (String infileName, String tfileName,String afileName) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(infileName), encoding));
		
		String line;
		int runs = 0;
		//int lineNo = 0;
		BufferedWriter bw1 =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (tfileName), encoding));
		BufferedWriter bw2 =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (afileName), encoding));
		while((line = reader.readLine()) != null)
		{
			if (line.contains("runs")){
				for (int m=0;m<10;m++){
					line = reader.readLine();
					runs++;
					String trigger="";
					String argument="";
					for (int j=0;j<9;j++){
						line = reader.readLine();
						String[] items = line.split("\\s+");
						trigger+=Double.parseDouble(items[5])+"\t";
						argument+=Double.parseDouble(items[11])+"\t";
					}
					trigger +="\n";
					argument +="\n";
					bw1.write(trigger);
					bw2.write(argument);
				}
			}
		}
		
		bw1.close();
		bw2.close();
		System.out.println(Integer.toString(runs));
		return;
	}
	
	public static void genData1 (String infileName, String tfileName,String afileName) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(infileName), encoding));
		
		String line;
		int runs = 0;
		//int lineNo = 0;
		BufferedWriter bw1 =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (tfileName), encoding));
		BufferedWriter bw2 =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (afileName), encoding));
		while((line = reader.readLine()) != null)
		{
			if (line.contains("runs")){
				
					runs++;
					String trigger="";
					String argument="";
					for (int j=0;j<9;j++){
						line = reader.readLine();
						String[] items = line.split("\\s+");
						trigger+=Double.parseDouble(items[5])+"\t";
						argument+=Double.parseDouble(items[11])+"\t";
					}
					trigger +="\n";
					argument +="\n";
					bw1.write(trigger);
					bw2.write(argument);
				
			}
		}
		
		bw1.close();
		bw2.close();
		System.out.println(Integer.toString(runs));
		return;
	}
	
	public static void genData2 (String infileName, String fileName) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(infileName), encoding));
		
		String line;
		int runs = 0;
		//int lineNo = 0;
		BufferedWriter bw1 =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (fileName), encoding));
		
		while((line = reader.readLine()) != null)
		{
			if (line.contains("runs")){
				for (int m=0;m<10;m++){
					line = reader.readLine();
					//line = reader.readLine();
					runs++;
					String f="";
				//	line = reader.readLine();
					for (int j=0;j<1;j++){
						line = reader.readLine();
						/*line = reader.readLine();
						line = reader.readLine();
						line = reader.readLine();
						line = reader.readLine();*/
						
						String[] items = line.split("\\s+");
						/*double error1= Double.parseDouble(items[1]);
						double error2= Double.parseDouble(items[2]);
						double miss1= Double.parseDouble(items[3]);
						double miss2= Double.parseDouble(items[4]);
						
						f+=error1/(error1+error2)+"\t"+error2/(error1+error2)+"\t"+
						miss1/(miss1+miss2)+"\t"+miss2/(miss1+miss2)+"\t";*/
						f+=items[0]+"\t"+items[1]+"\t"+items[2]+"\t";
						
					}
					f +="\n";
					
					bw1.write(f);
					
				}
			}
		}
		
		bw1.close();
		
		System.out.println(Integer.toString(runs));
		return;
	}
	
	public static void genU8File (String fileName, String inDir, String outDir) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(fileName), "gb2312"));
		
		String currentDocPath;
		//int lineNo = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			StringBuffer text = new StringBuffer();
			String textFile = inDir + currentDocPath.replaceFirst(".sgm", ".apf.xml");
			BufferedReader reader1 = new BufferedReader(new InputStreamReader (new FileInputStream(textFile), "gb2312"));
			String line;
			while((line = reader1.readLine()) != null)
			{
				//line = line.replace("apf.v5.1.2.dtd", "apf.v5.1.1.dtd");
				/*line = line.replaceAll(" & ", " &amp; ");
				line = line.replaceAll("'", "&apos;");
				line = line.replaceAll(" 's ", " &apos;s ");
				int pos = line.indexOf('&');
				while (pos>=0){
					if (pos>=0&&Character.isUpperCase(line.charAt(pos+1)))
						line = line.substring(0,pos)+"&amp;"+line.substring(pos+1);
					pos = line.indexOf('&',pos+1);
				}	*/
				text.append(line+'\n');
			
			}
			String sentFileName = outDir+ currentDocPath.replaceFirst(".sgm", ".apf.xml") ;
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.flush();
			bw.write(text.toString());
			bw.close();
		}
		
		return;
	}
	
	public static void genSentence (String fileName, String inDir, String outDir) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(fileName), encoding));
		
		String currentDocPath;
		//int lineNo = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			
			
			StringBuffer text = new StringBuffer();
			String textFile = inDir + currentDocPath.replaceFirst(".sgm", ".sgm");
			BufferedReader reader1 = new BufferedReader(new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			while((line = reader1.readLine()) != null)
			{
				//line = line.replace("apf.v5.1.2.dtd", "apf.v5.1.1.dtd");
				if (line.contains("<TEXT>")){
					text.append(line+'\n');
					
					int id = 0;
					while (true){
						line = reader1.readLine();
						if (line.contains("</TEXT>"))
							break;
						else{
							id ++;
							line = "<sentence ID=\"SENT-"+Integer.toString(id)+"\">"+line+"</sentence>\n";
							text.append(line);
						}
					}
					text.append(line+'\n');
				}
				else 
					text.append(line+'\n');
			}
			String sentFileName = outDir+ currentDocPath.replaceFirst(".sgm", ".sgm") ;
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text.toString());
			bw.close();
		}
		
		return;
	}
	
	public static void makeapf (String fileName, String inDir, String outDir) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(fileName), encoding));
		
		String currentDocPath;
		BufferedWriter bw =
			  new BufferedWriter
					(new OutputStreamWriter
						(new FileOutputStream (home+"finallist3"), encoding));
		//int lineNo = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			String textFile = inDir + currentDocPath;
			BufferedReader reader1 = new BufferedReader(new InputStreamReader (new FileInputStream(textFile), "gb2312"));
			String line;
			boolean bFind = false;
			while((line = reader1.readLine()) != null)
			{
				//line = line.replace("apf.v5.1.2.dtd", "apf.v5.1.1.dtd");
				if (line.indexOf('(')>=0)
				{
					bFind = true;
					break;
				}
					
			}
			if (!bFind)
				bw.write(currentDocPath+"\n");
		}
		bw.close();
		return;
	}
	
	public static void makeapfxml (String fileName, String inDir, String outDir) throws IOException {
		BufferedReader reader = new BufferedReader (
				new InputStreamReader (new FileInputStream(fileName), encoding));
		
		String currentDocPath;
		
		//int lineNo = 0;
		while((currentDocPath = reader.readLine()) != null)
		{	
			String textFile = inDir + currentDocPath;
			BufferedReader reader1 = new BufferedReader(new InputStreamReader (new FileInputStream(textFile), encoding));
			String buf=/*"<DOC>\n";
			buf += "<DOCID>";
			buf += currentDocPath.substring(0,currentDocPath.indexOf('.'));
			buf += "</DOCID>\n";
			buf +="<BODY>\n*/"<TEXT>\n";
			String line;
			
			while((line = reader1.readLine()) != null)
			{
				//line = line.replace(" ", "");
				buf += line+"\n";
			}
			
			buf +="</TEXT>\n";/*</BODY>\n</DOC>\n";*/
			String outputFile = outDir + currentDocPath.replace(".sent", ".sgm");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (outputFile), encoding));
			bw.write(buf);
			bw.close();
		}
		
		return;
	}
	
	public static void fixParse (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			StringBuffer text = new StringBuffer();
			String textFile = parserDir + currentDocPath.replaceFirst(".sgm", ".Parsed");
			BufferedReader reader1 = new BufferedReader (
					// (new FileReader(file));
					new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			while((line = reader1.readLine()) != null)
			{
				int pos = line.indexOf("(");
				text.append(line.substring(pos)+'\n');
			}
			String sentFileName = fixparserDir+ currentDocPath.replaceFirst(".sgm", ".Parsed") ;
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text.toString());
			bw.close();
		}
		
		return;
	}
	
	public static void fixSGMPunc (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			docCount++;
			//System.out.println ("Processing document " + docCount + " : " + currentDocPath);
			
			//StringBuffer text = new StringBuffer();
			String text = "";
			String textFile = dataDir + currentDocPath.replaceFirst(".sgm", ".sgm");
			BufferedReader reader1 = new BufferedReader (
					// new FileReader(textFile));
					new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			while((line = reader1.readLine()) != null)
			{
				//line = line.replaceAll("\u300c","\u201c");
				//line = line.replaceAll("\u300d","\u201d");
				if (line.endsWith(".")){
					if (line.length()>=2&&line.charAt(line.length()-2)=='.')
						text= text+line+"\n";
					else{
						String nextLine = reader1.readLine();
						if (nextLine.equals("</P>")){
							line=line.substring(0,line.length()-1)+"\u3002";
							text= text+line+"\n";
							System.out.println ("Processing document " + docCount + " : " + currentDocPath);
						    
						}
						else{
							text= text+line+"\n";
						}
						text= text+nextLine+"\n";	
					}
				}	
				else
					text= text+line+"\n";
			}
			text = text.replaceAll("\u300c","\u201c");
			//text = text.replaceAll("\n\u300d"," \u201d");
			text = text.replaceAll("\u300d","\u201d");
			/*Pattern p=Pattern.compile(" {2,}");

			Matcher m=p.matcher(text);

	        text=m.replaceAll(" ");*/
	        /*if (text.indexOf(".\n")>0)
	        	System.out.println ("Processing document " + docCount + " : " + currentDocPath);
	       */ 
	        
	        

			String sentFileName = outDir + currentDocPath.replaceFirst(".sgm", ".sgm");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text);
			bw.close();
		}
		
		return;
	}
	public static void fixParsePunc (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			docCount++;
			//System.out.println ("Processing document " + docCount + " : " + currentDocPath);
			
			//StringBuffer text = new StringBuffer();
			String text = "";
			String textFile = dataDir + currentDocPath.replaceFirst(".sgm", ".parsed");
			BufferedReader reader1 = new BufferedReader (
					// new FileReader(textFile));
					new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			//int lineCount = 0;
			while((line = reader1.readLine()) != null)
			{
				//lineCount++;
				//line = line.replaceAll("\u300c","\u201c");
				//line = line.replaceAll("\u300d","\u201d");
				
				text= text+line+"\n";
				
			}
			text = text.replaceAll("NN \\)","PU RPA");
			text = text.replaceAll("PU \\)","PU RPA");
			text = text.replaceAll("VV \\)","PU RPA");
			text = text.replaceAll("NR \\)","PU RPA");
			text = text.replaceAll("NR \\(","PU LPA");
			text = text.replaceAll("PU \\(","PU LPA");
			text = text.replaceAll("VV \\(","PU LPA");
			text = text.replaceAll("NN \\(\\)","PU LPA)");
			text = text.replaceAll("FW \\(\\)","PU LPA)");
			text = text.replaceAll("VC \\(\\)","PU LPA)");
			text = text.replaceAll("NT \\(\\)","PU LPA)");
			text = text.replaceAll(" M \\(\\)","PU LPA)");
			text = text.replaceAll("CD \\(\\)","PU LPA)");
			text = text.replaceAll("JJ \\(\\)","PU LPA)");
			String sentFileName = outDir + currentDocPath.replaceFirst(".sgm", ".parsed");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text);
			bw.close();
		}
		
		return;
	}
	
	public static void fixsentence (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			docCount++;
			//System.out.println ("Processing document " + docCount + " : " + currentDocPath);
			
			//StringBuffer text = new StringBuffer();
			String text = "";
			String textFile = dataDir + currentDocPath.replace(".sgm", ".sgm.sent");
			BufferedReader reader1 = new BufferedReader (
					// new FileReader(textFile));
					new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			//int lineCount = 0;
			while((line = reader1.readLine()) != null)
			{
				//lineCount++;
				//line = line.replaceAll("\u300c","\u201c");
				//line = line.replaceAll("\u300d","\u201d");
				line = line.replaceAll("\\b\\s{2,}\\b", " ");

				text= text+line+"\n";
				
			}
			
			String sentFileName = outDir + currentDocPath.replaceFirst(".sgm", ".sgm.sent");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text);
			bw.close();
		}
		
		return;
	}
	
	public static void fixSEGPunc (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			docCount++;
			//System.out.println ("Processing document " + docCount + " : " + currentDocPath);
			
			//StringBuffer text = new StringBuffer();
			String text = "";
			String textFile = dataDir + currentDocPath.replaceFirst(".sgm", ".seg");
			BufferedReader reader1 = new BufferedReader (
					// new FileReader(textFile));
					new InputStreamReader (new FileInputStream(textFile), "gb2312"));
			String line;
			//int lineCount = 0;
			while((line = reader1.readLine()) != null)
			{
				//lineCount++;
				//line = line.replaceAll("\u300c","\u201c");
				//line = line.replaceAll("\u300d","\u201d");
				
				if (line.endsWith(".")){
					System.out.println ("Processing document " + docCount + " : " + currentDocPath);
					text= text+line;
				}
				else{
					text= text+line+"\n";
				}
				
			}
			text = text.replaceAll("\u300c","\u201c");
			text = text.replaceAll("\n\u300d"," \u201d");
			text = text.replaceAll("\u300d","\u201d");
			//text = text.replaceAll("\u201d\n","\u201d ");
			text = text.replaceAll("\u3002 \u201d ","\u3002 \u201d\n");
			text = text.replaceAll("\u3002\n\u201d","\u3002 \u201d\n");
			text = text.replaceAll(": ",":\n");
			text = text.replaceAll("\uff01 ","\uff01\n");
			text = text.replaceAll("\uff1f ","\uff1f\n");
			Pattern p=Pattern.compile(" {2,}");

			Matcher m=p.matcher(text);

	        text=m.replaceAll(" ");
	        /*if (text.indexOf(".\n")>0)
	        	System.out.println ("Processing document " + docCount + " : " + currentDocPath);
	       */ 
	        
			String sentFileName = outDir + currentDocPath.replaceFirst(".sgm", ".seg");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text);
			bw.close();
		}
		
		return;
	}
	
	public static void fixPlainsentPunc (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			docCount++;
			//System.out.println ("Processing document " + docCount + " : " + currentDocPath);
			
			//StringBuffer text = new StringBuffer();
			String text = "";
			String textFile = dataDir + currentDocPath.replaceFirst(".sgm", ".sen");
			BufferedReader reader1 = new BufferedReader (
					// new FileReader(textFile));
					new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			//int lineCount = 0;
			while((line = reader1.readLine()) != null)
			{
				//lineCount++;
				//line = line.replaceAll("\u300c","\u201c");
				//line = line.replaceAll("\u300d","\u201d");
				
				
				text= text+line+"\n";
			
				
			}
			
			if (text.indexOf("\\?")>0 || text.indexOf("\u3002\n\u201d")>0)
				System.out.println ("Processing document " + docCount + " : " + currentDocPath);
		      
			text=text.replace("\\?", "\\?\n");
			text = text.replaceAll("\u3002\n\u201d","\u3002\u201d\n");
	        /*if (text.indexOf(".\n")>0)
	        	System.out.println ("Processing document " + docCount + " : " + currentDocPath);
	       */ 
	        
			String sentFileName = outDir + currentDocPath.replaceFirst(".sgm", ".sen");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text);
			bw.close();
		}
		
		return;
	}
	
	public static void fixSegmorePunc (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			docCount++;
			//System.out.println ("Processing document " + docCount + " : " + currentDocPath);
			
			//StringBuffer text = new StringBuffer();
			String text = "";
			String textFile = dataDir + currentDocPath.replaceFirst(".sgm", ".seg");
			BufferedReader reader1 = new BufferedReader (
					// new FileReader(textFile));
					new InputStreamReader (new FileInputStream(textFile), encoding));
			String line;
			//int lineCount = 0;
			while((line = reader1.readLine()) != null)
			{
				//lineCount++;
				//line = line.replaceAll("\u300c","\u201c");
				//line = line.replaceAll("\u300d","\u201d");
				
				//if (line.length()!=0)
					
			
				/*if (line.indexOf('?')>0&&line.indexOf('?')!=line.length()-1){
					line = line.replace("?", "?\n");
				}
				if (line.indexOf('!')>0&&line.indexOf('!')!=line.length()-1){
					line = line.replace("!", "!\n");
				}*/
				line = line.trim();
				text= text+line+"\n";
			}
			text = text.replaceAll(" \\?\n\u201d"," \\? \u201d");
			text = text.replaceAll(" \u0021\n\u201d"," \u0021 \u201d");
			/*text = text.replaceAll(" ?\n)"," ? )\n");
			text = text.replaceAll(" !\n)"," ! )\n");
			text = text.replaceAll(" \u3002\n)"," \u3002 )\n");*/
			/*text = text.replaceAll("\u3002 ","\u3002\n");
			text = text.replaceAll(" \u201d\n"," \u201d ");
			text = text.replaceAll("\u3002 \u201d ","\u3002 \u201d\n");
			text = text.replaceAll("\u3002\n\u201d","\u3002 \u201d\n");
			
			text = text.replaceAll("\uff1f\n\u201d","\uff1f \u201d\n");
			text = text.replaceAll("\uff01\n\u201d","\uff01 \u201d\n");
			text = text.replaceAll(" \u201d\n\n"," \u201d\n");*/
			
			String sentFileName = outDir + currentDocPath.replaceFirst(".sgm", ".seg");
			BufferedWriter bw =
				  new BufferedWriter
						(new OutputStreamWriter
							(new FileOutputStream (sentFileName), encoding));
			bw.write(text);
			bw.close();
		}
		
		return;
	}
	
	public static void genTrainFileList () throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(totalFileList));
		ArrayList fileList = new ArrayList();
		ArrayList testList = new ArrayList();
		String currentDocPath;
		//int lineNo = 0;
		int docCount = 0;
		while((currentDocPath = reader.readLine()) != null)
		{
			fileList.add(currentDocPath);
		}
		reader.close();
		reader = new BufferedReader(new FileReader(testFileList));
		while((currentDocPath = reader.readLine()) != null)
		{
			testList.add(currentDocPath);
		}
		reader.close();
		fileList.removeAll(testList);
		PrintStream textWriter = new PrintStream(new FileOutputStream(
				trainFileList));
		for (int i=0;i<fileList.size();i++)
		{
			textWriter.append(fileList.get(i)+"\n");
		}
		textWriter.close();
	}
	
	public static void ProcessDir(String strPath)
	throws IOException {
		/*String strPath2 = strPath + "adj/";
		String textFile = strPath + "list.txt";*/
		try {
			File dir1 = new File(strPath);
			
			if (dir1.exists() && dir1.isDirectory()) {
				
					File[] fileList = dir1.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						File sgm = fileList[i];
						if (sgm.isDirectory())
						{
							File[] sgmList = sgm.listFiles();
							if (sgmList.length!=25)
							{
								System.out.println("Wrong folder "
										+ sgm.getName());
								//sgm.delete();
							}
						}
					}
					
				}
		
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	static String source = "C:/Users/blender/workspace/blender/corpus/ACE05/sent/";
	
	public static void ComposeRel(String strPath) throws IOException {
		try {
			File dir1 = new File(strPath);
			
			if (dir1.exists() && dir1.isDirectory()) {
				
					File[] fileList = dir1.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						File sgm = fileList[i];
						if (sgm.isDirectory())
						{
							File[] sgmList = sgm.listFiles();
							if (sgmList.length==25)
							{
								System.out.println("Processing "
										+ sgm.getName());
								String textFile = strPath + sgm.getName()+".rel";
								PrintStream textWriter = new PrintStream(new FileOutputStream(
										textFile));
								StringBuffer str= new StringBuffer();
								str.append("<DOC>\n<DOCID>"+sgm.getName()+"</DOCID>\n<BODY>\n<TEXT>\n");
								String text;
								BufferedReader r = new BufferedReader(new FileReader(source+sgm.getName()+".sent"));
								while((text = r.readLine()) != null)
								{
									str.append(text+"\n");
								}
								str.append("#####################0\n");
								r.close();
								for (int j=0;j<sgmList.length;j++){
									BufferedReader reader = new BufferedReader(new FileReader(sgmList[j].getAbsolutePath()));
									String line;
									
									while((line = reader.readLine()) != null)
									{
										int pos = line.indexOf(' ');
										line = line.substring(pos+1);
										str.append(line+"\n");
									}
									reader.close();
									str.append("#####################"+(j+1)+"\n");
								}
								str.append("</TEXT>\n</BODY>\n</DOC>");
								textWriter.flush();
								textWriter.print(str.toString());
								textWriter.close();
							}
						}
					}
					
				}
		
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static void makeList(String strPath, String prefix)
			throws IOException {
		String strPath2 = strPath + "adj/";
		String textFile = strPath + "list.txt";
		try {
			File dir1 = new File(strPath);
			
			if (dir1.exists() && dir1.isDirectory()) {
				File dir2 = new File(strPath2);
				if (dir2.exists() && dir2.isDirectory()) {
					PrintStream textWriter = new PrintStream(
							new FileOutputStream(textFile));
					File[] fileList = dir2.listFiles();
					for (int i = 0; i < fileList.length; i++) {
						String fileName = fileList[i].getName();
						if (fileName.endsWith(".sgm")) {
							textWriter
									.println(/*prefix+"/timex2norm/"+*/fileName);
						}
					}
					System.out.println("File list has been saved in \""
							+ textFile + "\"");
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public static void makeList1(String strPath) throws IOException {
		String textFile = home + "../filelist";
		try {
			File dir1 = new File(strPath);
			if (dir1.exists() && dir1.isDirectory()) {
				PrintStream textWriter = new PrintStream(new FileOutputStream(textFile));
				File[] fileList = dir1.listFiles();
				for (int i = 0; i < fileList.length; i++) {
					System.out.println(i);
					String fileName = fileList[i].getName();
					/*if (fileName.endsWith(".Pos")) {
						textWriter.println(fileName.replaceFirst(".Pos", ".sgm"));
					}*/
					if (fileName.endsWith(".apf.xml")) {
						textWriter.println(fileName.replaceFirst(".apf.xml",""));
					}
				}
				System.out.println("File list has been saved in \"" + textFile
						+ "\"");
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
}