package cuny.blender.englishie.indri;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.TreeSet;

import cuny.blender.englishie.ace.AceDocument;
import cuny.blender.englishie.ace.AceEntity;
import cuny.blender.englishie.ace.AceEntityMention;
import cuny.blender.englishie.ace.AceEvent;
import cuny.blender.englishie.ace.AceEventArgumentValue;
import cuny.blender.englishie.ace.AceEventMention;
import cuny.blender.englishie.ace.AceEventMentionArgument;
import cuny.blender.englishie.ace.AceTimex;
import cuny.blender.englishie.ace.AceTimexMention;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.tipster.ExternalDocument;

import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;

public class IndriWrapper {

	static String home;

	static String sourceDir;
	static String apfDir;
	static String filelist;
	static String triggerDir;

	static String indexDir;
	static String tdt5Dir;

	static double EVENT_THRESHOLD = 0.3;
	static protected Properties config = new Properties();

	public static void main(String[] args) throws Exception {
		//getQueriesByType();
		//queryIndriBytype();
		initializeFromConfig("props/coref.property");
		String filelist = "/m3/KBP/software/EnglishIE/ENIE/dataset/exp1/dataset0/fold0/testing.list";
		String outfile = "/m3/KBP/software/EnglishIE/ENIE/dataset/exp1/dataset0/fold0/out.list";
		printEventNo(filelist, outfile);
	}

	public static void queryIndri() throws Exception {
		initializeFromConfig("props/coref.property");
		loadQueries();
		query();
	}
	
	public static void queryIndriBytype() throws Exception {
		initializeFromConfig("props/coref.property");
		loadQueriesBytype();
		queryBytype();
	}

	public static void createIndriInParameter() throws IOException {
		initializeFromConfig("props/coref.property");
		String inFile = "/m1/GALE/TDT5/english/indriInParameter";
		getInParameterFileFromTDT5(inFile, indexDir, tdt5Dir);
	}

	public static void getQueries() throws IOException {
		initializeFromConfig("props/coref.property");
		loadTriggers();
		constructQueries();
	}
	
	public static void getQueriesByType() throws IOException {
		initializeFromConfig("props/coref.property");
		loadTriggers();
		constructQueriesByType();
	}
	

