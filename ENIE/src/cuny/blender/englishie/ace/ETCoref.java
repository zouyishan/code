package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;
import java.nio.channels.FileChannel;

import org.apache.lucene.index.IndexReader;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import cuny.blender.englishie.algorithm.clustering.SpectralClusterer;
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


public class ETCoref {

	// minimal confidence for an arg to be used for coref determination
	static double COREF_CONFIDENCE = 0.10;
	// minimal probability for merging two events
	static double COREF_THRESHOLD = 0.40;

	PrintStream corefFeatureWriter;
	static GISModel corefModel;

	static String home;

	static String srcDir;
	static String apfDir;
	
	static String relmapfile;
	static String relSrcDir;
	static String relApfDir;
	
	static String approach;

	static protected Properties config = new Properties();
	private static IndexReader src_reader;
	private static String src_index = "";
	private static String src_map = "";
	private static String output_dir = "";
	private static int totalDatasets = 1;
	private static int folds = 10;
	private static int expID = 1;
	private static String filelist;
	private static int featureset;
	private static String evaluationMetric = "";
	static String corefFeatureFileName = "";
	static String corefModelFileName = "";
	private static String dtdfile;
	static TreeMap<String, TreeMap<String,List<String>>> queriesBytype = new TreeMap<String, TreeMap<String,List<String>>>();


	public ETCoref() {

	}

