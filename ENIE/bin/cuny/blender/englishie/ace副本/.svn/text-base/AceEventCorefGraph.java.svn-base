//Filename:     AceEventCorefGraph.java
//Copyright:    2009
//Author:       Zheng Chen
//Description:  
//	Implement an spectral clustering algorithm for event coreference resolution,
//	using perfect event mentions annotated in the corpus

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.algorithm.clustering.SpectralClusterer;
import cuny.blender.englishie.algorithm.tfidf.TfIdf;
import cuny.blender.englishie.evaluation.event.BCubeCorefEval;
import cuny.blender.englishie.evaluation.event.CEAFCorefEval;
import cuny.blender.englishie.evaluation.event.CEAFCorefEval1;
import cuny.blender.englishie.evaluation.event.MUCCorefEval;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.PorterStemmer;
import cuny.blender.englishie.nlp.lex.Stemmer;
import cuny.blender.englishie.nlp.parser.*;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;
import cuny.blender.englishie.nlp.wordnet.similarity.SimilarityAssessor;


import opennlp.maxent.*;
import opennlp.maxent.io.*;
import opennlp.model.EventStream;



import cern.colt.matrix.*;

/**
 * assigns ACE events to a Document, given the entities, times, and values.
 */

public class AceEventCorefGraph {

	// parameters
	//  use statistical model to gather arguments
	static boolean useArgumentModel = true;
	//  events below this probability are dropped
	static double EVENT_PROBABILITY_THRESHOLD = 0.10;
	//  args below this prob are never added to an event, and not used
	//  in estimating event probability
	static double MIN_ARG_PROBABILITY = 0.10;
	//  args below this probability are dropped from the final event
	static double ARGUMENT_PROBABILITY_THRESHOLD = 0.35;
	// minimal confidence for an arg to be used for coref determination
	static double COREF_CONFIDENCE = 0.10;

	static double CLUSTER_THRESHOLD = 0.85;

	// mapping from an anchor to a list of EventPatterns
	TreeMap<String, List> anchorMap;

	PrintStream eventFeatureWriter;
	// PrintStream evTypeFeatureWriter;
	PrintStream argFeatureWriter;
	PrintStream roleFeatureWriter;
	PrintStream corefFeatureWriter;

	static GISModel eventModel;
	// static GISModel evTypeModel;
	static GISModel argModel;
	static GISModel roleModel;
	static GISModel corefModel;
	static String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	static String ace = home + "source/";
	static String aceModels = home + "model/";
	static String triplesDir =
			ace + "011306-fast-tuples/";    // new Charniak parser, fast mode
	static String triplesSuffix =
			".sent.txt.ns-2005-fast-ace-n-tuple92";
		
	// use predicate-argument roles from GLARF
	static boolean usePA = false;
	// use parser (pre-parsed text), else use chunker
	static boolean useParser = false;
	static boolean processOnlyOddDocuments = false;
	static boolean processOnlyEvenDocuments = false;
	static boolean useStem = true;
	static Stemmer ps = Stemmer.getDefaultStemmer();
	
	static int FEATURE_SET = 2;
	
	public AceEventCorefGraph() {
		anchorMap = new TreeMap<String, List>();
	}

	/**
	 *  trains an event tagger from a set of text and APF files.
	 *  @param fileList  a list of text file names, one per line.
	 *                   The APF file names are obtained by replacing
	 *                   'sgm' by 'apf.xml'.
	 */