	public static void initializeFromConfig(String file) {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			config.load(in);

			filelist = config.getProperty("dataset.filelist");

			home = config.getProperty("dataset.home");
			sourceDir = config.getProperty("dataset.srcdir");
			apfDir = config.getProperty("dataset.apfdir");
			triggerDir = config.getProperty("dataset.triggerdir");
			tdt5Dir = config.getProperty("dataset.tdt5dir");
			indexDir = config.getProperty("dataset.indexdir");
		} catch (IOException ioe) {
			System.err.println("Error: could not open file " + file);
			System.exit(1);
		}
	}

	static void loadFileList(String filename, List<String> files)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		while ((line = reader.readLine()) != null) {
			if (!files.contains(line))
				files.add(line);
		}

		reader.close();
	}

	public static void getInParameterFileFromTDT5(String inFile,
			String indexDir, String tdt5Dir) throws IOException {

		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(inFile), "UTF8"));
		out.append("<parameters>\n<memory>200m</memory>\n<index>");
		out.append(indexDir
				+ "</index>\n<stemmer><name>krovetz</name></stemmer>\n");

		File dir = new File(tdt5Dir);
		if (dir.exists() && dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				if (file.isDirectory()) {
					out.append("<corpus>\n<path>" + file.getAbsolutePath()
							+ "</path>\n<class>xml</class>\n</corpus>\n");
				}
			}
		}
		// out.append("<field><name>headline</name></field>\n");

		// out.append("<field><name>headline</name></field>\n<field><name>text</name></field>\n");
		out.append("</parameters>");
		out.close();
	}

	static TreeMap<String, List<String>> triggers = new TreeMap<String, List<String>>();

	public static void loadTriggers() throws IOException {
		File dir = new File(triggerDir);
		if (dir.exists() && dir.isDirectory()) {
			File[] fileList = dir.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				File file = fileList[i];
				if (file.isFile()) {
					String type = file.getName();
					int pos = type.indexOf(".txt");
					if (pos >= 0) {
						type = type.substring(0, pos);
					}
					String filename = file.getAbsolutePath();
					BufferedReader reader = new BufferedReader(new FileReader(
							filename));
					String line;

					List<String> ts = new ArrayList<String>();
					while ((line = reader.readLine()) != null) {
						if (!ts.contains(line.trim())) {
							ts.add(line.trim());
						}
					}

					triggers.put(type, ts);
					reader.close();
				}
			}
		}
	}

	static TreeMap<String, String> queries = new TreeMap<String, String>();
	static TreeMap<String, TreeMap<String,String>> queriesBytype = new TreeMap<String, TreeMap<String,String>>();

	public static void loadQueries() throws IOException {

		BufferedReader reader = new BufferedReader(
				new FileReader("log/queries"));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] items = line.split("=");
			queries.put(items[0], items[1]);
		}

		reader.close();
	}
	
	public static void loadQueriesBytype() throws IOException {

		BufferedReader reader = new BufferedReader(
				new FileReader("log/queries_by_type"));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] items = line.split("=");
			String filename = items[0].trim();
			String tmp = items[1].trim();
			TreeMap<String, String> map = new TreeMap<String,String> ();
			if (!tmp.isEmpty()){
				String[] subitems= tmp.split(";");
				for (int j=0;j<subitems.length;j++){
					String type= subitems[j];
					String [] subitemitems = type.split(":");
					map.put(subitemitems[0], subitemitems[1]);
				}
			}
			queriesBytype.put(items[0], map);
		}

		reader.close();
	}


	public static void query() throws Exception {
		QueryEnvironment env = new QueryEnvironment();

		String[] names;
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("log/query_results"), "UTF8"));

		// open an Indri repository
		env.addIndex(indexDir);

		Iterator it = queries.keySet().iterator();
		int count = 0;
		while (it.hasNext()) {
			String filename = (String) it.next();
			count++;
			out.append(filename + "=");
			System.out.println(count + "/" + queries.size() + ":" + filename);
			String query = queries.get(filename).trim();
			if (query.isEmpty()) {
				out.append(" \n");
				continue;
			}
			ScoredExtentResult[] results;
			// System.out.println(query);
			results = env.runQuery(query, 20);

			// fetch the names of the retrieved documents
			names = env.documentMetadata(results, "docno");

			String str = "";
			for (int i = 0; i < results.length; i++) {
				DecimalFormat df = new DecimalFormat("#.####");
				str += names[i] + ":" + df.format(results[i].score) + "|";
			}
			if (str.length() > 0)
				str = str.substring(0, str.length() - 1);
			else
				str = " ";
			out.append(str + "\n");
		}
		env.close();
		out.close();
	}

	public static void queryBytype() throws Exception {
		QueryEnvironment env = new QueryEnvironment();

		String[] names;
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("log/query_results_by_type"), "UTF8"));

		// open an Indri repository
		env.addIndex(indexDir);

		Iterator it = queriesBytype.keySet().iterator();
		int count = 0;
		while (it.hasNext()) {
			String filename = (String) it.next();
			count++;
			out.append(filename + "=");
			System.out.println(count + "/" + queriesBytype.size() + ":" + filename);
			TreeMap<String,String> map= queriesBytype.get(filename);
			if (map.size()==0) {
				out.append(" \n");
				continue;
			}
			Iterator it2 = map.keySet().iterator();
			String str = "";
			while (it2.hasNext()) {
				String subtype = (String) it2.next();
				String query = map.get(subtype);
				ScoredExtentResult[] results;
				// System.out.println(query);
				results = env.runQuery(query, 20);

				// fetch the names of the retrieved documents
				names = env.documentMetadata(results, "docno");

				str+=subtype+":";
				
				for (int i = 0; i < results.length; i++) {
					DecimalFormat df = new DecimalFormat("#.####");
					str += names[i] + "*" + df.format(results[i].score) + "|";
				}
				if (results.length>0){
					str=str.substring(0,str.length()-1);
					str+=";";
				}
				else
					str+=" ;";
			}
			
			str = str.substring(0, str.length() - 1);
			
			out.append(str + "\n");
		}
		env.close();
		out.close();
	}
	
	public static void printEventNo(String filelist, String outfile) throws IOException {
		List<String> fileNames = new ArrayList<String>();
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outfile), "UTF8"));

		loadFileList(filelist, fileNames);
		for (int i = 0; i < fileNames.size(); i++) {
			int pos = fileNames.get(i).lastIndexOf(".");
			String docid = fileNames.get(i).substring(0,pos);
			out.append(docid + ":");
			System.out.println(fileNames.get(i));
			String filename = fileNames.get(i);
			String textFile = sourceDir + filename;
			String xmlFile = apfDir + filename.replaceFirst(".sgm", ".apf.xml");
			String docId = filename.replaceFirst(".sgm", "");

			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			/*
			 * doc.setAllTags(true); doc.open(); doc.stretchAll();
			 * Control.processDocument(doc, null, false, 0);
			 */

			TreeMap<String, List<AceEvent>> eventNos = new TreeMap<String, List<AceEvent>>();
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			for (int j = 0; j < aceDoc.events.size(); j++) {
				AceEvent e = (AceEvent) aceDoc.events.get(j);
				if (eventNos.get(e.subtype) == null) {
					eventNos.put(e.subtype, new ArrayList<AceEvent>());
				}
				List<AceEvent> es = eventNos.get(e.subtype);
				es.add(e);
			}
			Iterator it = eventNos.keySet().iterator();
			while (it.hasNext()) {
				String type = (String) it.next();
				List<AceEvent> events = eventNos.get(type);
				out.append(type + "|" + events.size() + ":");
				String str = "";
				for (int j = 0; j < events.size(); j++) {
					str = str + events.get(j).mentions.size() + ",";
				}
				str = str.substring(0, str.length() - 1);
				out.append(str + "\t");
			}
			out.append("\n");
		}
		out.close();
	}

	static boolean ENABLE_TRIGGER = true;
	static boolean ENABLE_TRIGGER_SYNOYM = false;
	static boolean ENABLE_ARGUMENT_COREF = true;

	public static void constructQueries() throws NumberFormatException,
			IOException {
		List<String> fileNames = new ArrayList<String>();
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("log/queries"), "UTF8"));

		loadFileList(filelist, fileNames);
		for (int i = 0; i < fileNames.size(); i++) {
			out.append(fileNames.get(i) + "=");
			System.out.println(fileNames.get(i));
			String filename = fileNames.get(i);

			String textFile = sourceDir + filename;
			String xmlFile = apfDir + filename.replaceFirst(".sgm", ".apf.xml");
			String docId = filename.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			TreeMap<String, List<AceEvent>> eventNos = new TreeMap<String, List<AceEvent>>();
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);

			TreeMap<String, Double> args = new TreeMap<String, Double>();
			TreeMap<String, Double> ts = new TreeMap<String, Double>();
			for (int j = 0; j < aceDoc.events.size(); j++) {
				AceEvent e = (AceEvent) aceDoc.events.get(j);
				for (int k = 0; k < e.mentions.size(); k++) {
					AceEventMention em = (AceEventMention) e.mentions.get(k);
					// process trigger
					double eventconf = em.confidence;
					if (eventconf < EVENT_THRESHOLD)
						continue;

					if (ENABLE_TRIGGER) {
						String anchor = em.anchorText.replaceAll("\n", " ");
						if (ENABLE_TRIGGER_SYNOYM) {
							if (triggers.get(e.type + "_" + e.subtype) != null) {
								List<String> anchors = triggers.get(e.type
										+ "_" + e.subtype);
								for (int m = 0; m < anchors.size(); m++) {
									String word = anchors.get(m);
									if (ts.get(word) == null)
										ts.put(word, eventconf);
									else {
										double score = ts.get(word);
										if (eventconf > score)
											ts.put(word, eventconf);
									}
								}
							}
						} else {
							if (ts.get(anchor) == null)
								ts.put(anchor, eventconf);
							else {
								double score = ts.get(anchor);
								if (eventconf > score)
									ts.put(anchor, eventconf);
							}
						}
					}

					// process each argument
					for (int m = 0; m < em.arguments.size(); m++) {
						AceEventMentionArgument arg = em.arguments.get(m);
						double conf = eventconf * arg.confidence;

						if (ENABLE_ARGUMENT_COREF) {
							AceEventArgumentValue value = arg.value.getParent();
							if (value instanceof AceEntity) {
								for (int n = 0; n < ((AceEntity) value).mentions
										.size(); n++) {
									AceEntityMention coref = (AceEntityMention) ((AceEntity) value).mentions
											.get(n);
									if (!coref.type.equals("NAM")
											&& !coref.type.equals("NOM"))
										continue;
									String head = coref.getHeadText();
									if (args.get(head) == null)
										args.put(head, conf);
									else {
										double score = args.get(head);
										if (conf > score)
											args.put(head, conf);
									}
								}
							}
							if (value instanceof AceTimex) {
								for (int n = 0; n < ((AceTimex) value).mentions
										.size(); n++) {
									AceTimexMention coref = (AceTimexMention) ((AceTimex) value).mentions
											.get(n);

									String head = coref.getHeadText();
									if (args.get(head) == null)
										args.put(head, conf);
									else {
										double score = args.get(head);
										if (conf > score)
											args.put(head, conf);
									}
								}
							}
						}

						else {
							String head = arg.value.getHeadText();
							if (args.get(head) == null)
								args.put(head, conf);
							else {
								double score = args.get(head);
								if (conf > score)
									args.put(head, conf);
							}
						}
					}
				}
			}
			String query = "#combine (";
			if (ENABLE_TRIGGER && ts.size() > 0) {
				query += "#weight(";
				Iterator it = ts.keySet().iterator();
				double sum = 0;
				while (it.hasNext()) {
					String word = (String) it.next();

					double score = ts.get(word);
					sum += score;
				}

				it = ts.keySet().iterator();

				while (it.hasNext()) {
					String word = (String) it.next();

					double score = ts.get(word);
					DecimalFormat df = new DecimalFormat("#.####");
					query += df.format(score / sum) + "  #1(";
					word = formatIndriString(word);
					query += word + ") ";
				}
				query += ") ";
			}

			if (args.size() > 0) {
				query += "#weight(";
				Iterator it = args.keySet().iterator();
				double sum = 0;
				while (it.hasNext()) {
					String word = (String) it.next();
					double score = args.get(word);
					sum += score;
				}
				it = args.keySet().iterator();

				while (it.hasNext()) {
					String word = (String) it.next();

					double score = args.get(word);
					DecimalFormat df = new DecimalFormat("#.####");
					query += df.format(score / sum) + " #1(";
					word = formatIndriString(word);

					query += word + ") ";
				}
				query += ")";
			}
			query += ")";
			if (ts.size() > 0 || args.size() > 0)
				out.append(query + "\n");
			else
				out.append(" \n");

		}
		out.close();
	}

	public static void constructQueriesByType() throws NumberFormatException,
			IOException {
		List<String> fileNames = new ArrayList<String>();
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("log/queries_by_type"), "UTF8"));

		loadFileList(filelist, fileNames);
		for (int i = 0; i < fileNames.size(); i++) {
			out.append(fileNames.get(i) + "=");
			System.out.println(fileNames.get(i));
			String filename = fileNames.get(i);

			String textFile = sourceDir + filename;
			String xmlFile = apfDir + filename.replaceFirst(".sgm", ".apf.xml");
			String docId = filename.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			
			TreeMap<String, List<AceEvent>> eventNos = new TreeMap<String, List<AceEvent>>();
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			for (int j = 0; j < aceDoc.events.size(); j++) {
				AceEvent e = (AceEvent) aceDoc.events.get(j);
				if (eventNos.get(e.subtype) == null) {
					eventNos.put(e.subtype, new ArrayList<AceEvent>());
				}
				List<AceEvent> es = eventNos.get(e.subtype);
				es.add(e);
			}
			Iterator typeIt = eventNos.keySet().iterator();
			String str = "";
			while (typeIt.hasNext()){
				String subtype = (String)typeIt.next();
				List<AceEvent> es = eventNos.get(subtype);
				
				TreeMap<String, Double> args = new TreeMap<String, Double>();
				TreeMap<String, Double> ts = new TreeMap<String, Double>();
				for (int j = 0; j < es.size(); j++) {
					AceEvent e = (AceEvent) es.get(j);
					for (int k = 0; k < e.mentions.size(); k++) {
						AceEventMention em = (AceEventMention) e.mentions.get(k);
						// process trigger
						double eventconf = em.confidence;
						if (eventconf < EVENT_THRESHOLD)
							continue;

						if (ENABLE_TRIGGER) {
							String anchor = em.anchorText.replaceAll("\n", " ");
							if (ENABLE_TRIGGER_SYNOYM) {
								if (triggers.get(e.type + "_" + e.subtype) != null) {
									List<String> anchors = triggers.get(e.type
											+ "_" + e.subtype);
									for (int m = 0; m < anchors.size(); m++) {
										String word = anchors.get(m);
										if (ts.get(word) == null)
											ts.put(word, eventconf);
										else {
											double score = ts.get(word);
											if (eventconf > score)
												ts.put(word, eventconf);
										}
									}
								}
							} else {
								if (ts.get(anchor) == null)
									ts.put(anchor, eventconf);
								else {
									double score = ts.get(anchor);
									if (eventconf > score)
										ts.put(anchor, eventconf);
								}
							}
						}

						// process each argument
						for (int m = 0; m < em.arguments.size(); m++) {
							AceEventMentionArgument arg = em.arguments.get(m);
							double conf = eventconf * arg.confidence;

							if (ENABLE_ARGUMENT_COREF) {
								AceEventArgumentValue value = arg.value.getParent();
								if (value instanceof AceEntity) {
									for (int n = 0; n < ((AceEntity) value).mentions
											.size(); n++) {
										AceEntityMention coref = (AceEntityMention) ((AceEntity) value).mentions
												.get(n);
										if (!coref.type.equals("NAM")
												&& !coref.type.equals("NOM"))
											continue;
										String head = coref.getHeadText();
										if (args.get(head) == null)
											args.put(head, conf);
										else {
											double score = args.get(head);
											if (conf > score)
												args.put(head, conf);
										}
									}
								}
								if (value instanceof AceTimex) {
									for (int n = 0; n < ((AceTimex) value).mentions
											.size(); n++) {
										AceTimexMention coref = (AceTimexMention) ((AceTimex) value).mentions
												.get(n);

										String head = coref.getHeadText();
										if (args.get(head) == null)
											args.put(head, conf);
										else {
											double score = args.get(head);
											if (conf > score)
												args.put(head, conf);
										}
									}
								}
							}

							else {
								String head = arg.value.getHeadText();
								if (args.get(head) == null)
									args.put(head, conf);
								else {
									double score = args.get(head);
									if (conf > score)
										args.put(head, conf);
								}
							}
						}
					}
				}
				String query = "#combine (";
				if (ENABLE_TRIGGER && ts.size() > 0) {
					query += "#weight(";
					Iterator it = ts.keySet().iterator();
					double sum = 0;
					while (it.hasNext()) {
						String word = (String) it.next();

						double score = ts.get(word);
						sum += score;
					}

					it = ts.keySet().iterator();

					while (it.hasNext()) {
						String word = (String) it.next();

						double score = ts.get(word);
						DecimalFormat df = new DecimalFormat("#.####");
						query += df.format(score / sum) + "  #1(";
						word = formatIndriString(word);
						query += word + ") ";
					}
					query += ") ";
				}

				if (args.size() > 0) {
					query += "#weight(";
					Iterator it = args.keySet().iterator();
					double sum = 0;
					while (it.hasNext()) {
						String word = (String) it.next();
						double score = args.get(word);
						sum += score;
					}
					it = args.keySet().iterator();

					while (it.hasNext()) {
						String word = (String) it.next();

						double score = args.get(word);
						DecimalFormat df = new DecimalFormat("#.####");
						query += df.format(score / sum) + " #1(";
						word = formatIndriString(word);

						query += word + ") ";
					}
					query += ")";
				}
				query += ")";
				if (ts.size() > 0 || args.size() > 0)
					str += subtype+":"+query+";";
				else
					str += subtype+": ;";
			}
			if (str.length()>0){
				str = str.substring(0,str.length()-1);
			}
			else{
				str = " ";
			}
			out.append(str+"\n");
		}

			
		out.close();
	}

	public static String formatIndriString(String str) {
		str = str.replaceAll("\n", " ");
		String newStr = "";
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (Character.isLetterOrDigit(ch) || ch == ' ') {
				newStr += ch;
			}
		}
		return newStr;
	}
}