	public void loadQueriesBytype() throws IOException {

		BufferedReader reader = new BufferedReader(
				new FileReader("log/query_results_by_type"));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] items = line.split("=");
			String filename = items[0].trim();
			String tmp = items[1].trim();
			TreeMap<String, List<String>> map = new TreeMap<String,List<String>> ();
			if (!tmp.isEmpty()){
				String[] subitems= tmp.split(";");
				for (int j=0;j<subitems.length;j++){
					String tmp2= subitems[j];
					String [] subitemitems = tmp2.split(":");
					String type = subitemitems[0];
					List<String> rels = new ArrayList<String>();
					if (subitemitems.length>1){
						String tmp3=subitemitems[1];
						String[] lists = tmp3.split("\\|");
						
						for (int k=0;k<lists.length;k++){
							String file= lists[k];
							rels.add(file.split("\\*")[0]);
						}
					}
					map.put(subitemitems[0], rels);
				}
			}
			queriesBytype.put(items[0], map);
		}

		reader.close();
	}
	
	public void initializeFromConfig(File configFile) {
		InputStream in = null;
		try {
			in = new FileInputStream(configFile);
			config.load(in);
			filelist = config.getProperty("dataset.filelist");
			totalDatasets = Integer.parseInt(config
					.getProperty("dataset.total"));
			folds = Integer.parseInt(config.getProperty("dataset.folds"));
			expID = Integer.parseInt(config.getProperty("dataset.experimentID"));

			home = config.getProperty("dataset.home");
			srcDir = config.getProperty("dataset.srcdir");
			apfDir = config.getProperty("dataset.apfdir");
			dtdfile = config.getProperty("dataset.dtdfile");
			
			relmapfile = config.getProperty("dataset.relmapfile");
			relSrcDir = config.getProperty("dataset.relsrcdir");
			relApfDir = config.getProperty("dataset.relapfdir");
			
			approach = config.getProperty("coref.approach");
			featureset = Integer.parseInt(config.getProperty("coref.featureset"));
			evaluationMetric = config.getProperty("coref.metric");
	
		} catch (IOException ioe) {
			System.err.println("Error: could not open file " + configFile);
			System.exit(1);
		}
	}

	void saveFileList(String filename, List<String> files) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename), "UTF8"));
		for (int i = 0; i < files.size(); i++) {
			out.append(files.get(i) + "\n");
		}
		out.close();
	}

	void createDataSet() throws IOException {

		ArrayList<String> files = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(filelist));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			files.add(currentDocPath);
		}
		reader.close();

		File dir = new File("dataset/exp" + expID);
		if (!dir.exists()) {
			dir.mkdirs();
		} else {
			System.err.println("The experiment id " + expID
					+ " already exists, please assign a new experiment id.");
			return;
		}

		System.out.println("Create " + totalDatasets
				+ " datasets, and each dataset has " + folds + " folds");
		for (int i = 0; i < totalDatasets; i++) {
			Collections.shuffle(files);

			String path = "dataset/exp" + expID + "/dataset" + i + "/";
			File datasetDir = new File(path);
			if (!datasetDir.exists()) {
				datasetDir.mkdirs();
			}
			saveFileList(path + "/all.list", files);
			if (folds == 1) {
				String foldpath = path + "fold0/";
				File foldDir = new File(foldpath);
				if (!foldDir.exists()) {
					foldDir.mkdirs();
				}
				saveFileList(foldpath + "training.list", files);
			} else {
				for (int j = 0; j < folds; j++) {

					String foldpath = path + "fold" + j + "/";
					File foldDir = new File(foldpath);
					if (!foldDir.exists()) {
						foldDir.mkdirs();
					}
					saveFileList(path + "all.list", files);
					List<String> training = new ArrayList<String>(files);
					List<String> developing = new ArrayList<String>();
					List<String> testing = new ArrayList<String>();

					for (int k = files.size() * j / folds; k < files.size()
							* (j + 1) / folds; k++) {
						testing.add(files.get(k));
					}

					if (j != folds - 1) {
						for (int k = files.size() * (j + 1) / folds; k < files
								.size() * (j + 2) / folds; k++) {
							developing.add(files.get(k));
						}
					} else {
						for (int k = 0; k < files.size() * 1 / folds; k++) {
							developing.add(files.get(k));
						}
					}
					training.removeAll(testing);
					training.removeAll(developing);

					saveFileList(foldpath + "testing.list", testing);
					saveFileList(foldpath + "training.list", training);
					saveFileList(foldpath + "developing.list", developing);
				}
			}
		}
	}

	ArrayList allInOneClustering(AceDocument aceDoc, String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		ArrayList<AceEvent> newEvents = new ArrayList<AceEvent>();

		int aceEventNo = 1;

		HashMap<String, ArrayList> eventMap = getEventsByTypes(events, docId);
		for (String eventType : eventMap.keySet()) {
			ArrayList<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1&&!EXCLUDE_SINGLETONS) {
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				newEvents.add(e);
				aceEventNo++;
			} 
			else if (v.size() > 1){
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				for (int j=1;j<v.size();j++){
					AceEvent e2= v.get(j);
					AceEventMention mention = (AceEventMention) e2.mentions
							.get(0);

					mention.setId(e.id+"-"+(j+1));
					for (int iarg = 0; iarg < mention.arguments.size(); iarg++) {
						AceEventMentionArgument marg = (AceEventMentionArgument) mention.arguments
								.get(iarg);
						e.addArgument(new AceEventArgument(marg.value
								.getParent(), marg.role));
					}
					e.addMention(mention);
				}
				newEvents.add(e);
				aceEventNo++;
			}
		}
		return newEvents;
	}

	AceEvent findEventCluster(String subtype, ArrayList<AceEvent> newEvents) {
		for (int i = 0; i < newEvents.size(); i++) {
			if (subtype.equals(newEvents.get(i).subtype)) {
				return newEvents.get(i);
			}
		}
		return null;
	}

	static boolean EXCLUDE_SINGLETONS = true;
	public void tag(String expPath, List<String> fileNames)
			throws IOException {
		
		for (int i=0;i<fileNames.size();i++) {
			String filename = fileNames.get(i);
			String textFile = srcDir + filename;
			String xmlFile = apfDir + filename.replaceFirst(".sgm", ".apf.xml");
			String docId = filename.replaceFirst(".sgm", "");
			
			String outputFile = expPath + filename.replaceFirst(".sgm", ".apf.xml");

			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			Control.processDocument(doc, null, false, 0);
			
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			ArrayList<AceEvent> events = new ArrayList<AceEvent>();
			if (approach.equals("one-in-one"))
				events = oneInOneClustering(aceDoc, aceDoc.docID);
			else if (approach.equals("all-in-one"))
				events = allInOneClustering(aceDoc, aceDoc.docID);
			else if (approach.equals("aggr"))
				events = aggrClustering(aceDoc, doc,docId);
			else if (approach.equals("graph"))
				events = graphClustering(aceDoc, doc,docId);
			else if (approach.equals("graph-micro"))
				events = microClustering(aceDoc, doc,docId);
			AceEventCompare comp = new AceEventCompare();
			Collections.sort(events, comp);
			aceDoc.events = events;

			aceDoc.write(new BufferedWriter(new FileWriter(outputFile)), doc);
		}
	}
	
	public ArrayList oneInOneClustering(AceDocument aceDoc, String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		ArrayList<AceEvent> newEvents = new ArrayList<AceEvent>();

		int aceEventNo = 1;

		HashMap<String, ArrayList> eventMap = getEventsByTypes(events, docId);
		for (String eventType : eventMap.keySet()) {
			ArrayList<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1&&!EXCLUDE_SINGLETONS) {
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				newEvents.add(e);
				aceEventNo++;
			} 
			else if (v.size() > 1){
				for (int j = 0; j < v.size(); j++) {
					AceEvent e = v.get(j);
					String eventId = aceDoc.docID + "-EV" + aceEventNo;
					e.setId(eventId);
					AceEventMention m0 = (AceEventMention) e.mentions.get(0);
					m0.setId(e.id + "-1");
					newEvents.add(e);
					aceEventNo++;
				}
			}
		}
		return newEvents;
	}
	
	public ArrayList graphClustering(AceDocument aceDoc, Document doc,String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		ArrayList<AceEvent> newEvents = new ArrayList<AceEvent>();

		int aceEventNo = 1;

		HashMap<String, ArrayList> eventMap = getEventsByTypes(events, docId);
		for (String eventType : eventMap.keySet()) {
			ArrayList<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1&&!EXCLUDE_SINGLETONS) {
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				newEvents.add(e);
				aceEventNo++;
			} 
			else if (v.size() > 1){
				DoubleMatrix2D w = null;
				//if (choice == 0)
				w = csimModel(v, doc);
				/*else if (choice == 1)
					w = csimFormula(v, doc, relations);
				else if (choice == 2)
					w = csimOverlap(v, doc, relations);
				else if (choice == 3)
					w = csimTfIdf(v, doc, relations,docId);*/
				SpectralClusterer sc = new SpectralClusterer();
				int[][] p = null;
				try {
					p = sc.buildClusterer(w, COREF_THRESHOLD);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (p == null){
					System.err.println("Error in graph clustering...");
					return newEvents; 
					//System.exit(-1);
				}
				for (int i = 0; i < p.length; i++) {
					AceEvent priorEvent = v.get(p[i][0]);
					String eventId = aceDoc.docID + "-EV" + aceEventNo;
					priorEvent.setId(eventId);
					AceEventMention m0 = (AceEventMention) priorEvent.mentions
							.get(0);
					m0.setId(priorEvent.id + "-1");
					for (int j = 1; j < p[i].length; j++) {
						AceEvent event = v.get(p[i][j]);
						priorEvent.arguments = mergeArguments(event.arguments,
								priorEvent.arguments);
						AceEventMention m = (AceEventMention) event.mentions
								.get(0);
						priorEvent.addMention(m);
						// fix id for new mention
						m.setId(priorEvent.id + "-" + priorEvent.mentions.size());
					}
					newEvents.add(priorEvent);
					aceEventNo++;
				}
			}
		}

		return newEvents;
		//aceDoc.events = newEvents;
		//System.out.println("eventCoref: " + aceDoc.events.size() + " events");
	}
	
	private static int RELEVANT_DOCUMENT = 2;
	public ArrayList<AceEvent> collectRelevantEvents (String docId, String type){
		List<String> files = queriesBytype.get(docId).get(type);
		ArrayList<AceEvent> relEvents = new ArrayList<AceEvent> ();
		int size = (files.size()<RELEVANT_DOCUMENT)?files.size():RELEVANT_DOCUMENT;
		for (int i=0;i<size;i++){
			String relDocId = files.get(i);
			ArrayList<AceEvent> ems = getEventsByType(relDocId,type);
			relEvents.addAll(ems);
		}
		return relEvents;
	}
	
	public ArrayList microClustering(AceDocument aceDoc, Document doc,String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		ArrayList<AceEvent> newEvents = new ArrayList<AceEvent>();

		int aceEventNo = 1;

		HashMap<String, ArrayList> eventMap = getEventsByTypes(events, docId);
		for (String eventType : eventMap.keySet()) {
			ArrayList<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1&&!EXCLUDE_SINGLETONS) {
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				newEvents.add(e);
				aceEventNo++;
			} 
			else if (v.size() > 1){
				ArrayList<AceEvent> relEvents = collectRelevantEvents(docId, eventType);
				
				ArrayList<AceEvent> totalEvents = new ArrayList<AceEvent>(relEvents);
				totalEvents.addAll(v);
				
				DoubleMatrix2D w = null;
				//if (choice == 0)
				w = csimModel(totalEvents, doc);
				/*else if (choice == 1)
					w = csimFormula(v, doc, relations);
				else if (choice == 2)
					w = csimOverlap(v, doc, relations);
				else if (choice == 3)
					w = csimTfIdf(v, doc, relations,docId);*/
				SpectralClusterer sc = new SpectralClusterer();
				int[][] p = null;
				try {
					p = sc.buildClusterer(w, COREF_THRESHOLD);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (p == null){
					System.err.println("Error in graph clustering...");
					return newEvents; 
					//System.exit(-1);
				}
				for (int i = 0; i < p.length; i++) {
					AceEvent firstEvent = null;
					int index = 0;
					for (int j = 0; j < p[i].length; j++) {
						AceEvent priorEvent = totalEvents.get(p[i][j]);
						if (v.contains(priorEvent)){
							firstEvent = priorEvent;
							index = j;
							break;
						}
					}
					if (firstEvent!=null){
						String eventId = aceDoc.docID + "-EV" + aceEventNo;
						firstEvent.setId(eventId);
						AceEventMention m0 = (AceEventMention) firstEvent.mentions.get(0);
						m0.setId(firstEvent.id + "-1");
						for (int j = index+1; j < p[i].length; j++) {
							AceEvent event = totalEvents.get(p[i][j]);
							if (v.contains(event)){
								firstEvent.arguments = mergeArguments(event.arguments,firstEvent.arguments);
								AceEventMention m = (AceEventMention) event.mentions.get(0);
								firstEvent.addMention(m);
								// fix id for new mention
								m.setId(firstEvent.id + "-"	+ firstEvent.mentions.size());
							}
						}
						newEvents.add(firstEvent);
						aceEventNo++;
					}
				}
			}
		}

		return newEvents;
		//aceDoc.events = newEvents;
		//System.out.println("eventCoref: " + aceDoc.events.size() + " events");
	}
	
	
	protected boolean useSparseMatrix = false;
	public DoubleMatrix2D csimModel(ArrayList<AceEvent> v, Document doc) {
		int n = v.size();
		DoubleMatrix2D w;
		if (useSparseMatrix)
			w = DoubleFactory2D.sparse.make(n, n);
		else
			w = DoubleFactory2D.dense.make(n, n);
		for (int i = 0; i < n; i++) {
			AceEvent e1 = v.get(i);
			AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
		
			for (int j = i; j < n; j++) {
				AceEvent e2 = v.get(j);
				AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
				
				Datum d = graphCorefFeatures(e1, e2, doc);
				double prob = corefModel.eval(d.toArray())[corefModel
						.getIndex("coref")];

				w.set(i, j, prob);
				w.set(j, i, prob);
			}
		} 
		return w;
	}

	ArrayList<AceEvent> getEventsByType(String docId, String type) {
		int aceEventNo = 1;
		ArrayList newEvents = new ArrayList();
		String textFile = relSrcDir + relMap.get(docId);
		String xmlFile = relApfDir + relMap.get(docId).replaceFirst(".sgm", ".sgm.apf");
		
		int pos = xmlFile.lastIndexOf("/");
		String apfDir = xmlFile.substring(0,pos);
		String dtd = apfDir+"/apf.v5.1.1.dtd";
		File dtdFile = new File(dtd);
		if (!dtdFile.exists()){
			copydtd(dtdfile,dtd);
		}
		
		ExternalDocument doc = new ExternalDocument("sgml", textFile);
		AceDocument aceDoc = new AceDocument(textFile, xmlFile);
		
		for (int i = 0; i < aceDoc.events.size(); i++) {
			AceEvent event = aceDoc.events.get(i);

			if (!event.subtype.equals(type))
				continue;
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
	
	
	ArrayList maketSingtonEvents(AceDocument aceDoc, String docId) {
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

	static List<String> training = new ArrayList<String>();
	static List<String> developing = new ArrayList<String>();
	static List<String> testing = new ArrayList<String>();

	public void train() throws IOException {
		for (int i = 0; i < training.size(); i++) {
			String filename = training.get(i);

			String textFile = srcDir + filename;
			String xmlFile = apfDir + filename.replaceFirst(".sgm", ".apf.xml");
			String docId = filename.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();

			Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			if (approach.contains("aggr"))
				aggrTrain(doc, aceDoc, docId);
			else if (approach.contains("graph"))
				graphTrain(doc, aceDoc, docId);
		}

	}

	private void graphTrain(Document doc, AceDocument aceDoc, String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);

		// aceDoc.events = events;

		int aceEventNo = 1;
		HashMap<String, ArrayList> eventMap = getEventsByTypes(events, docId);

		for (String eventType : eventMap.keySet()) {
			ArrayList<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1) {
				continue;
			} else {
				for (int i = 0; i < v.size(); i++) {
					AceEvent e1 = v.get(i);

					for (int j = i + 1; j < v.size(); j++) {
						AceEvent e2 = v.get(j);

						Datum d = graphCorefFeatures(e1, e2, doc);

						d.setOutcome(isCorefEvent(e1, e2, aceDoc.events) == true ? "coref"
								: "dontcoref");
						corefFeatureWriter.println(d.toString());
					}
				}
			}
		}
	}

	private boolean isCorefEvent(AceEvent e1, AceEvent e2, ArrayList keyEvents) {
		AceEvent key1 = findEvent(e1, keyEvents);
		AceEvent key2 = findEvent(e2, keyEvents);
		if (key1 == null || key2 == null || !key1.id.equals(key2.id))
			return false;
		else
			return true;
	}

	private AceEvent findEvent(AceEvent event, ArrayList keyEvents) {
		AceEventMention mention = (AceEventMention) event.mentions.get(0);
		for (int i = 0; i < keyEvents.size(); i++) {
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			ArrayList keyMentions = keyEvent.mentions;
			for (int j = 0; j < keyMentions.size(); j++) {
				AceEventMention keyMention = (AceEventMention) keyMentions
						.get(j);
				Span keyExtent = keyMention.jetExtent;
				if (mention.anchorExtent.within(keyExtent)
						&& event.type.equals(keyEvent.type)
						&& event.subtype.equals(keyEvent.subtype)) {

					return keyEvent;
				}
			}
		}
		return null;
	}

	public HashMap<String, ArrayList> getEventsByTypes(ArrayList events, String docID) {
		HashMap eventTypeMap = new HashMap();
		int aceEventNo = 1;
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			String eventId = docID + "-EV" + aceEventNo;
			event.setId(eventId);
			aceEventNo++;
			if (!eventTypeMap.containsKey(event.subtype)) {
				eventTypeMap.put(event.subtype, new ArrayList());
			}
			ArrayList v = (ArrayList) eventTypeMap.get(event.subtype);
			v.add(event);
			eventTypeMap.put(event.subtype, v);

		}
		return eventTypeMap;
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
			GISModel m = (GISModel) new SuffixSensitiveGISModelReader(f)
					.getModel();
			System.err.println("GIS model " + f.getName() + " loaded.");
			return m;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return null; // required by compiler
		}
	}

	private void aggrTrain(Document doc, AceDocument aceDoc, String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);

		// system-generated event list
		ArrayList systemEvents = new ArrayList();
		
		HashMap keyIdToSystemEventMap = new HashMap();

		for (int i = 0; i < events.size(); i++) {
			String eventId = docId + "-EV" + (i + 1);
			AceEvent newEvent = (AceEvent) events.get(i);
			newEvent.setId(eventId);
			Span anchorExtent = newEvent.mentions.get(0).anchorExtent;
			AceEvent keyEvent = findEvent(anchorExtent, newEvent, aceDoc.events);
			String keyEventId = keyEvent.id;
			// determine which system event it should be folded into, if any
			Integer I = (Integer) keyIdToSystemEventMap.get(keyEventId);
			int systemEventIndex = (I == null) ? -1 : I.intValue();

			for (int iEvent = 0; iEvent < systemEvents.size(); iEvent++) {
				AceEvent priorEvent = (AceEvent) systemEvents.get(iEvent);
				if (!priorEvent.subtype.equals(newEvent.subtype))
					continue;
				// if same type/subtype, generate training example
				// (with outcome = whether it belongs to this systemEvent)
				Datum d = aggrCorefFeatures(priorEvent, newEvent, events, doc);
				d.setOutcome((iEvent == systemEventIndex) ? "merge"
						: "dontMerge");
				corefFeatureWriter.println(d.toString());
			}
			// if it should be folded in, do so; else create new event
			if (systemEventIndex >= 0) {
				// fold event into prior event
				AceEvent priorEvent = (AceEvent) systemEvents
						.get(systemEventIndex);
				priorEvent.arguments = mergeArguments(newEvent.arguments,
						priorEvent.arguments);
				AceEventMention m = (AceEventMention) newEvent.mentions.get(0);
				priorEvent.addMention(m);
				m.setId(priorEvent.id + "-" + priorEvent.mentions.size());
			} else {
				systemEvents.add(newEvent);
				keyIdToSystemEventMap.put(keyEventId,
						new Integer(systemEvents.size() - 1));

			}
		}
	}

	private AceEvent findEvent(Span anchorExtent, AceEvent event,
			ArrayList keyEvents) {
		AceEventMention mention = (AceEventMention) event.mentions.get(0);
		for (int i = 0; i < keyEvents.size(); i++) {
			AceEvent keyEvent = (AceEvent) keyEvents.get(i);
			ArrayList keyMentions = keyEvent.mentions;
			for (int j = 0; j < keyMentions.size(); j++) {
				AceEventMention keyMention = (AceEventMention) keyMentions
						.get(j);
				Span keyExtent = keyMention.jetExtent;
				if (anchorExtent.within(keyExtent)
						&& event.type.equals(keyEvent.type)
						&& event.subtype.equals(keyEvent.subtype)) {

					return keyEvent;
				}
			}
		}
		return null;
	}

	private ArrayList mergeArguments(ArrayList args1, ArrayList args2) {
		ArrayList result = new ArrayList(args1);
		nextarg: for (int i = 0; i < args2.size(); i++) {
			AceEventArgument arg2 = (AceEventArgument) args2.get(i);
			String role2 = arg2.role;
			String id2 = arg2.value.id;
			for (int j = 0; j < args1.size(); j++) {
				AceEventArgument arg1 = (AceEventArgument) args1.get(j);
				String role1 = arg1.role;
				String id1 = arg1.value.id;
				if (role1.equals(role2) && id1.equals(id2))
					continue nextarg;
			}
			result.add(arg2);
		}
		return result;
	}

	static String getAnchorCat(Span anchorSpan, Document doc) {
		int posn = anchorSpan.start();
		Vector constits = doc.annotationsAt(posn, "constit");
		if (constits != null) {
			for (int i = 0; i < constits.size(); i++) {
				Annotation constit = (Annotation) constits.get(i);
				String cat = (String) constit.get("cat");
				if (cat == "n")
					cat += "_" + (String) constit.get("number");
				return cat;

			}
		}
		return "";

	}

	static int getEventId(AceEventMention mention, ArrayList events) {
		for (int i = 0; i < events.size(); i++) {
			AceEvent e = (AceEvent) events.get(i);
			if (e.mentions.get(0).anchorExtent.equals(mention.anchorExtent)) {
				return i;
			}
		}
		return -1;
	}

	private Datum aggrCorefFeatures(AceEvent priorEvent, AceEvent event,
			ArrayList events, Document doc) {
		Datum d = new Datum();
		AceEventMention lastMentionPriorEvent = (AceEventMention) priorEvent.mentions
				.get(priorEvent.mentions.size() - 1);
		AceEventMention lastMentionOfEvent = (AceEventMention) event.mentions
				.get(event.mentions.size() - 1);
		String lastPOS = getAnchorCat(lastMentionPriorEvent.anchorExtent, doc);
		String POS = getAnchorCat(lastMentionOfEvent.anchorExtent, doc);

		// baseline features
		// type/subtype of events
		d.addFV("subtype", event.subtype);
		// normalized anchor
		// d.addFV ("anchor", anchor);
		// pos pair of last mention and current mention
		if (lastPOS.equals(POS))
			d.addFV("pos", "1");
		else
			d.addFV("pos", "0");
		d.addFV("pospair", lastPOS + ":" + POS);

		// matching anchors
		/*
		 * if
		 * (lastMentionOfEvent.anchorText.equals(lastMentionPriorEvent.anchorText
		 * )) d.addF("anchorMatch");
		 */
		for (int i = 0; i < priorEvent.mentions.size(); i++) {
			if (priorEvent.mentions.get(i).anchorText
					.equals(lastMentionOfEvent.anchorText)) {
				d.addF("anchorMatch");
				break;
			}
		}
		// matching stem of anchors
		PorterStemmer ps = new PorterStemmer();
		for (int i = 0; i < priorEvent.mentions.size(); i++) {
			if (ps.stem(priorEvent.mentions.get(i).anchorText).equals(
					ps.stem(lastMentionOfEvent.anchorText))) {
				d.addF("anchorStemMatch");
				break;
			}
		}

		// quantized anchor similarity
		SimilarityAssessor _assessor = new SimilarityAssessor();
		double maxSim = -1;
		for (int i = 0; i < priorEvent.mentions.size(); i++) {
			double sim = _assessor.getSimilarity(
					ps.stem(priorEvent.mentions.get(i).anchorText),
					ps.stem(lastMentionOfEvent.anchorText));
			if (sim > maxSim)
				maxSim = sim;
		}
		if (maxSim >= 0)
			d.addFV("anchorsim", Integer.toString((int) (maxSim * 5)));

		if (featureset == 0)
			return d;

		// feature set 1
		// distance (100's of chars, up to 10)
		/*int posnPriorEvent = lastMentionPriorEvent.anchorExtent.start();
		int posnEvent = lastMentionOfEvent.anchorExtent.start();
		int tokendist = posnEvent - posnPriorEvent;

		// token_dist:how many tokens two mentions are apart
		d.addFV("tokendist", Integer.toString(Math.min(tokendist / 100, 9)));

		// sent_dist:how many sentences two mentions are apart
		// sentence distance
		int sen1 = findSentenceId(doc, lastMentionPriorEvent.anchorExtent);
		int sen2 = findSentenceId(doc, lastMentionOfEvent.anchorExtent);
		if (sen1 != -1 && sen2 != -1) {
			d.addFV("sendist", Integer.toString(sen2 - sen1));
		}

		// how many mentions in between the two mentions in question (quantized)
		int ev1 = getEventId(lastMentionPriorEvent, events);
		int ev2 = getEventId(lastMentionOfEvent, events);
		if (ev1 != -1 && ev2 != -1) {
			d.addFV("memdist", Integer.toString(ev2 - ev1));
		}*/
		if (featureset == 1)
			return d;

		// feature set: arguments
		// check whether there are any roles that can be matched
		// if yes, how many roles that can be matched
		// check whether there are any ids that can be matched
		// if yes, how many ids that can be matched

		ArrayList<AceEventArgument> priorArgs = priorEvent.arguments;
		ArrayList<AceEventArgument> args = event.arguments;

		TreeSet<String> priorRoles = new TreeSet<String>();
		TreeSet<String> priorIds = new TreeSet<String>();
		TreeSet<String> roles = new TreeSet<String>();
		TreeSet<String> ids = new TreeSet<String>();

		for (int i = 0; i < priorArgs.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) priorArgs.get(i);

			String role = arg1.role;
			priorRoles.add(role);
			String id = arg1.value.id;
			priorIds.add(id);
		}

		for (int i = 0; i < args.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) args.get(i);

			String role = arg1.role;
			roles.add(role);
			String id = arg1.value.id;
			ids.add(id);
		}

		TreeSet<String> tmp = new TreeSet<String>(priorRoles);
		tmp.removeAll(roles);
		// how many unique roles are in prior roles
		d.addFV("priorRoles", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(priorIds);
		tmp.removeAll(ids);
		// how many unique ids are in prior ids
		d.addFV("priorIds", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(roles);
		tmp.removeAll(priorRoles);
		// how many unique roles are in current roles
		d.addFV("curRoles", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(ids);
		tmp.removeAll(priorIds);
		// how many unique ids are in current ids
		d.addFV("curIds", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(priorRoles);
		tmp.retainAll(roles);
		// how many common ids are in current ids and prior ids
		d.addFV("commonRoles", Integer.toString(tmp.size()));

		if (tmp.size() > 0) {
			d.addFV("hasCommonRole", "1");
		}

		tmp = new TreeSet<String>(priorIds);
		tmp.retainAll(ids);
		// how many common ids are in current ids and prior ids
		d.addFV("commonIds", Integer.toString(tmp.size()));

		if (tmp.size() > 0) {
			d.addFV("hasCommonId", "1");
		}

		if (featureset == 2)
			return d;

		// feature set : event attributes

		/*
		 * d.addFV("modality", event.modality); d.addFV("polarity",
		 * event.polarity); d.addFV("genericity", event.genericity);
		 * d.addFV("tense", event.tense); if
		 * (!event.modality.equals(priorEvent.modality))
		 * d.addFV("modalityConflict", "T"); if
		 * (!event.polarity.equals(priorEvent.polarity))
		 * d.addFV("polarityConflict", "T"); if
		 * (!event.genericity.equals(priorEvent.genericity))
		 * d.addFV("genericityConflict", "T"); if
		 * (!event.tense.equals(priorEvent.tense)) d.addFV("tenseConflict",
		 * "T");
		 */
		return d;
	}

	private Datum graphCorefFeatures(AceEvent e1, AceEvent e2, Document doc) {
		Datum d = new Datum();
		AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
		AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
		String pos1 = getAnchorCat(m1.anchorExtent, doc);
		String pos2 = getAnchorCat(m2.anchorExtent, doc);

		PorterStemmer ps = new PorterStemmer();
		String stem1 = ps.stem(m1.anchorText.toLowerCase());
		String stem2 = ps.stem(m2.anchorText.toLowerCase());

		// baseline features
		// type/subtype of events
		d.addFV("subtype", e2.subtype);

		d.addFV("pospair", pos1 + ":" + pos2);

		// matching anchors
		/*
		 * if
		 * (lastMentionOfEvent.anchorText.equals(lastMentionPriorEvent.anchorText
		 * )) d.addF("anchorMatch");
		 */

		if (m1.anchorText.equals(m2.anchorText)) {
			d.addF("anchorMatch");
		}
		// matching stem of anchors

		if (stem1.equals(stem2)) {
			d.addF("anchorStemMatch");
		}

		// quantized anchor similarity
		SimilarityAssessor _assessor = new SimilarityAssessor();
		double sim = _assessor.getSimilarity(stem1, stem2);

		if (sim >= 0)
			d.addFV("sim", Integer.toString((int) (sim * 5)));

		if (featureset == 0)
			return d;

		// feature set 1
		/*int posnPriorEvent = m1.anchorExtent.start();
		int posnEvent = m2.anchorExtent.start();
		int tokendist = posnEvent - posnPriorEvent;

		// token_dist:how many tokens two mentions are apart
		d.addFV("tokendist", Integer.toString(Math.min(tokendist / 100, 9)));

		// sent_dist:how many sentences two mentions are apart
		// sentence distance
		int sen1 = findSentenceId(doc, m1.anchorExtent);
		int sen2 = findSentenceId(doc, m2.anchorExtent);
		if (sen1 != -1 && sen2 != -1) {
			d.addFV("sendist", Integer.toString(Math.min(sen2 - sen1, 10)));
		}

		int ev1 = Integer.parseInt(e1.id.substring(e1.id.indexOf("-EV") + 3));
		int ev2 = Integer.parseInt(e2.id.substring(e2.id.indexOf("-EV") + 3));

		d.addFV("mendist", Integer.toString(Math.min(ev2 - ev1, 9)));*/

		if (featureset == 1)
			return d;

		// feature set: arguments
		// check whether there are any roles that can be matched
		// if yes, how many roles that can be matched
		// check whether there are any ids that can be matched
		// if yes, how many ids that can be matched

		ArrayList<AceEventArgument> priorArgs = e1.arguments;
		ArrayList<AceEventArgument> args = e2.arguments;

		TreeSet<String> priorRoles = new TreeSet<String>();
		TreeSet<String> priorIds = new TreeSet<String>();
		TreeSet<String> roles = new TreeSet<String>();
		TreeSet<String> ids = new TreeSet<String>();

		for (int i = 0; i < priorArgs.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) priorArgs.get(i);

			String role = arg1.role;
			priorRoles.add(role);
			String id = arg1.value.id;
			priorIds.add(id);
		}

		for (int i = 0; i < args.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) args.get(i);

			String role = arg1.role;
			roles.add(role);
			String id = arg1.value.id;
			ids.add(id);
		}

		TreeSet<String> tmp = new TreeSet<String>(priorRoles);
		tmp.removeAll(roles);
		// how many unique roles are in prior roles
		d.addFV("priorRoles", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(priorIds);
		tmp.removeAll(ids);
		// how many unique ids are in prior ids
		d.addFV("priorIds", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(roles);
		tmp.removeAll(priorRoles);
		// how many unique roles are in current roles
		d.addFV("curRoles", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(ids);
		tmp.removeAll(priorIds);
		// how many unique ids are in current ids
		d.addFV("curIds", Integer.toString(tmp.size()));

		tmp = new TreeSet<String>(priorRoles);
		tmp.retainAll(roles);
		// how many common ids are in current ids and prior ids
		d.addFV("commonRoles", Integer.toString(tmp.size()));

		if (tmp.size() > 0) {
			d.addFV("hasCommonRole", "1");
		}

		tmp = new TreeSet<String>(priorIds);
		tmp.retainAll(ids);
		// how many common ids are in current ids and prior ids
		d.addFV("commonIds", Integer.toString(tmp.size()));

		if (tmp.size() > 0) {
			d.addFV("hasCommonId", "1");
		}

		if (featureset == 2)
			return d;

		// feature set : event attributes

		/*
		 * d.addFV("modality", event.modality); d.addFV("polarity",
		 * event.polarity); d.addFV("genericity", event.genericity);
		 * d.addFV("tense", event.tense); if
		 * (!event.modality.equals(priorEvent.modality))
		 * d.addFV("modalityConflict", "T"); if
		 * (!event.polarity.equals(priorEvent.polarity))
		 * d.addFV("polarityConflict", "T"); if
		 * (!event.genericity.equals(priorEvent.genericity))
		 * d.addFV("genericityConflict", "T"); if
		 * (!event.tense.equals(priorEvent.tense)) d.addFV("tenseConflict",
		 * "T");
		 */
		return d;
	}


	void loadFileList(String filename, List<String> files)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		while ((line = reader.readLine()) != null) {
			if (!files.contains(line))
				files.add(line);
		}

		reader.close();
	}

	TreeMap<String,String> relMap = new TreeMap<String,String>();
	void loadRelMapfile(String filename)
			throws NumberFormatException, IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String line;

		while ((line = reader.readLine()) != null) {
			String [] items = line.split("\t");
			relMap.put(items[0], items[1]);
		}

		reader.close();
	}

	

	/**
	 * performs coreference on the events in an Ace document. On entry, the
	 * AceDocument <CODE>aceDoc</CODE> should have a set of events each with a
	 * single mention. The event mentions which are believed to corefer are
	 * combined into a single event.
	 */

	public ArrayList aggrClustering(AceDocument aceDoc, Document doc, String docId) {
		ArrayList events = maketSingtonEvents(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		ArrayList<AceEvent> newEvents = new ArrayList<AceEvent>();

		int aceEventNo = 1;

		HashMap<String, ArrayList> eventMap = getEventsByTypes(events, docId);
		for (String eventType : eventMap.keySet()) {
			ArrayList<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1&&!EXCLUDE_SINGLETONS) {
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				newEvents.add(e);
				aceEventNo++;
			} 
			else if (v.size() > 1){
				for (int i = 0; i < v.size(); i++) {
					AceEvent event = (AceEvent) v.get(i);
					// is there a prior event on newEvents of the same type
					// such that the arguments are compatible?
					int priorEventIndex = -1;
					double priorEventProb = 0.;
					for (int j = newEvents.size() - 1; j >= 0; j--) {
						AceEvent newEvent = (AceEvent) newEvents.get(j);
						if (event.type.equals(newEvent.type)
								&& event.subtype.equals(newEvent.subtype)) {
							AceEventMention m = (AceEventMention) event.mentions.get(0);
		
							Datum d = aggrCorefFeatures(newEvent, event, events, doc);
							double prob = corefModel.eval(d.toArray())[corefModel
									.getIndex("merge")];
							if (prob > COREF_THRESHOLD && prob > priorEventProb) {
								priorEventIndex = j;
								priorEventProb = prob;
							}
						}
					}
					if (priorEventIndex >= 0) {
						AceEvent priorEvent = (AceEvent) newEvents.get(priorEventIndex);
						priorEvent.arguments = mergeArguments(event.arguments,
								priorEvent.arguments);
						AceEventMention m = (AceEventMention) event.mentions.get(0);
						priorEvent.addMention(m);
						// fix id for new mention
						m.setId(priorEvent.id + "-" + priorEvent.mentions.size());
					} else {
						// if not, put event on newEvents
						String eventId = aceDoc.docID + "-EV" + aceEventNo;
						event.setId(eventId);
						aceEventNo++;
						newEvents.add(event);
					}
				}
			}
		}
		return newEvents;
		//System.out.println("eventCoref: " + aceDoc.events.size() + " events");
	}

	static void copydtd (String src, String des){
		try {
	        // Create channel on the source
	        FileChannel srcChannel = 
	          new FileInputStream(src).getChannel();
	    
	        // Create channel on the destination
	        FileChannel dstChannel = 
	          new FileOutputStream(des).getChannel();
	    
	        // Copy file contents from source to destination
	        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
	    
	        // Close the channels
	        srcChannel.close();
	        dstChannel.close();
	        
	    } catch (IOException e) {
	    }
	}
	public static void main(String[] args) throws IOException {
		ETCoref et = new ETCoref();

		et.initializeFromConfig(new File(args[0]));
//		et.createDataSet();

		if (approach.contains("micro")){
			et.loadRelMapfile(relmapfile);
			et.loadQueriesBytype();
		}
		
		for (int i = 0; i < totalDatasets; i++) {
			String path = "dataset/exp" + expID + "/dataset" + i + "/";
			File datasetDir = new File(path);
			if (!datasetDir.exists()) {
				System.err.println("Folder of dataset" + i
						+ " does not exist, run \"CreateDataset\" first.");
				return;
			}
			System.out.println("Processing dataset" + i);

			for (int j = 0; j < folds; j++) {
				String foldpath = path + "fold" + j + "/";
				File foldDir = new File(foldpath);
				if (!foldDir.exists()) {
					System.err.println("Folder of fold" + j
							+ " does not exist, run \"CreateDataset\" first.");
					return;
				}
				System.out.println("dataset " + i + ":fold" + j);
				training = new ArrayList<String>();
				developing = new ArrayList<String>();
				testing = new ArrayList<String>();

				et.loadFileList(foldpath + "testing.list", testing);
				et.loadFileList(foldpath + "training.list", training);
				et.loadFileList(foldpath + "developing.list", developing);
				
				String expPath = foldpath + approach + "/";
				File expPathFile = new File(expPath);
				if (!expPathFile.exists()) {
					expPathFile.mkdirs();
				}
				String dtd = expPath+"apf.v5.1.1.dtd";
				File dtdFile = new File(dtd);
				if (!dtdFile.exists()){
					copydtd(dtdfile,dtd);
				}
				
				if (approach.contains("aggr") || approach.contains("graph")) {
					System.out.println("training coref model for "+approach);
					et.trainCorefModel(foldpath);
					
					corefModel = loadClassifierModel(corefModelFileName);
					
					System.out.println("tagging on developing set...");
					double best_threshold = 0;
					double max = 0;
					for (COREF_THRESHOLD = 0; COREF_THRESHOLD < 1.0; COREF_THRESHOLD = COREF_THRESHOLD + 0.05) {
						et.tag(expPath,developing);
						System.out.print("COREF_THRESHOLD="+COREF_THRESHOLD+":");
						if (evaluationMetric.equals("MUC")){
							MUCCorefEval e = new MUCCorefEval();
							e.evalCoref(srcDir,apfDir, expPath, developing);
							if (max<e.F){
								max = e.F;
								best_threshold = COREF_THRESHOLD;
							}
							System.out.println(Double.toString(e.precision) + "\t" + Double.toString(e.recall) + "\t"+ Double.toString(e.F) + "\n");
						}
						if (evaluationMetric.equals("BCube")){
							BCubeCorefEval e = new BCubeCorefEval();
							e.evalCoref(srcDir,apfDir, expPath, developing);
							if (max<e.F){
								max = e.F;
								best_threshold = COREF_THRESHOLD;
							}
							System.out.println(Double.toString(e.precision) + "\t" + Double.toString(e.recall) + "\t"+ Double.toString(e.F) + "\n");
						}
						if (evaluationMetric.equals("CEAF")){
							CEAFCorefEval e = new CEAFCorefEval();
							e.evalCoref(srcDir,apfDir, expPath, developing);
							if (max<e.F){
								max = e.F;
								best_threshold = COREF_THRESHOLD;
							}
							System.out.println(Double.toString(e.precision) + "\t" + Double.toString(e.recall) + "\t"+ Double.toString(e.F) + "\n");
						}
					}
					
					System.out.println("Best threshold="+best_threshold+","+"F="+max);
					System.out.println("tagging on testing set...");
					
					COREF_THRESHOLD=best_threshold;
				}
				et.tag(expPath,testing);
				if (evaluationMetric.equals("MUC")){
					MUCCorefEval e = new MUCCorefEval();
					e.evalCoref(srcDir,apfDir, expPath, testing);
					
					System.out.println(Double.toString(e.precision) + "\t" + Double.toString(e.recall) + "\t"+ Double.toString(e.F) + "\n");
				}
				if (evaluationMetric.equals("BCube")){
					BCubeCorefEval e = new BCubeCorefEval();
					e.evalCoref(srcDir,apfDir, expPath, testing);
				
					System.out.println(Double.toString(e.precision) + "\t" + Double.toString(e.recall) + "\t"+ Double.toString(e.F) + "\n");
				}
				if (evaluationMetric.equals("CEAF")){
					CEAFCorefEval e = new CEAFCorefEval();
					e.evalCoref(srcDir,apfDir, expPath, testing);
					System.out.println(Double.toString(e.precision) + "\t" + Double.toString(e.recall) + "\t"+ Double.toString(e.F) + "\n");
				}
			}
		}
	}

	private void trainCorefModel(String path) throws IOException {
		corefFeatureFileName = path + approach + "-featurefile";
		corefModelFileName = path + approach + "-modelfile";
		// set coref model
		corefFeatureWriter = new PrintStream(new FileOutputStream(
				corefFeatureFileName));
		// train coref model on even documents, using models from odd documents
		// processOnlyEvenDocuments = true;
		train();

		// build coref model
		corefFeatureWriter.close();
		buildClassifierModel(corefFeatureFileName, corefModelFileName);
	}
}