	public void train (String fileList, int pass) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			// if (docCount > 10) break;
			if (processOnlyOddDocuments && docCount%2 == 0) continue;
			if (processOnlyEvenDocuments && docCount%2 == 1) continue;
			System.out.println ("\nTrain file " + Integer.toString(docCount)+":"+currentDocPath);
			String textFile = ace + (useParser ? "perfect-parses/" : "") + currentDocPath;
			String xmlFile = ace + currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm","");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			Resolve.ACE = true;
			Control.processDocument (doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			if (pass == 0)
				acquirePatterns (doc, aceDoc, docId);
			else if (pass == 1)
				evaluatePatterns (doc, aceDoc, docId);
			else if (pass == 2)
				trainEventModel (doc, aceDoc, docId);
			else /* pass == 3 */
				trainCorefModel (doc, aceDoc, docId);
		}
		reader.close();
	}

	String[] patternTypeList = {"CHUNK", "SYNTAX", "PA"};

	/**
	 *  trains the tagger from document 'doc' and corresponding AceDocument
	 *  (APF file) aceDoc.
	 */

	public void acquirePatterns (Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations (triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		for (int i=0; i<events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			ArrayList mentions = event.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceEventMention m = (AceEventMention) mentions.get(j);
				//System.out.println ("\nProcessing mention " + m.id + " = " + m.text);
				// .. get anchor extent and text
				Span anchorExtent = m.anchorJetExtent;
				String anchor =
					EventPattern.normalizedAnchor (anchorExtent, m.anchorText, doc, relations);
				// generate patterns
				for (String patternType : patternTypeList) {
					EventPattern ep = new EventPattern (patternType, doc, relations, event, m);
					if (ep.empty()) continue;
					//System.out.println (patternType + " pattern = " + ep);
					addPattern (anchor, ep);
					// try event pattern
					AceEvent builtEvent = ep.match(anchorExtent, anchor, doc, relations, aceDoc);
					if (builtEvent == null)
						System.err.println ("**** match failed ****");
					// else
					 	// System.out.println ("Reconstructed event = " + builtEvent);
					// prepare training data for argument classifier
				}
				if (trainArgModels) {
					trainArgClassifier (event, m, doc, aceDoc, relations);
				}
			}
		}
	}

	private void addPattern (String anchor, EventPattern ep) {
		List patternList = anchorMap.get(anchor);
		if (patternList == null) {
			patternList = new ArrayList();
			anchorMap.put(anchor, patternList);
		}
		if (!patternList.contains(ep))
			patternList.add(ep);
		// --
		String relatedForm = (String) SyntacticRelationSet.nomVmap.get(anchor);
		if (relatedForm != null) {
			EventPattern epClone = new EventPattern(ep);
			epClone.anchor = relatedForm;
			epClone.paths = null;
			patternList = (List) anchorMap.get(relatedForm);
			if (patternList == null) {
				patternList = new ArrayList();
				anchorMap.put(relatedForm, patternList);
			}
			if (!patternList.contains(epClone))
				patternList.add(epClone);
		}
		// ---
	}

	private void addBasicPattern (String anchor, EventPattern ep) {
		List patternList = (List) anchorMap.get(anchor);
		if (patternList == null) {
			patternList = new ArrayList();
			anchorMap.put(anchor, patternList);
		}
		if (!patternList.contains(ep))
			patternList.add(ep);
	}

	/**
	 *  trains two statistical models:
	 *  - argModel to decide whether a mention is an argument of an event
	 *  - roleModel to decide which role the mention should have
	 */

	private void trainArgClassifier (AceEvent event, AceEventMention eventMention,
			Document doc, AceDocument aceDoc, SyntacticRelationSet relations) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor (anchorExtent, anchorText, doc, relations);
		AceMention anchorMention =
			new AceEventAnchor (anchorExtent, anchorExtent, anchorText, doc);
		// find sentence containing anchor
		Annotation sentence = findContainingSentence (doc, anchorExtent);
		if (sentence == null) return;
		Span sentenceSpan = sentence.span();
		// iterate over mentions in sentence
		ArrayList mentions = aceDoc.getAllMentions();
		for (int im=0; im<mentions.size(); im++) {
			AceMention mention = (AceMention) mentions.get(im);
			if (!mention.jetExtent.within(sentenceSpan)) continue;
		// - compute syntactic path
		// - determine if mention has role in event
		  ArrayList arguments = eventMention.arguments;
		  String role = "noArg";
		  for (int ia=0; ia<arguments.size(); ia++) {
		  	AceEventMentionArgument argument = (AceEventMentionArgument) arguments.get(ia);
		  	if (argument.value.equals(mention)) {
		  		role = argument.role;
		  		break;
		  	}
		  }
		  Datum d = argumentFeatures (doc, anchor, event, mention, anchorMention, relations);
			//   outcome = argument role
			if (role == "noArg") {
				d.setOutcome("noArg");
				argFeatureWriter.println(d.toString());
			} else {
				d.setOutcome("arg");
				argFeatureWriter.println(d.toString());
				d.setOutcome (role);
				roleFeatureWriter.println(d.toString());
			}
		}
	}

	private Datum argumentFeatures
	  (Document doc, String anchor, AceEvent event, AceMention mention,
	   AceMention anchorMention, SyntacticRelationSet relations) {
  	String direction;
		ChunkPath cpath;
		// - compute chunk path from anchor
		if (anchorMention.getJetHead().start() < mention.getJetHead().start()) {
			direction = "follow";
			cpath = new ChunkPath (doc, anchorMention, mention);
		} else {
			direction = "precede";
			cpath = new ChunkPath (doc, mention, anchorMention);
		}
		String spath = EventSyntacticPattern.buildSyntacticPath
		  (anchorMention.getJetHead().start(), mention.getJetHead().start(), relations);
		// System.out.println ("spath = " + spath);
		//   build feature entry
		Datum d = new Datum();
		//   = anchor word
		d.addFV ("anchor", anchor);
		//   = event type
		d.addFV ("evType", event.type);
		//   = EDT type of mention
		d.addFV ("menType", mention.getType());
		//   = head of mention (by itself and coupled with event subtype)
		//     (intuition:  US attacks Iraq, bombers kill, etc.)
		String headText = Resolve.normalizeName(mention.getHeadText()).replace(' ','_');
		d.addFV ("arg", headText);
		d.addFV ("evTypeArg", event.subtype + ":" + headText);
		//   = word preceding mention
		int pos = mention.jetExtent.start();
		Annotation token = doc.tokenEndingAt(pos);
		if (token != null) {
			d.addFV ("prevToken", doc.text(token).trim());
			d.addFV ("prevTokenAndType", event.type + "_" + doc.text(token).trim());
		}
		//   = chunk path, direction, distance
		if (cpath == null || cpath.toString() == null)
			d.addFV ("noChunkPath", null);
		else {
			String cpathString = cpath.toString().replace(' ','_');
			d.addFV ("chunkPath", direction + "_" + cpathString);
			d.addFV ("chunkPathAndType", event.type + "_" + direction + "_" + cpathString);
			d.addFV ("dist", Integer.toString(cpath.size()));
		}
		//   = syntactic path
		if (spath == null)
			d.addF ("noSynPath");
		else {
			d.addFV ("synPath", spath);
			d.addFV ("synPathEvType", event.type + "_" + spath);
			d.addFV ("synPathTypes", event.type + "_" + mention.getType() + "_" + spath);
		}
		return d;
	}

	private Annotation findContainingSentence (Document doc, Span span) {
		Vector sentences = doc.annotationsOfType ("sentence");
		if (sentences == null) {
			System.err.println ("findContainingSentence:  no sentences found");
			return null;
		}
		Annotation sentence = null;
		for (int i=0; i<sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			if (span.within(s.span())) {
				sentence = s;
				break;
			}
		}
		if (sentence == null) {
			System.err.println ("findContainingSentence:  can't find sentence with span");
			return null;
		}
		return sentence;
	}

	private int findSentenceId (Document doc, Span span) {
		Vector sentences = doc.annotationsOfType ("sentence");
		if (sentences == null) {
			System.err.println ("findContainingSentence:  no sentences found");
			return -1;
		}
		Annotation sentence = null;
		for (int i=0; i<sentences.size(); i++) {
			Annotation s = (Annotation) sentences.get(i);
			if (span.within(s.span())) {
				return i;
			}
		}
		return -1;
	}
	
	static private void buildClassifierModel (String featureFileName, String modelFileName) {
    boolean USE_SMOOTHING = false;
    boolean PRINT_MESSAGES = true;
    double SMOOTHING_OBSERVATION = 0.1;
		try {
	    FileReader datafr = new FileReader(new File(featureFileName));
	    EventStream es =
				new BasicEventStream(new PlainTextByLineDataStream(datafr));
	    GIS.SMOOTHING_OBSERVATION = SMOOTHING_OBSERVATION;
	    GISModel model = GIS.trainModel(es, 100, 4, USE_SMOOTHING, PRINT_MESSAGES);

	    File outputFile = new File(modelFileName);
	    GISModelWriter writer = new SuffixSensitiveGISModelWriter(model, outputFile);
	    writer.persist();
		} catch (Exception e) {
			System.err.print("Unable to create model due to exception: ");
			System.err.println(e);
		}
   }

	private static GISModel loadClassifierModel (String modelFileName) {
		try {
			File f = new File(modelFileName);
			GISModel m = (GISModel) new SuffixSensitiveGISModelReader(f).getModel();
			System.err.println ("GIS model " + f.getName() + " loaded.");
		  return m;
		} catch (Exception e) {
		  e.printStackTrace();
		  System.exit(0);
		  return null; // required by compiler
		}
	}

	/**
	 *  add arguments to 'event' using classifiers to decide if a mention is
	 *  an argument, and to assign that argument a role.
	 *  <p>
	 *  For each mention in the sentence, determine the probability 'P' 
	 *  that it is an argument of this event and determine its most likely role.  
	 *  If P < MIN_ARG_PROBABILITY, ignore this mention.  If the most likely
	 *  role was filled by pattern matching or is not a valid role for this
	 *  event, ignore this mention.    If there are several mentions which have 
	 *  the same 'most likely role', select the one for which P is highest, 
	 *  and ignore the other mentions.  Add the remaining mentions as arguments
	 *  of the event, subject to the constraint that an entity may only
	 *  appear once as the argument of an event (so that, in particular,
	 *  entities already assigned as arguments by the pattern matcher will
	 *  be skipped here).
	 */

	private void collectArguments (AceEvent event, AceEventMention eventMention,
			Document doc, AceDocument aceDoc, SyntacticRelationSet relations) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor (anchorExtent, anchorText, doc, relations);
		AceMention anchorMention = new AceEventAnchor (anchorExtent,
		  	  eventMention.anchorJetExtent, anchorText, doc);
		// identify roles already filled
		Set rolesFilled = rolesFilledInEvent(event);
		// find sentence containing anchor
		Annotation sentence = findContainingSentence (doc, anchorExtent);
		if (sentence == null) return;
		Span sentenceSpan = sentence.span();
		// get arguments already used
		HashSet argumentsUsed = argumentValues(event);
		// iterate over mentions in sentence
		Map<String, Double> bestRoleProb = new HashMap<String, Double>();
		Map<String, AceMention> bestRoleFiller = new HashMap<String, AceMention>();
		Map<String, Double> bestRoleRoleProb = new HashMap<String, Double>();
		ArrayList mentions = aceDoc.getAllMentions();
		for (int im=0; im<mentions.size(); im++) {
			AceMention mention = (AceMention) mentions.get(im);
			if (!mention.jetExtent.within(sentenceSpan)) continue;
			if (mention.getJetHead().within(anchorExtent)) continue; // Nov. 4 2005
			//   build feature entry
			Datum d = argumentFeatures (doc, anchor, event, mention, anchorMention, relations);
			//   classify:
			//      probability that this is an argument
			//      most likely role assignment
			double argProb = argModel.eval(d.toArray())[argModel.getIndex("arg")];
			String role = roleModel.getBestOutcome(roleModel.eval(d.toArray())).intern();
			double roleProb = roleModel.eval(d.toArray())[roleModel.getIndex(role)];
			// System.out.println ("argProb of " + mention.getHeadText() + " is " + argProb);
			/*  the following code chooses the best valid role
			double[]  roleProbs = roleModel.eval(d.toArray());
			String role = null;
			double best = -1;
			for (int i=0; i<roleModel.getNumOutcomes(); i++) {
				String r = roleModel.getOutcome(i);
				if (roleProbs[i] > best
				    && AceEventArgument.isValid(event.subtype, r, mention)) {
					role = r;
					best = roleProbs[i];
				}
			}
			if (role == null) continue; */
			//      if role already filled, continue
			if (rolesFilled.contains(role)) continue;
			//      if not a valid role for this event type, continue
			if (!AceEventArgument.isValid(event.subtype, role, mention)) continue;
			//      if likely argument, add to event
			if (argProb > MIN_ARG_PROBABILITY) {
				// if this mention has the highest probability of filling this role,
				//    record it
				if (bestRoleProb.get(role) == null ||
				    argProb > bestRoleProb.get(role).doubleValue()) {
					bestRoleProb.put(role, argProb);
					bestRoleRoleProb.put(role, roleProb);
					bestRoleFiller.put(role, mention);
				}
			}
		}
		for (String role : bestRoleFiller.keySet()) {
			AceMention mention = (AceMention) bestRoleFiller.get(role);
			// don't use an argument twice
			AceEventArgumentValue argValue = mention.getParent();
			if (argumentsUsed.contains(argValue)) continue;
			double argProb = bestRoleProb.get(role);
			AceEventMentionArgument mentionArg =
				new AceEventMentionArgument(mention, role);
			mentionArg.confidence = argProb;
			mentionArg.roleConfidence = bestRoleRoleProb.get(role);
			eventMention.arguments.add(mentionArg);
			AceEventArgument eventArg = new AceEventArgument(argValue, role);
			eventArg.confidence = argProb;
			event.arguments.add(eventArg);
			// System.out.println ("Adding " + mention.getHeadText() + " in role " +
			//                      role + " with prob " + argProb);
			argumentsUsed.add(argValue);
		}
	}
	
	

	public void report (String reportFile) throws IOException {
		PrintWriter reportWriter = new PrintWriter (new FileWriter (reportFile));
		Set anchors = anchorMap.keySet();
		Iterator iter = anchors.iterator();
		while (iter.hasNext()) {
			String anchor = (String) iter.next();
			reportWriter.println("\n" + anchor + " ================================");
			List patterns = (List) anchorMap.get(anchor);
			for (int j=0; j<patterns.size(); j++) {
				EventPattern ep = (EventPattern) patterns.get(j);
				reportWriter.println(ep.toString());
			}
		}
		reportWriter.close();
	}

	/**
	 *  write out all the patterns to file 'fileName' in a form which
	 *  can be reloaded by the 'load' method.
	 */

	public void save (String fileName) throws IOException {
		PrintWriter writer = new PrintWriter (new FileWriter (fileName));
		Set anchors = anchorMap.keySet();
		Iterator iter = anchors.iterator();
		while (iter.hasNext()) {
			String anchor = (String) iter.next();
			List patterns = (List) anchorMap.get(anchor);
			for (int j=0; j<patterns.size(); j++) {
				EventPattern ep = (EventPattern) patterns.get(j);
				ep.write(writer);
			}
		}
		writer.close();
	}

	/**
	 *  load an event pattern set in the form saved by the 'save' method.
	 */

	public void load (String fileName) throws IOException {
		BufferedReader reader = new BufferedReader (new FileReader (fileName));
		int patternCount = 0;
		String line = reader.readLine();
		while (line != null) {
			EventPattern ep = new EventPattern(reader);
			addBasicPattern (ep.anchor, ep);
			patternCount++;
			line = reader.readLine();
		}
		System.out.println (patternCount + " patterns loaded");
	}


	private static boolean evalTrace = false;
	private static int eventWeight = 10;

	/**
	 *  applies the learned patterns to Document 'doc' and records the
	 *  number of times it produced a correct or incorrect event.
	 */

	public void evaluatePatterns (Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations (triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		Vector constituents = doc.annotationsOfType("constit");
		for (int i=0; i<constituents.size(); i++) {
			Annotation constit = (Annotation) constituents.get(i);
			String cat = (String) constit.get("cat");
			if (cat == "n" || cat == "v" || cat == "tv" || cat == "ven" ||
				  cat == "ving" || cat == "adj") {
				String anchor = EventPattern.normalizedAnchor (constit, doc, relations);
				Span anchorExtent = constit.span();
				List patterns = (List) anchorMap.get(anchor);
				if (patterns != null) {
					String eventType = null;
					// record success / failure for individual patterns
					for (int j=0; j<patterns.size(); j++) {
						EventPattern ep = (EventPattern) patterns.get(j);
						AceEvent event = ep.match(anchorExtent, anchor, doc, relations, aceDoc);
						if (event != null) {
							if (evalTrace) System.out.println ("Evaluating " + ep);
							if (evalTrace) System.out.println ("  for matched event " + event);
							AceEventMention mention = (AceEventMention) event.mentions.get(0);
							if (evalTrace) System.out.println ("  with extent " + doc.text(mention.jetExtent));
							AceEvent keyEvent = correctEvent(anchorExtent, event, events);
							if (keyEvent != null) {
								// if event is correct, count correct and spurious arguments
								ArrayList arguments = mention.arguments;
								ArrayList keyArguments = correctEventMention.arguments;
								ArrayList correctArguments = new ArrayList(arguments);
								correctArguments.retainAll(keyArguments);
								ArrayList spuriousArguments = new ArrayList(arguments);
								spuriousArguments.removeAll(keyArguments);
								int successCount = eventWeight + correctArguments.size();
								int failureCount = spuriousArguments.size();
								// ---
								ep.evaluation.recordSuccess(mention.arguments, successCount);
								ep.evaluation.recordFailure(mention.arguments, failureCount);
								if (evalTrace) System.out.println ("    a success");
								eventType = event.type + ":" + event.subtype;
							} else {
								// if event is incorrect, count all arguments as spurious
								int failureCount = eventWeight + mention.arguments.size();
								ep.evaluation.recordFailure(mention.arguments, failureCount);
								if (evalTrace) System.out.println ("    a failure");
							}
						}
					}
				}
			}
		}
	}

	AceEventMention correctEventMention = null;

	private AceEvent correctEvent(Span anchorExtent, AceEvent event,
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
					correctEventMention = keyMention;
					return keyEvent;
				}
			}
		}
		return null;
	}

	private AceEvent correctEvent(AceEvent event, ArrayList keyEvents) {
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
					correctEventMention = keyMention;
					return keyEvent;
				}
			}
		}
		return null;
	}


	AceEvent eventAnchoredByConstituent
	(Annotation constit, Document doc, AceDocument aceDoc, String docId,
	 SyntacticRelationSet relations, int aceEventNo) {
		String anchor = EventPattern.normalizedAnchor (constit, doc, relations);
		Span anchorExtent = constit.span();
		// search patterns for match
		List patterns = (List) anchorMap.get(anchor);
		if (patterns == null)
			return null;
		AceEvent bestEvent =
			matchPatternSet (patterns, anchorExtent, anchor, doc, relations, aceDoc);
		if (bestEvent == null)
			return null;
		Annotation sentence = EventPattern.containingSentence(doc, anchorExtent);
		int slash = docId.lastIndexOf('/');
			if (slash >= 0)
				docId = docId.substring(slash + 1);
		String eventId = docId + "-EV" + aceEventNo;
		bestEvent.setId(eventId);
		AceEventMention bestMention = (AceEventMention) bestEvent.mentions.get(0);
		bestMention.setId(eventId + "-1");
		// collect additional arguments using statistical model
		if (useArgumentModel)
			collectArguments (bestEvent, bestMention, doc, aceDoc, relations);
		if (confidentRoleCount (bestEvent, CONFIDENT_ARG) > 0) {
			// System.out.println ("Tagged event = " + bestEvent);
			return bestEvent;
		} else {
			return null;
		}
	}

	
	private void trainEventModel (Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations (triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		// iterate over potential anchors
		Vector constituents = doc.annotationsOfType("constit");
		for (int i=0; i<constituents.size(); i++) {
			Annotation constit = (Annotation) constituents.get(i);
			String cat = (String) constit.get("cat");
			if (cat == "n" || cat == "v" || cat == "tv" || cat == "ven" ||
				  cat == "ving" || cat == "adj") {
				String anchor = EventPattern.normalizedAnchor (constit, doc, relations);
				Span anchorExtent = constit.span();
				List patterns = (List) anchorMap.get(anchor);
				if (patterns == null) continue;
				CONFIDENT_ARG = 0.10;
				AceEvent event = eventAnchoredByConstituent
				  (constit, doc, aceDoc, docId, relations, 0);
				// if no event (success count too low), skip for now
				if (event == null) continue;
				// if a very confident arg (pattern match), skip for now
				if (confidentRoleCount (event, 0.999) != 0) continue;
				EventPattern pattern = (EventPattern) patternMatched;
				// outcome:
				//    is this the anchor of a true event (even if of different type)
				Datum d = eventFeatures (anchor, event, pattern);
				boolean isEvent = correctEvent(anchorExtent, event, events) != null;
				d.setOutcome(isEvent ? "event" : "noEvent");
				eventFeatureWriter.println(d.toString());
			}
		}
	}

	/**
	 *  the features for deciding whether an event is reportable are
	 *  - the anchor
	 *  - the fraction of times the anchor is reportable
	 *  - the probability that it has each argument
	 */

	private Datum eventFeatures (String anchor, AceEvent event, EventPattern pattern) {
		Datum d = new Datum();
		d.addF (anchor);
		PatternEvaluation eval = pattern.evaluation;
		int successRate = 5 * eval.successCount / (eval.successCount + eval.failureCount);
		d.addFV ("successRate", Integer.toString(successRate));
		ArrayList arguments = event.arguments;
		for (int i=0; i<arguments.size(); i++) {
			AceEventArgument arg = (AceEventArgument) arguments.get(i);
			String role = arg.role;
			int conf = (int) (arg.confidence * 5.);
			d.addF (role + "+" + conf);
	 	}
		return d;
	}

	
	
	private void trainCorefModel(Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		
		relations.addRelations(doc);
		

		// event list from key

		ArrayList events = constructEvent(aceDoc, docId);
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		// aceDoc.events = events;

		int aceEventNo = 1;
		HashMap<String, Vector> eventMap = findEventTypes(events, docId);

		for (String eventType : eventMap.keySet()) {
			Vector<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1) {
				continue;
			} else {
				for (int i = 0; i < v.size(); i++) {
					AceEvent e1 = v.get(i);
					AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
					String anchor1 = EventPattern.normalizedAnchor(
							m1.anchorExtent, m1.anchorText, doc, relations);

					for (int j = i + 1; j < v.size(); j++) {
						AceEvent e2 = v.get(j);
						AceEventMention m2 = (AceEventMention) e2.mentions
								.get(0);
						String anchor2 = EventPattern.normalizedAnchor(
								m2.anchorExtent, m2.anchorText, doc, relations);
						Datum d = corefFeatures(e1, e2, anchor1, anchor2, doc,
								FEATURE_SET);

						d.setOutcome(corefEvent(e1, e2, aceDoc.events) == true ? "coref"
										: "dontcoref");
						corefFeatureWriter.println(d.toString());
					}
				}
			}
		}
	}
	
	private boolean corefEvent(AceEvent e1, AceEvent e2, ArrayList keyEvents) {
		AceEvent key1 = correctEvent(e1, keyEvents);
		AceEvent key2 = correctEvent(e2, keyEvents);
		if (key1 == null || key2 == null || !key1.id.equals(key2.id))
			return false;
		else
			return true;
	}
	
	static String getAnchorCat (Span anchorSpan,Document doc) {
		int posn = anchorSpan.start();
		Vector constits = doc.annotationsAt (posn, "constit");
		if (constits != null) {
			for (int i=0; i<constits.size(); i++) {
				Annotation constit = (Annotation) constits.get(i);
				String cat = (String) constit.get("cat");
				if (cat=="n")
					cat += "_"+(String) constit.get("number");
				return cat;
				
			}
		}
		return "";
	
	}
	
	static int getEventId (AceEventMention mention,ArrayList events) {
		for (int i=0;i<events.size();i++){
			AceEvent e= (AceEvent)events.get(i);
			if (e.mentions.get(0).anchorExtent.equals(mention.anchorExtent)){
				return i;
			}
		}
		return -1;
	}

	private Datum corefFeatures(AceEvent e1, AceEvent e2, String anchor1,
			String anchor2, Document doc, int id) {
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
		// normalized anchor
		d.addFV("anchorpair", anchor1 + ":" + anchor2);
		d.addFV("pospair", pos1 + ":" + pos2);
		d.addFV("stempair", stem1 + ":" + stem2);

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

		if (id == 0)
			return d;

		int posnPriorEvent = m1.anchorExtent.start();
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

		d.addFV("mendist", Integer.toString(Math.min(ev2 - ev1, 9)));

		if (id == 1)
			return d;

		// feature set: arguments
		// overlapping and conflicting roles with confidence
		ArrayList priorArgs = e1.arguments;
		ArrayList args = e2.arguments;
		int shareNumber = 0;
		int priorNumber = 0;
		int curNumber = 0;
		//int corefNumber = 0;
		for (int i = 0; i < priorArgs.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) priorArgs.get(i);
			if (arg1.confidence < COREF_CONFIDENCE)
				continue;
			String role1 = arg1.role;
			String id1 = arg1.value.id;
			boolean bFound = false;
			for (int j = 0; j < args.size(); j++) {
				AceEventArgument arg2 = (AceEventArgument) args.get(j);
				if (arg2.confidence < COREF_CONFIDENCE)
					continue;
				String role2 = arg2.role;
				String id2 = arg2.value.id;
				if (id1.equals(id2) && !role1.equals(role2)) {
					d.addFV("coref1", role1);
					d.addFV("coref2", role2);
					//corefNumber++;
				}
				// int confidence = (int) (Math.min(arg1.confidence,
				// arg2.confidence) * 5);
				if (role1.equals(role2)) {
					if (id1.equals(id2)) {
						d.addFV("overlap", role1 /* + ":" + confidence */);
						shareNumber++;
						bFound = true;
						break;
					}
				}
			}
			if (!bFound) {
				priorNumber++;
				d.addFV("prior", role1);
			}
		}
		d.addFV("overlapNumber", Integer.toString(shareNumber));
		d.addFV("priorNumber", Integer.toString(priorNumber));
		//d.addFV("corefNumber", Integer.toString(corefNumber));

		shareNumber = 0;
		for (int i = 0; i < args.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) args.get(i);
			if (arg1.confidence < COREF_CONFIDENCE)
				continue;
			String role1 = arg1.role;
			String id1 = arg1.value.id;
			boolean bFound = false;
			for (int j = 0; j < priorArgs.size(); j++) {
				AceEventArgument arg2 = (AceEventArgument) priorArgs.get(j);
				if (arg2.confidence < COREF_CONFIDENCE)
					continue;
				String role2 = arg2.role;
				String id2 = arg2.value.id;
				int confidence = (int) (Math.min(arg1.confidence,
						arg2.confidence) * 5);
				if (role1.equals(role2)) {
					if (id1.equals(id2)) {
						shareNumber++;
						bFound = true;
						break;
					}
				}
				if (role1.equals("Place") && role2.equals("Place")) {
					if (!id1.equals(id2)) {
						d.addFV("placeConflict", "T");
					}
				}
				if (role1.equals("Time-Within") && role2.equals("Time-Within")) {
					if (!id1.equals(id2)) {
						d.addFV("timeConflict", "T");
					}
				}
			}
			if (!bFound) {
				curNumber++;
				d.addFV("current", role1);
			}
		}
		d.addFV("curNumber", Integer.toString(curNumber));

		if (id == 2)
			return d;

		// feature set : event attributes

		d.addFV("modality", e2.modality);
		d.addFV("polarity", e2.polarity);
		d.addFV("genericity", e2.genericity);
		d.addFV("tense", e2.tense);
		if (!e2.modality.equals(e1.modality))
			d.addFV("modalityConflict", "T");
		if (!e2.polarity.equals(e1.polarity))
			d.addFV("polarityConflict", "T");
		if (!e2.genericity.equals(e1.genericity))
			d.addFV("genericityConflict", "T");
		if (!e2.tense.equals(e1.tense))
			d.addFV("tenseConflict", "T");
		return d;
	}

	
	// minimum confidence to consider arg a confident argument
	private static double CONFIDENT_ARG = 0.10;

	/**
	 * identify ACE events in Document 'doc' and add them to 'aceDoc'.
	 */

	public void tag(Document doc, AceDocument aceDoc, String currentDocPath,
			String docId, int choice) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations (triplesDir + currentDocPath + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		if (type.equals("system")){
			ArrayList newEvents = new ArrayList();
			int aceEventNo = 1;
			Vector constituents = doc.annotationsOfType("constit");
			HashSet matchedAnchors = new HashSet();
			if (constituents != null) {
				for (int i=0; i<constituents.size(); i++) {
					Annotation constit = (Annotation) constituents.get(i);
					String cat = (String) constit.get("cat");
					if (cat == "n" || cat == "v" || cat == "tv" || cat == "ven" ||
					    cat == "ving" || cat == "adj") {
					  Span anchorExtent = constit.span();
					  if (matchedAnchors.contains(anchorExtent)) continue; //<< added 13 Feb 06
						AceEvent event = eventAnchoredByConstituent
						  (constit, doc, aceDoc, docId, relations, aceEventNo);
						event = pruneEvent (event, constit, doc, relations);
						if (event != null/* && findEvent(aceDoc,event.mentions.get(0))!=null*/) {
							newEvents.add (event);
							aceEventNo++;
							matchedAnchors.add(anchorExtent);
						}
					}
				}
			}
			aceDoc.events.clear();
			aceDoc.events=newEvents;
		}
		eventCoref(aceDoc, doc, relations, choice,docId);
	}

	
	ArrayList constructEvent(AceDocument aceDoc, String docId) {
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

	

	public void tag(String fileList, int choice) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("\nTag file " + Integer.toString(docCount) + ":"
					+ currentDocPath);
			// String textFile = ace + currentDocPath;
			String textFile = ace + currentDocPath;
			String xmlFile = ace
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String outputFile = home + "output/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			if (choice<4)
				Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			//aceDoc.events.clear();
			
			if (type.equals("perfect")){
				ArrayList events = constructEvent(aceDoc, aceDoc.docID);
				AceEventCompare comp = new AceEventCompare();
				Collections.sort(events, comp);
				aceDoc.events = events;
			}
			
			if (choice<4)
				tag(doc, aceDoc, currentDocPath.replaceFirst(".sgm", ""),aceDoc.docID, choice);
			aceDoc.write(new BufferedWriter(new FileWriter(outputFile)), doc);
		}
	}

	
	
	// get all the event subtypes, and count the number for each subtype
	public HashMap<String, Vector> findEventTypes(AceDocument aceDoc) {
		ArrayList<AceEvent> events = aceDoc.events;
		HashMap eventTypeMap = new HashMap();
		int aceEventNo = 1;
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			String eventId = aceDoc.docID + "-EV" + aceEventNo;
			event.setId(eventId);
			aceEventNo++;
			if (!eventTypeMap.containsKey(event.subtype)) {
				eventTypeMap.put(event.subtype, new Vector());
			}
			Vector v = (Vector) eventTypeMap.get(event.subtype);
			v.add(event);
			eventTypeMap.put(event.subtype, v);

		}
		return eventTypeMap;
	}

	public HashMap<String, Vector> findEventTypes(ArrayList events, String docID) {
		HashMap eventTypeMap = new HashMap();
		int aceEventNo = 1;
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			String eventId = docID + "-EV" + aceEventNo;
			event.setId(eventId);
			aceEventNo++;
			if (!eventTypeMap.containsKey(event.subtype)) {
				eventTypeMap.put(event.subtype, new Vector());
			}
			Vector v = (Vector) eventTypeMap.get(event.subtype);
			v.add(event);
			eventTypeMap.put(event.subtype, v);

		}
		return eventTypeMap;
	}

	protected boolean useSparseMatrix = false;

	public DoubleMatrix2D csimModel(Vector<AceEvent> v, Document doc,
			SyntacticRelationSet relations) {
		int n = v.size();
		DoubleMatrix2D w;
		if (useSparseMatrix)
			w = DoubleFactory2D.sparse.make(n, n);
		else
			w = DoubleFactory2D.dense.make(n, n);
		for (int i = 0; i < n; i++) {
			AceEvent e1 = v.get(i);
			AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
			String anchor1 = EventPattern.normalizedAnchor(m1.anchorExtent,
					m1.anchorText, doc, relations);

			for (int j = i; j < n; j++) {
				AceEvent e2 = v.get(j);
				AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
				String anchor2 = EventPattern.normalizedAnchor(m2.anchorExtent,
						m2.anchorText, doc, relations);
				Datum d = corefFeatures(e1, e2, anchor1, anchor2, doc,
						FEATURE_SET);
				double prob = corefModel.eval(d.toArray())[corefModel
						.getIndex("coref")];

				w.set(i, j, prob);
				w.set(j, i, prob);
			}
		}
		return w;
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

	static String stopwordFile = home + "stoplist";
	static Vector<String> stopList = new Vector<String>();

	public DoubleMatrix2D csimOverlap(Vector<AceEvent> v, Document doc,
			SyntacticRelationSet relations) {
		int n = v.size();
		DoubleMatrix2D w;
		if (useSparseMatrix)
			w = DoubleFactory2D.sparse.make(n, n);
		else
			w = DoubleFactory2D.dense.make(n, n);
		for (int i = 0; i < n; i++) {
			AceEvent e1 = v.get(i);
			AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
			Annotation sentence1 = findContainingSentence(doc, m1.anchorExtent);
			if (sentence1 == null)
				continue;
			Vector<Annotation> tokens = doc.annotationsOfType("token",sentence1.span());
			ArrayList textList1 = new ArrayList();
			if (tokens != null) {
				for (Annotation token : tokens) {
					doc.shrink(token);
					Span tokenSpan = token.span();
					String term = doc.text(tokenSpan).toLowerCase().trim();
					
					if (TfIdf.stopList.contains(term))
						continue;
					//System.out.println(term+":"+term.length());
					if (term.length()<=2)
						continue;
					if (isNumber(term))
						continue;
					
					term = term.replace(".", "");
					if (useStem)
					{
						term = ps.getStem(term);
					}
					textList1.add(term);
			
				}
			}
			for (int j = i; j < n; j++) {
				AceEvent e2 = v.get(j);
				AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
				Annotation sentence2 = findContainingSentence(doc, m2.anchorExtent);
				if (sentence2 == null || sentence1.span().equals(sentence2.span())){
					w.set(i, j, 0);
					w.set(j, i, 0);
					continue;
				}
				tokens = doc.annotationsOfType("token",sentence2.span());
				ArrayList textList2 = new ArrayList();
				if (tokens != null) {
					for (Annotation token : tokens) {
						doc.shrink(token);
						Span tokenSpan = token.span();
						String term = doc.text(tokenSpan).toLowerCase().trim();
						
						if (TfIdf.stopList.contains(term))
							continue;
						//System.out.println(term+":"+term.length());
						if (term.length()<=2)
							continue;
						if (isNumber(term))
							continue;
						term = term.replace(".", "");
						if (useStem)
						{
							term = ps.getStem(term);
						}
						textList2.add(term);
				
					}
				}
				ArrayList textList3= new ArrayList(textList2);
				textList3.retainAll(textList1);
				double sim = (double)textList3.size()/(Math.log(textList2.size())+Math.log(textList1.size()));
				
				/*if (j == i) {
					w.set(i, j, 1);
					w.set(j, i, 1);
				} else {*/
					w.set(i, j, sim);
					w.set(j, i, sim);
				//}
			}
		}
		return w;
	}
	
	static int termNo = 11854;
	static int docNo = 599;
	
	public DoubleMatrix2D csimTfIdf(Vector<AceEvent> v, Document doc,
			SyntacticRelationSet relations,String docId) {
		int n = v.size();
		DoubleMatrix2D w;
		if (useSparseMatrix)
			w = DoubleFactory2D.sparse.make(n, n);
		else
			w = DoubleFactory2D.dense.make(n, n);
		for (int i = 0; i < n; i++) {
			AceEvent e1 = v.get(i);
			AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
			Annotation sentence1 = findContainingSentence(doc, m1.anchorExtent);
			if (sentence1 == null)
				continue;
			Vector<Annotation> tokens = doc.annotationsOfType("token",sentence1.span());
			DoubleMatrix1D w1= DoubleFactory1D.sparse.make(termNo);
			if (tokens != null) {
				for (Annotation token : tokens) {
					doc.shrink(token);
					Span tokenSpan = token.span();
					String term = doc.text(tokenSpan).toLowerCase().trim();
					
					if (TfIdf.stopList.contains(term))
						continue;
					//System.out.println(term+":"+term.length());
					if (term.length()<=2)
						continue;
					if (isNumber(term))
						continue;
					
					term = term.replace(".", "");
					if (useStem)
					{
						term = ps.getStem(term);
					}
					
					int row= TfIdf.termMap.get(term);
					int col = TfIdf.docMap.get(docId);
					w1.set(row, TfIdf.wordFreq.get(row, col));
					
				}
			}
			for (int j = i; j < n; j++) {
				AceEvent e2 = v.get(j);
				AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
				Annotation sentence2 = findContainingSentence(doc, m2.anchorExtent);
				if (sentence2 == null || sentence1.span().equals(sentence2.span())){
					w.set(i, j, 0);
					w.set(j, i, 0);
					continue;
				}
				tokens = doc.annotationsOfType("token",sentence2.span());
				DoubleMatrix1D w2= DoubleFactory1D.sparse.make(termNo);
				if (tokens != null) {
					for (Annotation token : tokens) {
						doc.shrink(token);
						Span tokenSpan = token.span();
						String term = doc.text(tokenSpan).toLowerCase().trim();
						
						if (TfIdf.stopList.contains(term))
							continue;
						//System.out.println(term+":"+term.length());
						if (term.length()<=2)
							continue;
						if (isNumber(term))
							continue;
						term = term.replace(".", "");
						if (useStem)
						{
							term = ps.getStem(term);
						}
						int row= TfIdf.termMap.get(term);
						int col = TfIdf.docMap.get(docId);
						w2.set(row, TfIdf.wordFreq.get(row, col));
						
						
					}
				}
				double sim = computeSim(w1,w2);
				/*if (j == i) {
					w.set(i, j, 1);
					w.set(j, i, 1);
				} else {*/
					w.set(i, j, sim);
					w.set(j, i, sim);
				//}
			}
		}
		return w;
	}
	
	public static double computeSim (DoubleMatrix1D w1,DoubleMatrix1D w2){
		double sim = 0;
		for (int i= 0;i<w1.size();i++){
			sim += w1.get(i)*w2.get(i);
		}
		double norm1=0;
		for (int i=0;i<w1.size();i++){
			norm1+=w1.get(i)*w1.get(i);
		}
		double norm2=0;
		for (int i=0;i<w2.size();i++){
			norm2+=w2.get(i)*w2.get(i);
		}
		sim = Math.exp(sim/Math.sqrt(norm1*norm2));
		return sim;
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
	
	public DoubleMatrix2D csimCon(Vector<AceEvent> v, Document doc,
			SyntacticRelationSet relations) {
		int n = v.size();
		DoubleMatrix2D w;
		if (useSparseMatrix)
			w = DoubleFactory2D.sparse.make(n, n);
		else
			w = DoubleFactory2D.dense.make(n, n);
		for (int i = 0; i < n; i++) {
			AceEvent e1 = v.get(i);
			AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
			String text1 = m1.text.replace("\n", " ");
			String[] textArr1 = text1.split("\\s+");
			ArrayList textList1 = new ArrayList();
			for (int k = 0; k < textArr1.length; k++) {
				String str = textArr1[k].replace(",", "");
				str = str.replace(".", "");
				str = str.replace(":", "");
				textList1.add(str);
			}
			textList1.removeAll(stopList);
			for (int j = i; j < n; j++) {
				AceEvent e2 = v.get(j);
				AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
				String text2 = m2.text.replace("\n", " ");
				String[] textArr2 = text2.split("\\s+");
				ArrayList textList2 = new ArrayList();
				for (int k = 0; k < textArr2.length; k++) {
					String str = textArr2[k].replace(",", "");
					str = str.replace(".", "");
					str = str.replace(":", "");
					textList2.add(str);
				}
				textList2.removeAll(stopList);
				textList2.retainAll(textList1);
				int sim = textList2.size();
				if (sim==0)
					sim = 1;
				/*if (j == i) {
					w.set(i, j, 1);
					w.set(j, i, 1);
				} else {*/
					w.set(i, j, sim);
					w.set(j, i, sim);
				//}
			}
		}
		return w;
	}


	static double tcoef1 = 0.45;
	static double tcoef2 = 0.22;
	static double tcoef3 = 0.08;
	static double tcoef4 = 0.09;
	static double tcoef5 = 0.12;

	static double acoef1 = 0.37;
	static double acoef2 = 0.18;
	static double acoef3 = 0.07;
	static double acoef4 = 0.1;

	public DoubleMatrix2D csimFormula(Vector<AceEvent> v, Document doc,
			SyntacticRelationSet relations) {
		int n = v.size();
		DoubleMatrix2D w;
		if (useSparseMatrix)
			w = DoubleFactory2D.sparse.make(n, n);
		else
			w = DoubleFactory2D.dense.make(n, n);
		for (int i = 0; i < n; i++) {
			AceEvent e1 = v.get(i);
			AceEventMention m1 = (AceEventMention) e1.mentions.get(0);
			String anchor1 = EventPattern.normalizedAnchor(m1.anchorExtent,
					m1.anchorText, doc, relations);

			for (int j = i; j < n; j++) {
				AceEvent e2 = v.get(j);
				AceEventMention m2 = (AceEventMention) e2.mentions.get(0);
				String anchor2 = EventPattern.normalizedAnchor(m2.anchorExtent,
						m2.anchorText, doc, relations);
				double anchorSim;
				PorterStemmer ps = new PorterStemmer();
				if (m1.anchorText.equals(m2.anchorText)) {
					anchorSim = tcoef1;
				} else {
					String stem1 = ps.stem(m1.anchorText.toLowerCase());
					String stem2 = ps.stem(m2.anchorText.toLowerCase());
					if (stem1.equals(stem2)) {
						anchorSim = tcoef2;
					} else {
						// quantized anchor similarity
						SimilarityAssessor _assessor = new SimilarityAssessor();
						double semSim = _assessor.getSimilarity(stem1, stem2);
						if (semSim >= 1) {
							anchorSim = tcoef3;
						} else if (semSim < 1 && semSim > 0) {
							anchorSim = tcoef4;
						} else
							anchorSim = tcoef5;
					}
				}

				AceEventMention m3, m4;
				if (m1.arguments.size() > m2.arguments.size()) {
					m3 = m2;
					m4 = m1;

				} else {
					m3 = m1;
					m4 = m2;
				}
				int count = 0;
				int count1 = 0;
				int count2 = 0;
				int count3 = 0;
				double argSim1, argSim2, argSim3, argSim4;
				count = m3.arguments.size();
				for (int k = 0; k < m3.arguments.size(); k++) {
					String id1 = m3.arguments.get(k).value.getParent().id;
					String role1 = m3.arguments.get(k).role;

					for (int m = 0; m < m4.arguments.size(); m++) {
						String id2 = m4.arguments.get(m).value.getParent().id;
						String role2 = m4.arguments.get(m).role;
						// role and id match
						if (id1.equals(id2) && role1.equals(role2)) {
							count1++;
							break;
						}
						// id match, but role does not match
						if (id1.equals(id2) && !role1.equals(role2)) {
							count2++;
							break;
						}
						// id does not match, but role match
						if (!id1.equals(id2) && role1.equals(role2)) {
							count3++;
							break;
						}
					}
				}
				double sim;
				if (count > 0)
					sim = anchorSim + (acoef1 * count1) / count
							+ (acoef2 * count2) / count + (acoef3 * count3)
							/ count
							+ (acoef4 * (count - count1 - count2 - count3))
							/ count;
				else
					sim = anchorSim;
				sim = Math.exp(-1/sim);
				w.set(i, j, sim);
				w.set(j, i, sim);
			}
		}
		return w;
	}

	/**
	 * performs coreference on the events in an Ace document. On entry, the
	 * AceDocument <CODE>aceDoc</CODE> should have a set of events each with a
	 * single mention. The event mentions which are believed to corefer are
	 * combined into a single event.
	 */



	public void eventCoref(AceDocument aceDoc, Document doc,
			SyntacticRelationSet relations, int choice,String docId) {
		ArrayList<AceEvent> events = aceDoc.events;
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		System.out.println("eventCoref (before): " + events.size() + " event mentions");
		ArrayList<AceEvent> newEvents = new ArrayList<AceEvent>();

		int aceEventNo = 1;

		HashMap<String, Vector> eventMap = findEventTypes(aceDoc);
		for (String eventType : eventMap.keySet()) {
			Vector<AceEvent> v = eventMap.get(eventType);
			if (v == null || v.size() == 0)
				System.out.println("Error");
			else if (v.size() == 1) {
				AceEvent e = v.get(0);
				String eventId = aceDoc.docID + "-EV" + aceEventNo;
				e.setId(eventId);
				AceEventMention m0 = (AceEventMention) e.mentions.get(0);
				m0.setId(e.id + "-1");

				newEvents.add(e);
				aceEventNo++;
			} else {
				DoubleMatrix2D w = null;
				if (choice == 0)
					w = csimModel(v, doc, relations);
				else if (choice == 1)
					w = csimFormula(v, doc, relations);
				else if (choice == 2)
					w = csimOverlap(v, doc, relations);
				else if (choice == 3)
					w = csimTfIdf(v, doc, relations,docId);
				SpectralClusterer sc = new SpectralClusterer();
				int[][] p = null;
				try {
					p = sc.buildClusterer(w, CLUSTER_THRESHOLD);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (p == null)
					return;
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
						m.setId(priorEvent.id + "-"
								+ priorEvent.mentions.size());
					}
					newEvents.add(priorEvent);
					aceEventNo++;
				}
			}
		}

		aceDoc.events = newEvents;
		System.out.println("eventCoref (after): " + aceDoc.events.size() + " events");
	}

	private boolean compatibleArguments(ArrayList args1, ArrayList args2) {
		boolean intersect = false;
		for (int i = 0; i < args1.size(); i++) {
			AceEventArgument arg1 = (AceEventArgument) args1.get(i);
			if (arg1.confidence < COREF_CONFIDENCE)
				continue;
			String role1 = arg1.role;
			String id1 = arg1.value.id;
			for (int j = 0; j < args2.size(); j++) {
				AceEventArgument arg2 = (AceEventArgument) args2.get(j);
				if (arg2.confidence < COREF_CONFIDENCE)
					continue;
				String role2 = arg2.role;
				String id2 = arg2.value.id;
				if (role1.equals(role2))
					if (id1.equals(id2))
						intersect = true;
					else
						return false;
			}
		}
		return intersect;
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

	AceEvent pruneEvent (AceEvent event, Annotation constit, Document doc,
			SyntacticRelationSet relations) {
		if (event == null)
			return null;
		if (confidentRoleCount (event, 0.999) != 0)
			return event;
		String anchor = EventPattern.normalizedAnchor (constit, doc, relations);
		EventPattern pattern = (EventPattern) patternMatched;
		Datum d = eventFeatures (anchor, event, pattern);
		double eventProb = eventModel.eval(d.toArray())[eventModel.getIndex("event")];
		if (eventProb < EVENT_PROBABILITY_THRESHOLD)
			return null;
		AceEventMention emention = (AceEventMention)event.mentions.get(0);
		emention.confidence = eventProb;
		// System.out.println ("Accepted event with prob " + eventProb);
		ArrayList args = event.arguments;
		Iterator it = args.iterator();
		while (it.hasNext()) {
			AceEventArgument ea = (AceEventArgument) it.next();
			if (ea.confidence * eventProb < ARGUMENT_PROBABILITY_THRESHOLD) it.remove();
		}
		args = emention.arguments;
		it = args.iterator();
		while (it.hasNext()) {
			AceEventMentionArgument ea = (AceEventMentionArgument) it.next();
			if (ea.confidence * eventProb < ARGUMENT_PROBABILITY_THRESHOLD) it.remove();
		}
		return event;
	}

	EventPattern patternMatched;

	AceEvent matchPatternSet (List patterns, Span anchorExtent, String anchor,
		  Document doc, SyntacticRelationSet relations, AceDocument aceDoc) {
		patternMatched = null;
		AceEvent bestEvent = null;
		EventPattern bestPattern = null;
		int bestMatchScore = 0;
		for (int j=0; j<patterns.size(); j++) {
			EventPattern ep = (EventPattern) patterns.get(j);
			// if (ep.patternType.equals("PA")) continue; // <<< SYNTAX only
			// try event pattern
			AceEvent event = ep.match(anchorExtent, anchor, doc, relations, aceDoc);
			// if it matches
			if (event != null && ep.evaluation.test(event.arguments) > 0) {
			  // and fills some argument role
			  int score = ep.getMatchScore() + ep.evaluation.test(event.arguments);
			  if (ep.patternType != null && ep.patternType.equals("SYNTAX")) score += 50; //<<<<<<<< favor SYNTAX over PA
			  if (event.arguments.size() >= 0 && score > bestMatchScore) {
					bestMatchScore = score;
					bestEvent = event;
					bestPattern = ep;
				}
			}
		}
		patternMatched = bestPattern;
		// if (bestMatchScore > 0) System.out.println ("Best match score = " + bestMatchScore);
		return bestEvent;
	}

	/**
	 *  returns the set of argument roles filled in 'event'.
	 */

	private Set<String> rolesFilledInEvent (AceEvent event) {
		ArrayList args = event.arguments;
		Set<String> roles = new HashSet<String>();
		for (int i=0; i<args.size(); i++) {
			AceEventArgument arg = (AceEventArgument) args.get(i);
			roles.add(arg.role);
		}
		return roles;
	}

	/**
	 *  returns a count of the number of arguments whose confidence
	 *  is above a threshold.
	 */

	private int confidentRoleCount (AceEvent event, double threshold) {
		ArrayList args = event.arguments;
		int count = 0;
		for (int i=0; i<args.size(); i++) {
			AceEventArgument arg = (AceEventArgument) args.get(i);
			if (arg.confidence > threshold) count++;
		}
		return count;
	}

	/**
	 *  returns a set of the values of the arguments of the event.
	 */

	private HashSet argumentValues (AceEvent event) {
		ArrayList args = event.arguments;
		HashSet values = new HashSet();
		for (int i=0; i<args.size(); i++) {
			AceEventArgument arg = (AceEventArgument) args.get(i);
			values.add(arg.value);
		}
		return values;
	}
	
	static String fileListTrain = home + "trainfilelist";
	
	// static final String fileListTest = ace + "perfect-parses/nw-head.txt";
	// static final String fileListTest = ace + "perfect-parses/bn-head.txt";
	// static final String fileListTest = ace + "perfect-parses/bc-head.txt";
	// static final String fileListTest = ace + "perfect-parses/un-head.txt";
	//static final String fileListTest = ace + "perfect-parses/head6.txt";
	static String fileListTest = home + "testfilelist";
	// ---- for eval
	// static final String fileListTest = home + "Ace 05/eval/evalFiles";

	static String eventFeatureFileName = aceModels + "eventFeatureFile.log";
	static String eventModelFileName = aceModels + "eventModel.log";
	// static final String evTypeFeatureFileName = ace + "evTypeFeatureFile.log";
	// static final String evTypeModelFileName = ace + "evTypeModel.log";

	static String corefFeatureFileName = aceModels + "corefFeatureFile.log";
	static String corefModelFileName = aceModels + "corefModel.log";

	static String argFeatureFileName = aceModels + "argFeatureFile.log";
	static String argModelFileName = aceModels + "argModel.log";
	static String roleFeatureFileName = aceModels + "roleFeatureFile.log";
	static String roleModelFileName = aceModels + "roleModel.log";

	static String argFeatureHalfFileName = aceModels + "argFeatureHalfFile.log";
	static String argModelHalfFileName = aceModels + "argModelHalf.log";
	static String roleFeatureHalfFileName = aceModels + "roleFeatureHalfFile.log";
	static String roleModelHalfFileName = aceModels + "roleModelHalf.log";

	static String eventPatternFile = aceModels + "eventPatterns.log";
	static String eventReportFile = aceModels + "eventPatternReport.log";
	static String halfEventPatternFile = aceModels + "eventPatternsHalf.log";
	static final boolean savePatterns = true;
	static final boolean trainArgModels = true;

	static final boolean trainPatterns = true;
	static final boolean trainEvModel = true;
	static final boolean trainCorefModel = true;
	static final boolean evaluate = true;

	static String mucFileName = home +"coref_muc";
	static String bcubeFileName = home +"coref_bcube";
	static String eacfFileName = home +"coref_eacf";
	static String fileList = home +"totalfilelist";
	static String evaluationDir;
	
	static String type = "perfect";
	static int simMeasure;
	
	private  void trainEventModel () throws IOException {
		anchorMap.clear();
		
		processOnlyOddDocuments = true;
		// set arg models
		argFeatureWriter = new PrintStream (new FileOutputStream (argFeatureHalfFileName));
		roleFeatureWriter = new PrintStream (new FileOutputStream (roleFeatureHalfFileName));
	  // train patterns on odd documents
		for (int pass=0; pass<=1; pass++) {
		  train (fileListTrain, pass);
			
		}
		save (halfEventPatternFile);
		// build arg models (half) and load them
		argFeatureWriter.close();
		roleFeatureWriter.close();
		buildClassifierModel(argFeatureHalfFileName, argModelHalfFileName);
		buildClassifierModel(roleFeatureHalfFileName, roleModelHalfFileName);
		argModel = loadClassifierModel(argModelHalfFileName);
		roleModel = loadClassifierModel(roleModelHalfFileName);
		// set ev model
		eventFeatureWriter = new PrintStream (new FileOutputStream (eventFeatureFileName));
		// et.evTypeFeatureWriter = new PrintStream (new FileOutputStream (evTypeFeatureFileName));
		// train event model on even documents
		processOnlyOddDocuments = false;
		processOnlyEvenDocuments = true;
		train (fileListTrain, 2);
		
		// build ev model
		eventFeatureWriter.close();
		// et.evTypeFeatureWriter.close();
		buildClassifierModel(eventFeatureFileName, eventModelFileName);
		// buildClassifierModel(evTypeFeatureFileName, evTypeModelFileName);
	}
	
	public static void main(String[] args) throws IOException {
		//TfIdf.genTfIdf();
		// get arguments
		System.out.println("Starting spectral clustering algorithm for event coreference resolution...");
		System.out.println("Usage example: java -cp /jar/workspace/coref/jet1.5.jar -Xmx1000m -server -DjetHome=$1 -Dthreshold=$2 -Dsim=$3 -Dtype=4 AceJet.AceEventCorefGraph");
		System.out.println("$1:home path");
		System.out.println("$2:clustering threshold,0.0-1.0, suggested 0.85");
		System.out.println("$3:similarity type,0:maximum entropy, 1:formula,");
		System.out.println("$4:perfect or system event mentions,perfect,system");
		System.out.println("property file is located at /props/AceEventCoref.properties");
		System.out.println("trainfilelist is located at /corpus/Ace05/trainfilelist");
		System.out.println("testfilelist is located at /corpus/Ace05/testfilelist");
		System.out.println("source files(.sgm, .apf.xml) are located at /corpus/Ace05/source/");
		System.out.println("model files are located at /corpus/Ace05/model/");
		System.out.println("tagged files are located at /corpus/Ace05/output/");
		
		//home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
		home = System.getProperty("jetHome")+"corpus/ACE05/";
		CLUSTER_THRESHOLD =Double.parseDouble(System.getProperty("threshold"));
		simMeasure =Integer.parseInt(System.getProperty("sim"));
		type = System.getProperty("type");
		if (System.getProperty("jetHome") == null) {
			System.out.println ("-DjetHome is not given in the command");
			System.exit(1);
		}
		if (System.getProperty("threshold") == null) {
			System.out.println ("-Dthreshold is not given in the command");
			System.exit(1);
		}
		if (System.getProperty("sim") == null) {
			System.out.println ("-Dsim is not given in the command");
			System.exit(1);
		}
		if (System.getProperty("type") == null) {
			System.out.println ("-Dtype is not given in the command");
			System.exit(1);
		}
		
		ace = home +"source/";
		aceModels = home + "model/";
		evaluationDir = home + "evaluation/coref/";
		
		mucFileName = evaluationDir +"muc";
		bcubeFileName = evaluationDir +"bcube";
		eacfFileName = evaluationDir +"eacf";
		
		fileListTrain = home +"trainfilelist";
		fileListTest = home + "testfilelist";
		corefFeatureFileName = aceModels + "corefFeatureFile.log";
		corefModelFileName = aceModels + "corefModel.log";

		JetTest.initializeFromConfig(System.getProperty("jetHome")+"props/AceEventCoref.properties");
		
		Pat.trace = false;
		Resolve.trace = false;
		
		FileWriter bw1 = new FileWriter(mucFileName);
		FileWriter bw2 = new FileWriter(bcubeFileName);
		FileWriter bw3 = new FileWriter(eacfFileName);
		

		AceEventCorefGraph et = new AceEventCorefGraph();

		if (type.equals("system")){
			eventFeatureFileName = aceModels + "eventFeatureFile.log";
			eventModelFileName = aceModels + "eventModel.log";
			
			argFeatureFileName = aceModels + "argFeatureFile.log";
			argModelFileName = aceModels + "argModel.log";
			roleFeatureFileName = aceModels + "roleFeatureFile.log";
			roleModelFileName = aceModels + "roleModel.log";
	
			argFeatureHalfFileName = aceModels + "argFeatureHalfFile.log";
			argModelHalfFileName = aceModels + "argModelHalf.log";
			roleFeatureHalfFileName = aceModels + "roleFeatureHalfFile.log";
			roleModelHalfFileName = aceModels + "roleModelHalf.log";
	
			eventPatternFile = aceModels + "eventPatterns.log";
			eventReportFile = aceModels + "eventPatternReport.log";
			halfEventPatternFile = aceModels + "eventPatternsHalf.log";
			
			processOnlyOddDocuments = false;
			processOnlyEvenDocuments = false;
			
			if (trainPatterns) {
				if (trainArgModels) {
					et.argFeatureWriter = new PrintStream (new FileOutputStream (argFeatureFileName));
					et.roleFeatureWriter = new PrintStream (new FileOutputStream (roleFeatureFileName));
				}
				for (int pass=0; pass<=1; pass++) {
					et.train (fileListTrain, pass);
				}
				et.report (eventReportFile);
				if (trainArgModels) {
					et.argFeatureWriter.close();
					et.roleFeatureWriter.close();
					buildClassifierModel(argFeatureFileName, argModelFileName);
					buildClassifierModel(roleFeatureFileName, roleModelFileName);
				}
				if (savePatterns)
					et.save (eventPatternFile);
			} 
			if (trainEvModel) {
				et.trainEventModel ();
			}
		}
		
		if (simMeasure==0) {
			et.trainCorefModel();
		}
		if (evaluate) {
			et.loadAllModels(aceModels);
		
			evaluateEventTagger(et, simMeasure);
			
			//bw1.write("COREF_THRESHOLD=" + Double.toString(COREF_THRESHOLD) + "\n");
			MUCCorefEval ee1 = new MUCCorefEval();
			ee1.evalCoref(home,fileListTest);
			//bw.write("MUC\n");
			bw1.write(Double.toString(ee1.precision)+"\t"+Double.toString(ee1.recall)+"\t"+Double.toString(ee1.F)+"\n");
			//bw.write("BCube\n");
			BCubeCorefEval ee2 = new BCubeCorefEval();
			ee2.evalCoref(home,fileListTest);
			bw2.write(Double.toString(ee2.precision)+"\t"+Double.toString(ee2.recall)+"\t"+Double.toString(ee2.F)+"\n");
	
			//bw.write("CEAF\n");
			CEAFCorefEval ee3 = new CEAFCorefEval();
			ee3.evalCoref(home,fileListTest);
			bw3.write(Double.toString(ee3.precision)+"\t"+Double.toString(ee3.recall)+"\t"+Double.toString(ee3.F)+"\n");
		
		}
		bw1.close();
		bw2.close();
		bw3.close();
	}

	

	private void trainCorefModel() throws IOException {

		// set coref model
		corefFeatureWriter = new PrintStream(new FileOutputStream(
				corefFeatureFileName));
		// train coref model on even documents, using models from odd documents
		// processOnlyEvenDocuments = true;
		train(fileListTrain,3);

		// build coref model
		corefFeatureWriter.close();
		buildClassifierModel(corefFeatureFileName, corefModelFileName);
	}

	private static void evaluateEventTagger(AceEventCorefGraph et, int choice)
			throws IOException {
		// et.assessPatterns (fileListTest);
		et.tag(fileListTest, choice);
	}

	public void loadAllModels(String modelDir) throws IOException {
		if (type.equals("system")){
			anchorMap.clear();
			load(modelDir+"eventPatterns.log");
			eventModel = loadClassifierModel(modelDir + "eventModel.log");
			argModel = loadClassifierModel(modelDir + "argModel.log");
			roleModel = loadClassifierModel(modelDir + "roleModel.log");
		}
		if (simMeasure==0)
			corefModel = loadClassifierModel(modelDir + "corefModel.log");
	}

}
