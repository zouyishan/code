
//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkit
//              (ACE extensions)

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

public class ETGraphCorefV2 {

	// parameters
	// use statistical model to gather arguments
	static boolean useArgumentModel = true;
	// events below this probability are dropped
	static double EVENT_PROBABILITY_THRESHOLD = 0.10;
	// args below this prob are never added to an event, and not used
	// in estimating event probability
	static double MIN_ARG_PROBABILITY = 0.10;
	// args below this probability are dropped from the final event
	static double ARGUMENT_PROBABILITY_THRESHOLD = 0.35;
	// minimal confidence for an arg to be used for coref determination
	static double COREF_CONFIDENCE = 0.10;
	// minimal probability for merging two events
	static double COREF_THRESHOLD = 0.35;

	static int FEATURE_SET = 2;
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

	//static final String home = "/jar/workspace/graph/corpus/ACE05/";
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	// static final String home ="/home/zheng/Application/Coref/graph/ex1/ACE05/";
	// static final String home = "/home/zheng/Application/SRL/ACE05/";
	static final String ace = home + "source/";
	static String logfile = home+"coref2.log";
	static final String aceModels =
	// home + "ace 05/models/robust/";
	// home + "ace 05/models/non-robust/";
	// home + "Ace 05/models/no-sense/"; // final 2005 eval set
	// home + "ace 05/models/sense/";
	// home + "ace 05/models/noPA/"; // no PNB
	// home + "ace 05/models/noNB/"; // no NB
	// home + "ace 05/models/chunk/"; // chunk (no parse)
	// home + "ace 05/models/noGLARF/"; // relations from parse (no GLARF)
	// home + "Ace 05/models/Dec4/";
	// --- split (chunk / syntax / GLARF) patterns
	// with parse and GLARF
	// home + "Ace 05/models/splitPatterns/";
	// with chunk and GLARF
	// home + "Ace 05/models/chunkGLARF/";
	// with just chunk
	home + "model/";
	static String triplesDir =
	// home + "Ace 05/eval/tuples/"; // eval files
	// ace + "Nov1405-ntuples/"; // no-sense tuples
	// xxx ace + "Nov1105-ntuples/"; // non-robust tuples
	// xxx ace + "Nov1205-ntuples/"; // robust tuples
	// ace + "Dec4-stuples/"; // new Charniak parser
	// ace + "010306-slow-tuples/";
	ace + "011306-fast-tuples/"; // new Charniak parser, fast mode
	static String triplesSuffix =
	// ".sent.txt.ace-n-tuple90";
	// ".sent.txt.ns-ace-n-tuple90";
	// ".sent.txt.ns-2005-ace-n-tuple90";
	// ".sent.txt.ns-2005a-ace-n-tuple92";
	".sent.txt.ns-2005-fast-ace-n-tuple92";
	// ".sgm.sent.ns-2005-fast-ace-n-tuple92"; // for GALE files

	// use predicate-argument roles from GLARF
	static boolean usePA = false;
	// use parser (pre-parsed text), else use chunker
	static boolean useParser = false;
	static boolean processOnlyOddDocuments = false;
	static boolean processOnlyEvenDocuments = false;

	static boolean useStem = true;
	static Stemmer ps = Stemmer.getDefaultStemmer();
	
	public ETGraphCorefV2() {
		// anchorMap = new TreeMap<String, List>();
	}

	/**
	 * trains an event tagger from a set of text and APF files.
	 * 
	 * @param fileList
	 *            a list of text file names, one per line. The APF file names
	 *            are obtained by replacing 'sgm' by 'apf.xml'.
	 */

	public void train(String fileList, int pass) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			// if (docCount > 10) break;
			if (processOnlyOddDocuments && docCount % 2 == 0)
				continue;
			if (processOnlyEvenDocuments && docCount % 2 == 1)
				continue;
			System.out.println("\nTrain file " + Integer.toString(docCount)
					+ ":" + currentDocPath);
			String textFile = ace + (useParser ? "perfect-parses/" : "")
					+ currentDocPath;
			String xmlFile = ace
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String docId = currentDocPath.replaceFirst(".sgm", "");
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			Resolve.ACE = true;
			Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			if (pass == 0)
				acquirePatterns(doc, aceDoc, docId);
			else if (pass == 1)
				evaluatePatterns(doc, aceDoc, docId);
			else if (pass == 2)
				trainEventModel(doc, aceDoc, docId);
			else
				/* pass == 3 */
				trainCorefModel(doc, aceDoc, docId);
		}
		reader.close();
	}

	String[] patternTypeList = { "CHUNK", "SYNTAX", "PA" };

	/**
	 * trains the tagger from document 'doc' and corresponding AceDocument (APF
	 * file) aceDoc.
	 */

	public void acquirePatterns(Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations(triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			ArrayList mentions = event.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention m = (AceEventMention) mentions.get(j);
				System.out.println("\nProcessing mention " + m.id + " = "
						+ m.text);
				// .. get anchor extent and text
				Span anchorExtent = m.anchorJetExtent;
				String anchor = EventPattern.normalizedAnchor(anchorExtent,
						m.anchorText, doc, relations);
				// generate patterns
				for (String patternType : patternTypeList) {
					EventPattern ep = new EventPattern(patternType, doc,
							relations, event, m);
					if (ep.empty())
						continue;
					System.out.println(patternType + " pattern = " + ep);
					addPattern(anchor, ep);
					// try event pattern
					AceEvent builtEvent = ep.match(anchorExtent, anchor, doc,
							relations, aceDoc);
					if (builtEvent == null)
						System.err.println("**** match failed ****");
					// else
					// System.out.println ("Reconstructed event = " +
					// builtEvent);
					// prepare training data for argument classifier
				}
				if (trainArgModels) {
					trainArgClassifier(event, m, doc, aceDoc, relations);
				}
			}
		}
	}

	private void addPattern(String anchor, EventPattern ep) {
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

	private void addBasicPattern(String anchor, EventPattern ep) {
		List patternList = (List) anchorMap.get(anchor);
		if (patternList == null) {
			patternList = new ArrayList();
			anchorMap.put(anchor, patternList);
		}
		if (!patternList.contains(ep))
			patternList.add(ep);
	}

	/**
	 * trains two statistical models: - argModel to decide whether a mention is
	 * an argument of an event - roleModel to decide which role the mention
	 * should have
	 */

	private void trainArgClassifier(AceEvent event,
			AceEventMention eventMention, Document doc, AceDocument aceDoc,
			SyntacticRelationSet relations) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
				doc, relations);
		AceMention anchorMention = new AceEventAnchor(anchorExtent,
				anchorExtent, anchorText, doc);
		// find sentence containing anchor
		Annotation sentence = findContainingSentence(doc, anchorExtent);
		if (sentence == null)
			return;
		Span sentenceSpan = sentence.span();
		// iterate over mentions in sentence
		ArrayList mentions = aceDoc.getAllMentions();
		for (int im = 0; im < mentions.size(); im++) {
			AceMention mention = (AceMention) mentions.get(im);
			if (!mention.jetExtent.within(sentenceSpan))
				continue;
			// - compute syntactic path
			// - determine if mention has role in event
			ArrayList arguments = eventMention.arguments;
			String role = "noArg";
			for (int ia = 0; ia < arguments.size(); ia++) {
				AceEventMentionArgument argument = (AceEventMentionArgument) arguments
						.get(ia);
				if (argument.value.equals(mention)) {
					role = argument.role;
					break;
				}
			}
			Datum d = argumentFeatures(doc, anchor, event, mention,
					anchorMention, relations);
			// outcome = argument role
			if (role == "noArg") {
				d.setOutcome("noArg");
				argFeatureWriter.println(d.toString());
			} else {
				d.setOutcome("arg");
				argFeatureWriter.println(d.toString());
				d.setOutcome(role);
				roleFeatureWriter.println(d.toString());
			}
		}
	}

	private Datum argumentFeatures(Document doc, String anchor, AceEvent event,
			AceMention mention, AceMention anchorMention,
			SyntacticRelationSet relations) {
		String direction;
		ChunkPath cpath;
		// - compute chunk path from anchor
		if (anchorMention.getJetHead().start() < mention.getJetHead().start()) {
			direction = "follow";
			cpath = new ChunkPath(doc, anchorMention, mention);
		} else {
			direction = "precede";
			cpath = new ChunkPath(doc, mention, anchorMention);
		}
		String spath = EventSyntacticPattern.buildSyntacticPath(anchorMention
				.getJetHead().start(), mention.getJetHead().start(), relations);
		// System.out.println ("spath = " + spath);
		// build feature entry
		Datum d = new Datum();
		// = anchor word
		d.addFV("anchor", anchor);
		// = event type
		d.addFV("evType", event.type);
		// = EDT type of mention
		d.addFV("menType", mention.getType());
		// = head of mention (by itself and coupled with event subtype)
		// (intuition: US attacks Iraq, bombers kill, etc.)
		String headText = Resolve.normalizeName(mention.getHeadText()).replace(
				' ', '_');
		d.addFV("arg", headText);
		d.addFV("evTypeArg", event.subtype + ":" + headText);
		// = word preceding mention
		int pos = mention.jetExtent.start();
		Annotation token = doc.tokenEndingAt(pos);
		if (token != null) {
			d.addFV("prevToken", doc.text(token).trim());
			d.addFV("prevTokenAndType", event.type + "_"
					+ doc.text(token).trim());
		}
		// = chunk path, direction, distance
		if (cpath == null || cpath.toString() == null)
			d.addFV("noChunkPath", null);
		else {
			String cpathString = cpath.toString().replace(' ', '_');
			d.addFV("chunkPath", direction + "_" + cpathString);
			d.addFV("chunkPathAndType", event.type + "_" + direction + "_"
					+ cpathString);
			d.addFV("dist", Integer.toString(cpath.size()));
		}
		// = syntactic path
		if (spath == null)
			d.addF("noSynPath");
		else {
			d.addFV("synPath", spath);
			d.addFV("synPathEvType", event.type + "_" + spath);
			d.addFV("synPathTypes", event.type + "_" + mention.getType() + "_"
					+ spath);
		}
		return d;
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
			GISModel m = (GISModel) new SuffixSensitiveGISModelReader(f).getModel();
			System.err.println("GIS model " + f.getName() + " loaded.");
			return m;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
			return null; // required by compiler
		}
	}

	/**
	 * add arguments to 'event' using classifiers to decide if a mention is an
	 * argument, and to assign that argument a role.
	 * <p>
	 * For each mention in the sentence, determine the probability 'P' that it
	 * is an argument of this event and determine its most likely role. If P <
	 * MIN_ARG_PROBABILITY, ignore this mention. If the most likely role was
	 * filled by pattern matching or is not a valid role for this event, ignore
	 * this mention. If there are several mentions which have the same 'most
	 * likely role', select the one for which P is highest, and ignore the other
	 * mentions. Add the remaining mentions as arguments of the event, subject
	 * to the constraint that an entity may only appear once as the argument of
	 * an event (so that, in particular, entities already assigned as arguments
	 * by the pattern matcher will be skipped here).
	 */

	private void collectArguments(AceEvent event, AceEventMention eventMention,
			Document doc, AceDocument aceDoc, SyntacticRelationSet relations) {
		// get anchor
		Span anchorExtent = eventMention.anchorJetExtent;
		String anchorText = eventMention.anchorText;
		String anchor = EventPattern.normalizedAnchor(anchorExtent, anchorText,
				doc, relations);
		AceMention anchorMention = new AceEventAnchor(anchorExtent,
				eventMention.anchorJetExtent, anchorText, doc);
		// identify roles already filled
		Set rolesFilled = rolesFilledInEvent(event);
		// find sentence containing anchor
		Annotation sentence = findContainingSentence(doc, anchorExtent);
		if (sentence == null)
			return;
		Span sentenceSpan = sentence.span();
		// get arguments already used
		HashSet argumentsUsed = argumentValues(event);
		// iterate over mentions in sentence
		Map<String, Double> bestRoleProb = new HashMap<String, Double>();
		Map<String, AceMention> bestRoleFiller = new HashMap<String, AceMention>();
		Map<String, Double> bestRoleRoleProb = new HashMap<String, Double>();
		ArrayList mentions = aceDoc.getAllMentions();
		for (int im = 0; im < mentions.size(); im++) {
			AceMention mention = (AceMention) mentions.get(im);
			if (!mention.jetExtent.within(sentenceSpan))
				continue;
			if (mention.getJetHead().within(anchorExtent))
				continue; // Nov. 4 2005
			// build feature entry
			Datum d = argumentFeatures(doc, anchor, event, mention,
					anchorMention, relations);
			// classify:
			// probability that this is an argument
			// most likely role assignment
			double argProb = argModel.eval(d.toArray())[argModel
					.getIndex("arg")];
			String role = roleModel.getBestOutcome(roleModel.eval(d.toArray()))
					.intern();
			double roleProb = roleModel.eval(d.toArray())[roleModel
					.getIndex(role)];
			// System.out.println ("argProb of " + mention.getHeadText() +
			// " is " + argProb);
			/*
			 * the following code chooses the best valid role double[] roleProbs
			 * = roleModel.eval(d.toArray()); String role = null; double best =
			 * -1; for (int i=0; i<roleModel.getNumOutcomes(); i++) { String r =
			 * roleModel.getOutcome(i); if (roleProbs[i] > best &&
			 * AceEventArgument.isValid(event.subtype, r, mention)) { role = r;
			 * best = roleProbs[i]; } } if (role == null) continue;
			 */
			// if role already filled, continue
			if (rolesFilled.contains(role))
				continue;
			// if not a valid role for this event type, continue
			if (!AceEventArgument.isValid(event.subtype, role, mention))
				continue;
			// if likely argument, add to event
			if (argProb > MIN_ARG_PROBABILITY) {
				// if this mention has the highest probability of filling this
				// role,
				// record it
				if (bestRoleProb.get(role) == null
						|| argProb > bestRoleProb.get(role).doubleValue()) {
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
			if (argumentsUsed.contains(argValue))
				continue;
			double argProb = bestRoleProb.get(role);
			AceEventMentionArgument mentionArg = new AceEventMentionArgument(
					mention, role);
			mentionArg.confidence = argProb;
			mentionArg.roleConfidence = bestRoleRoleProb.get(role);
			eventMention.arguments.add(mentionArg);
			AceEventArgument eventArg = new AceEventArgument(argValue, role);
			eventArg.confidence = argProb;
			event.arguments.add(eventArg);
			// System.out.println ("Adding " + mention.getHeadText() +
			// " in role " +
			// role + " with prob " + argProb);
			argumentsUsed.add(argValue);
		}
	}

	private static boolean evalTrace = false;
	private static int eventWeight = 10;

	/**
	 * applies the learned patterns to Document 'doc' and records the number of
	 * times it produced a correct or incorrect event.
	 */

	public void evaluatePatterns(Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations(triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		Vector constituents = doc.annotationsOfType("constit");
		for (int i = 0; i < constituents.size(); i++) {
			Annotation constit = (Annotation) constituents.get(i);
			String cat = (String) constit.get("cat");
			if (cat == "n" || cat == "v" || cat == "tv" || cat == "ven"
					|| cat == "ving" || cat == "adj") {
				String anchor = EventPattern.normalizedAnchor(constit, doc,
						relations);
				Span anchorExtent = constit.span();
				List patterns = (List) anchorMap.get(anchor);
				if (patterns != null) {
					String eventType = null;
					// record success / failure for individual patterns
					for (int j = 0; j < patterns.size(); j++) {
						EventPattern ep = (EventPattern) patterns.get(j);
						AceEvent event = ep.match(anchorExtent, anchor, doc,
								relations, aceDoc);
						if (event != null) {
							if (evalTrace)
								System.out.println("Evaluating " + ep);
							if (evalTrace)
								System.out.println("  for matched event "
										+ event);
							AceEventMention mention = (AceEventMention) event.mentions
									.get(0);
							if (evalTrace)
								System.out.println("  with extent "
										+ doc.text(mention.jetExtent));
							AceEvent keyEvent = correctEvent(anchorExtent,
									event, events);
							if (keyEvent != null) {
								// if event is correct, count correct and
								// spurious arguments
								ArrayList arguments = mention.arguments;
								ArrayList keyArguments = correctEventMention.arguments;
								ArrayList correctArguments = new ArrayList(
										arguments);
								correctArguments.retainAll(keyArguments);
								ArrayList spuriousArguments = new ArrayList(
										arguments);
								spuriousArguments.removeAll(keyArguments);
								int successCount = eventWeight
										+ correctArguments.size();
								int failureCount = spuriousArguments.size();
								// ---
								ep.evaluation.recordSuccess(mention.arguments,
										successCount);
								ep.evaluation.recordFailure(mention.arguments,
										failureCount);
								if (evalTrace)
									System.out.println("    a success");
								eventType = event.type + ":" + event.subtype;
							} else {
								// if event is incorrect, count all arguments as
								// spurious
								int failureCount = eventWeight
										+ mention.arguments.size();
								ep.evaluation.recordFailure(mention.arguments,
										failureCount);
								if (evalTrace)
									System.out.println("    a failure");
							}
						}
					}
				}
			}
		}
	}

	AceEventMention correctEventMention = null;

	/**
	 * if 'event', triggered by the text in 'anchorExtent' (a Jet span), matches
	 * one of the events in 'keyEvents', returns the event (in keyEvents), else
	 * null.
	 */

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

	private boolean corefEvent(AceEvent e1, AceEvent e2, ArrayList keyEvents) {
		AceEvent key1 = correctEvent(e1, keyEvents);
		AceEvent key2 = correctEvent(e2, keyEvents);
		if (key1 == null || key2 == null || !key1.id.equals(key2.id))
			return false;
		else
			return true;
	}

	private void trainEventModel(Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations(triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}
		ArrayList events = aceDoc.events;
		// iterate over potential anchors
		Vector constituents = doc.annotationsOfType("constit");
		for (int i = 0; i < constituents.size(); i++) {
			Annotation constit = (Annotation) constituents.get(i);
			String cat = (String) constit.get("cat");
			if (cat == "n" || cat == "v" || cat == "tv" || cat == "ven"
					|| cat == "ving" || cat == "adj") {
				String anchor = EventPattern.normalizedAnchor(constit, doc,
						relations);
				Span anchorExtent = constit.span();
				List patterns = (List) anchorMap.get(anchor);
				if (patterns == null)
					continue;
				CONFIDENT_ARG = 0.10;
				AceEvent event = eventAnchoredByConstituent(constit, doc,
						aceDoc, docId, relations, 0);
				// if no event (success count too low), skip for now
				if (event == null)
					continue;
				// if a very confident arg (pattern match), skip for now
				if (confidentRoleCount(event, 0.999) != 0)
					continue;
				EventPattern pattern = (EventPattern) patternMatched;
				// outcome:
				// is this the anchor of a true event (even if of different
				// type)
				Datum d = eventFeatures(anchor, event, pattern);
				boolean isEvent = correctEvent(anchorExtent, event, events) != null;
				d.setOutcome(isEvent ? "event" : "noEvent");
				eventFeatureWriter.println(d.toString());
			}
		}
	}

	/**
	 * the features for deciding whether an event is reportable are - the anchor
	 * - the fraction of times the anchor is reportable - the probability that
	 * it has each argument
	 */

	private Datum eventFeatures(String anchor, AceEvent event,
			EventPattern pattern) {
		Datum d = new Datum();
		d.addF(anchor);
		PatternEvaluation eval = pattern.evaluation;
		int successRate = 5 * eval.successCount
				/ (eval.successCount + eval.failureCount);
		d.addFV("successRate", Integer.toString(successRate));
		ArrayList arguments = event.arguments;
		for (int i = 0; i < arguments.size(); i++) {
			AceEventArgument arg = (AceEventArgument) arguments.get(i);
			String role = arg.role;
			int conf = (int) (arg.confidence * 5.);
			d.addF(role + "+" + conf);
		}
		return d;
	}

	/*
	 * private void trainCorefModel (Document doc, AceDocument aceDoc, String
	 * docId) { SyntacticRelationSet relations = new SyntacticRelationSet(); if
	 * (usePA) { relations.readRelations (triplesDir + docId + triplesSuffix); }
	 * else { relations.addRelations(doc); } // event list from key ArrayList
	 * events = aceDoc.events; // system-generated event list ArrayList
	 * systemEvents = new ArrayList(); // HashMap keyIdToSystemEventMap = new
	 * HashMap(); int aceEventNo = 1; // iterate over potential anchors Vector
	 * constituents = doc.annotationsOfType("constit"); for (int i=0;
	 * i<constituents.size(); i++) { Annotation constit = (Annotation)
	 * constituents.get(i); String cat = (String) constit.get("cat"); if (cat ==
	 * "n" || cat == "v" || cat == "tv" || cat == "ven" || cat == "ving" || cat
	 * == "adj") { String anchor = EventPattern.normalizedAnchor (constit, doc,
	 * relations); Span anchorExtent = constit.span(); AceEvent event =
	 * eventAnchoredByConstituent (constit, doc, aceDoc, docId, relations,
	 * aceEventNo); event = pruneEvent (event, constit, doc, relations); if
	 * (event != null) { AceEvent keyEvent = correctEvent(anchorExtent, event,
	 * events); // if not a correct event, continue if (keyEvent == null)
	 * continue; String keyEventId = keyEvent.id; // determine which system
	 * event it should be folded into, if any Integer I = (Integer)
	 * keyIdToSystemEventMap.get(keyEventId); int systemEventIndex = (I == null)
	 * ? -1 : I.intValue(); System.out.println ("systemEventIndex = " +
	 * systemEventIndex); // compare to all events in systemEvents for (int
	 * iEvent = 0; iEvent < systemEvents.size(); iEvent++) { AceEvent priorEvent
	 * = (AceEvent) systemEvents.get(iEvent); if
	 * (!priorEvent.subtype.equals(event.subtype)) continue; // if same
	 * type/subtype, generate training example // (with outcome = whether it
	 * belongs to this systemEvent) Datum d = corefFeatures (priorEvent, event,
	 * anchor); d.setOutcome((iEvent == systemEventIndex) ? "merge" :
	 * "dontMerge"); corefFeatureWriter.println(d.toString()); } // if it should
	 * be folded in, do so; else create new event if (systemEventIndex >= 0) {
	 * // fold event into prior event AceEvent priorEvent = (AceEvent)
	 * systemEvents.get(systemEventIndex); priorEvent.arguments = mergeArguments
	 * (event.arguments, priorEvent.arguments); AceEventMention m =
	 * (AceEventMention) event.mentions.get(0); priorEvent.addMention(m);
	 * m.setId(priorEvent.id + "-" + priorEvent.mentions.size()); } else {
	 * systemEvents.add(event); keyIdToSystemEventMap.put(keyEventId, new
	 * Integer(systemEvents.size()-1)); aceEventNo++; } } } } }
	 */

	private void trainCorefModel(Document doc, AceDocument aceDoc, String docId) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations.readRelations(triplesDir + docId + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}

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
		int corefNumber = 0;
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
					corefNumber++;
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
		d.addFV("corefNumber", Integer.toString(corefNumber));

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

	/*
	 * private static boolean checkPlaceRole(String id,String
	 * priorId,AceDocument aceDoc){ AceEntity priorEntity=
	 * aceDoc.findEntity(priorId); AceEntity entity= aceDoc.findEntity(id);
	 * AceEntityMention eMention = (AceEntityMention)entity.mentions.get(0);
	 * String str = eMention.text; for (int
	 * i=0;i<priorEntity.mentions.size();i++){ AceEntityMention priorMention =
	 * (AceEntityMention)priorEntity.mentions.get(i); String priorStr =
	 * priorMention.text; StringTokenizer st = new StringTokenizer(str); while
	 * (st.hasMoreTokens()) { if (priorStr.indexOf(st.nextToken())>=0){ return
	 * true; } } } return false;
	 * 
	 * }
	 */
	// minimum confidence to consider arg a confident argument
	private static double CONFIDENT_ARG = 0.20;

	/**
	 * identify ACE events in Document 'doc' and add them to 'aceDoc'.
	 */

	public void tag(Document doc, AceDocument aceDoc, String currentDocPath,
			String docId, int choice) {
		SyntacticRelationSet relations = new SyntacticRelationSet();
		if (usePA) {
			relations
					.readRelations(triplesDir + currentDocPath + triplesSuffix);
		} else {
			relations.addRelations(doc);
		}

		eventCoref(aceDoc, doc, relations, choice,docId);
	}

	/**
	 * if 'constit' is the anchor of an event, return the event.
	 */

	AceEvent eventAnchoredByConstituent(Annotation constit, Document doc,
			AceDocument aceDoc, String docId, SyntacticRelationSet relations,
			int aceEventNo) {
		String anchor = EventPattern.normalizedAnchor(constit, doc, relations);
		Span anchorExtent = constit.span();
		// search patterns for match
		List patterns = (List) anchorMap.get(anchor);
		if (patterns == null)
			return null;
		AceEvent bestEvent = matchPatternSet(patterns, anchorExtent, anchor,
				doc, relations, aceDoc);
		if (bestEvent == null)
			return null;
		Annotation sentence = EventPattern
				.containingSentence(doc, anchorExtent);
		int slash = docId.lastIndexOf('/');
		if (slash >= 0)
			docId = docId.substring(slash + 1);
		String eventId = docId + "-EV" + aceEventNo;
		bestEvent.setId(eventId);
		AceEventMention bestMention = (AceEventMention) bestEvent.mentions
				.get(0);
		bestMention.setId(eventId + "-1");
		// collect additional arguments using statistical model
		if (useArgumentModel)
			collectArguments(bestEvent, bestMention, doc, aceDoc, relations);
		if (confidentRoleCount(bestEvent, CONFIDENT_ARG) > 0) {
			// System.out.println ("Tagged event = " + bestEvent);
			return bestEvent;
		} else {
			return null;
		}
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

	AceEvent pruneEvent(AceEvent event, Annotation constit, Document doc,
			SyntacticRelationSet relations) {
		if (event == null)
			return null;
		if (confidentRoleCount(event, 0.999) != 0)
			return event;
		String anchor = EventPattern.normalizedAnchor(constit, doc, relations);
		EventPattern pattern = (EventPattern) patternMatched;
		Datum d = eventFeatures(anchor, event, pattern);
		double eventProb = eventModel.eval(d.toArray())[eventModel
				.getIndex("event")];
		if (eventProb < EVENT_PROBABILITY_THRESHOLD)
			return null;
		AceEventMention emention = (AceEventMention) event.mentions.get(0);
		emention.confidence = eventProb;
		// System.out.println ("Accepted event with prob " + eventProb);
		ArrayList args = event.arguments;
		Iterator it = args.iterator();
		while (it.hasNext()) {
			AceEventArgument ea = (AceEventArgument) it.next();
			if (ea.confidence * eventProb < ARGUMENT_PROBABILITY_THRESHOLD)
				it.remove();
		}
		args = emention.arguments;
		it = args.iterator();
		while (it.hasNext()) {
			AceEventMentionArgument ea = (AceEventMentionArgument) it.next();
			if (ea.confidence * eventProb < ARGUMENT_PROBABILITY_THRESHOLD)
				it.remove();
		}
		return event;
	}

	EventPattern patternMatched;

	AceEvent matchPatternSet(List patterns, Span anchorExtent, String anchor,
			Document doc, SyntacticRelationSet relations, AceDocument aceDoc) {
		patternMatched = null;
		AceEvent bestEvent = null;
		EventPattern bestPattern = null;
		int bestMatchScore = 0;
		for (int j = 0; j < patterns.size(); j++) {
			EventPattern ep = (EventPattern) patterns.get(j);
			// if (ep.patternType.equals("PA")) continue; // <<< SYNTAX only
			// try event pattern
			AceEvent event = ep.match(anchorExtent, anchor, doc, relations,
					aceDoc);
			// if it matches
			if (event != null && ep.evaluation.test(event.arguments) > 0) {
				// and fills some argument role
				int score = ep.getMatchScore()
						+ ep.evaluation.test(event.arguments);
				if (ep.patternType != null && ep.patternType.equals("SYNTAX"))
					score += 50; // <<<<<<<< favor SYNTAX over PA
				if (event.arguments.size() >= 0 && score > bestMatchScore) {
					bestMatchScore = score;
					bestEvent = event;
					bestPattern = ep;
				}
			}
		}
		patternMatched = bestPattern;
		// if (bestMatchScore > 0) System.out.println ("Best match score = " +
		// bestMatchScore);
		return bestEvent;
	}

	/**
	 * returns the set of argument roles filled in 'event'.
	 */

	private Set<String> rolesFilledInEvent(AceEvent event) {
		ArrayList args = event.arguments;
		Set<String> roles = new HashSet<String>();
		for (int i = 0; i < args.size(); i++) {
			AceEventArgument arg = (AceEventArgument) args.get(i);
			roles.add(arg.role);
		}
		return roles;
	}

	/**
	 * returns a count of the number of arguments whose confidence is above a
	 * threshold.
	 */

	private int confidentRoleCount(AceEvent event, double threshold) {
		ArrayList args = event.arguments;
		int count = 0;
		for (int i = 0; i < args.size(); i++) {
			AceEventArgument arg = (AceEventArgument) args.get(i);
			if (arg.confidence > threshold)
				count++;
		}
		return count;
	}

	/**
	 * returns a set of the values of the arguments of the event.
	 */

	private HashSet argumentValues(AceEvent event) {
		ArrayList args = event.arguments;
		HashSet values = new HashSet();
		for (int i = 0; i < args.size(); i++) {
			AceEventArgument arg = (AceEventArgument) args.get(i);
			values.add(arg.value);
		}
		return values;
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
			String textFile = ace + (useParser ? "perfect-parses/" : "")
					+ currentDocPath;
			String xmlFile = ace
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String outputFile = home + "output/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			// for evaluation --------------
			/*
			 * textFile = home + "Ace 05/eval/allparses/" + currentDocPath;
			 * xmlFile = home + "Ace 05/eval/ACE05_diagdata_v1/english/" +
			 * currentDocPath.replaceFirst(".sgm", ".entities.apf.xml");
			 * outputFile = home + "Ace 05/eval/output/" +
			 * currentDocPath.replaceFirst(".sgm", ".apf.xml");
			 */
			// ------------
			ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();
			cuny.blender.englishie.ace.Ace.monocase = cuny.blender.englishie.ace.Ace.allLowerCase(doc);
			if (choice<4)
				Control.processDocument(doc, null, false, 0);
			AceDocument aceDoc = new AceDocument(textFile, xmlFile);
			ArrayList events = constructEvent(aceDoc, aceDoc.docID);
			AceEventCompare comp = new AceEventCompare();
			Collections.sort(events, comp);
			aceDoc.events = events;
			// aceDoc.events.clear();
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

	/*
	 * static double tcoef1 = 0.46; static double tcoef2 = 0.21; static double
	 * tcoef3 = 0.16; static double tcoef4 = 0.11; static double tcoef5 = 0.12;
	 * static double acoef1 = 0.38; static double acoef2 = 0.16; static double
	 * acoef3 = 0.07; static double acoef4 = 0.1;
	 */

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

	static double CLUSTER_THRESHOLD = 0.85;

	public void eventCoref(AceDocument aceDoc, Document doc,
			SyntacticRelationSet relations, int choice,String docId) {
		ArrayList<AceEvent> events = aceDoc.events;
		AceEventCompare comp = new AceEventCompare();
		Collections.sort(events, comp);
		System.out.println("eventCoref: " + events.size() + " event mentions");
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
		System.out.println("eventCoref: " + aceDoc.events.size() + " events");
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

	public void report(String reportFile) throws IOException {
		PrintWriter reportWriter = new PrintWriter(new FileWriter(reportFile));
		Set anchors = anchorMap.keySet();
		Iterator iter = anchors.iterator();
		while (iter.hasNext()) {
			String anchor = (String) iter.next();
			reportWriter.println("\n" + anchor
					+ " ================================");
			List patterns = (List) anchorMap.get(anchor);
			for (int j = 0; j < patterns.size(); j++) {
				EventPattern ep = (EventPattern) patterns.get(j);
				reportWriter.println(ep.toString());
			}
		}
		reportWriter.close();
	}

	/**
	 * write out all the patterns to file 'fileName' in a form which can be
	 * reloaded by the 'load' method.
	 */

	public void save(String fileName) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(fileName));
		Set anchors = anchorMap.keySet();
		Iterator iter = anchors.iterator();
		while (iter.hasNext()) {
			String anchor = (String) iter.next();
			List patterns = (List) anchorMap.get(anchor);
			for (int j = 0; j < patterns.size(); j++) {
				EventPattern ep = (EventPattern) patterns.get(j);
				ep.write(writer);
			}
		}
		writer.close();
	}

	/**
	 * load an event pattern set in the form saved by the 'save' method.
	 */

	public void load(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		int patternCount = 0;
		String line = reader.readLine();
		while (line != null) {
			EventPattern ep = new EventPattern(reader);
			addBasicPattern(ep.anchor, ep);
			patternCount++;
			line = reader.readLine();
		}
		System.out.println(patternCount + " patterns loaded");
	}

	static final String fileListTrain = home + "trainfilelist";
	static final String fileListTrain1 = ace + "perfect-parses/nw-tail.txt";
	static final String fileListTrain2 = ace + "perfect-parses/bn-tail.txt";
	static final String fileListTrain3 = ace + "perfect-parses/bc-tail.txt";
	static final String fileListTrain4 = ace + "perfect-parses/wl-tail.txt";
	static final String fileListTrain5 = ace + "perfect-parses/cts-tail.txt";
	static final String fileListTrain6 = ace + "perfect-parses/un-tail.txt";
	// static final String fileListTest = ace + "perfect-parses/nw-head.txt";
	// static final String fileListTest = ace + "perfect-parses/bn-head.txt";
	// static final String fileListTest = ace + "perfect-parses/bc-head.txt";
	// static final String fileListTest = ace + "perfect-parses/un-head.txt";
	// static final String fileListTest = ace + "perfect-parses/head6.txt";
	static final String fileListTest = home + "testfilelist";
	// ---- for eval
	// static final String fileListTest = home + "Ace 05/eval/evalFiles";

	static final String eventFeatureFileName = aceModels
			+ "eventFeatureFile.log";
	static final String eventModelFileName = aceModels + "eventModel.log";
	// static final String evTypeFeatureFileName = ace +
	// "evTypeFeatureFile.log";
	// static final String evTypeModelFileName = ace + "evTypeModel.log";

	static final String corefFeatureFileName = aceModels
			+ "corefFeatureFile2.log";
	static final String corefModelFileName = aceModels + "corefModel.log";

	static final String argFeatureFileName = aceModels + "argFeatureFile.log";
	static final String argModelFileName = aceModels + "argModel.log";
	static final String roleFeatureFileName = aceModels + "roleFeatureFile.log";
	static final String roleModelFileName = aceModels + "roleModel.log";

	static final String argFeatureHalfFileName = aceModels
			+ "argFeatureHalfFile.log";
	static final String argModelHalfFileName = aceModels + "argModelHalf.log";
	static final String roleFeatureHalfFileName = aceModels
			+ "roleFeatureHalfFile.log";
	static final String roleModelHalfFileName = aceModels + "roleModelHalf.log";

	static final String eventPatternFile = aceModels + "eventPatterns.log";
	static final String eventReportFile = aceModels + "eventPatternReport.log";
	static final String halfEventPatternFile = aceModels
			+ "eventPatternsHalf.log";
	static final boolean savePatterns = true;
	static final boolean trainArgModels = true;

	static final boolean trainPatterns = true;
	static final boolean trainEvModel = true;
	static final boolean trainCorefModel = true;
	static final boolean evaluate = true;

	static String mucFileName1 = home + "coref_muc1";
	static String bcubeFileName1 = home + "coref_bcube1";
	static String eacfFileName1 = home + "coref_eacf1";

	static String mucFileName2 = home + "coref_muc2";
	static String bcubeFileName2 = home + "coref_bcube2";
	static String eacfFileName2 = home + "coref_eacf2";

	static String mucFileName3 = home + "coref_muc3";
	static String bcubeFileName3 = home + "coref_bcube3";
	static String eacfFileName3 = home + "coref_eacf3";
	
	static String mucFileName4 = home + "coref_muc4";
	static String bcubeFileName4 = home + "coref_bcube4";
	static String eacfFileName4 = home + "coref_eacf4";
	
	static String mucFileName5 = home + "coref_muc5";
	static String bcubeFileName5 = home + "coref_bcube5";
	static String eacfFileName5 = home + "coref_eacf5";
	static String fileList = home + "totalfilelist1";

	static void splitCorpus(int fold, ArrayList v) throws IOException {
		PrintStream trainWriter = new PrintStream(new FileOutputStream(
				fileListTrain));
		PrintStream testWriter = new PrintStream(new FileOutputStream(
				fileListTest));
		for (int i = 0; i < v.size(); i++) {
			String str = (String) v.get(i);

			if (i >= fold * 50 && i < (fold + 1) * 50)
				testWriter.println(str);
			else
				trainWriter.println(str);
		}
		trainWriter.close();
		testWriter.close();
	}

	static public void splitCorpus(String corpus, int splitCount,
			String fileList1, String fileList2) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(corpus));
		int docCount = 0;
		String currentDocPath;
		ArrayList<String> fileName = new ArrayList();
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
		}
		reader.close();
		if (splitCount > docCount) {
			System.out
					.println("Error: Total number of test documents is less than total number of document\n");
			System.out.println("Total number of documents:"
					+ Integer.toString(docCount)
					+ "\nNo of test document required:"
					+ Integer.toString(splitCount) + "\n");
			return;
		}
		int i;
		boolean[] flag = new boolean[docCount];
		for (i = 0; i < docCount; i++)
			flag[i] = false;
		Random rand = new Random();
		for (i = 0; i < splitCount; i++) {
			int index = Math.abs(rand.nextInt()) % docCount;
			if (!flag[index])
				flag[index] = true;
			else
				i--;
		}
		reader = new BufferedReader(new FileReader(corpus));
		int pos = 0;
		PrintStream trainWriter = new PrintStream(new FileOutputStream(
				fileList1));
		PrintStream testWriter = new PrintStream(
				new FileOutputStream(fileList2));

		while ((currentDocPath = reader.readLine()) != null) {
			if (flag[pos])
				testWriter.println(currentDocPath);
			else
				trainWriter.println(currentDocPath);
			pos++;
		}
		reader.close();
	}

	public static void main(String[] args) throws IOException {
	//	TfIdf.genTfIdf();
		// splitCorpus(fileList,60,fileListTrain,fileListTest);
		if (useParser)
			JetTest.initializeFromConfig("props/ace use parses.properties");
		else
			JetTest.initializeFromConfig("props/MEace06.properties");
		Ace.gazetteer = new Gazetteer();
		Ace.gazetteer.load("data/loc.dict");
		Pat.trace = false;
		Resolve.trace = false;
		AceDocument.ace2005 = true;

		//loadStopwordFile();

		ArrayList<String> v = new ArrayList<String>();
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			v.add(currentDocPath);
		}
		reader.close();

	//	splitCorpus(fileList,500,home+"testfilelist",home+"totalfilelist1");
		FileWriter bw11 = new FileWriter(mucFileName1);
		FileWriter bw21 = new FileWriter(bcubeFileName1);
		FileWriter bw31 = new FileWriter(eacfFileName1);
		FileWriter bw12 = new FileWriter(mucFileName2);
		FileWriter bw22 = new FileWriter(bcubeFileName2);
		FileWriter bw32 = new FileWriter(eacfFileName2);
		FileWriter bw13 = new FileWriter(mucFileName3);
		FileWriter bw23 = new FileWriter(bcubeFileName3);
		FileWriter bw33 = new FileWriter(eacfFileName3);
		FileWriter bw14 = new FileWriter(mucFileName4);
		FileWriter bw24 = new FileWriter(bcubeFileName4);
		FileWriter bw34 = new FileWriter(eacfFileName4);
		FileWriter bw15 = new FileWriter(mucFileName5);
		FileWriter bw25 = new FileWriter(bcubeFileName5);
		FileWriter bw35 = new FileWriter(eacfFileName5);
		bw11.close();
		bw21.close();
		bw31.close();
		bw12.close();
		bw22.close();
		bw32.close();
		bw13.close();
		bw23.close();
		bw33.close();
		bw14.close();
		bw24.close();
		bw34.close();
		bw15.close();
		bw25.close();
		bw35.close();
		for (int runs = 0; runs < 1; runs++) {
			// shuffle the set of files first
			Collections.shuffle(v);

			bw11 = new FileWriter(mucFileName1, true);
			bw21 = new FileWriter(bcubeFileName1, true);
			bw31 = new FileWriter(eacfFileName1, true);
			bw12 = new FileWriter(mucFileName2, true);
			bw22 = new FileWriter(bcubeFileName2, true);
			bw32 = new FileWriter(eacfFileName2, true);
			bw13 = new FileWriter(mucFileName3, true);
			bw23 = new FileWriter(bcubeFileName3, true);
			bw33 = new FileWriter(eacfFileName3, true);
			bw14 = new FileWriter(mucFileName4, true);
			bw24 = new FileWriter(bcubeFileName4, true);
			bw34= new FileWriter(eacfFileName4, true);
			bw15 = new FileWriter(mucFileName5, true);
			bw25 = new FileWriter(bcubeFileName5, true);
			bw35= new FileWriter(eacfFileName5, true);
			
			bw11.write("runs=" + Integer.toString(runs) + "\n");
			bw21.write("runs=" + Integer.toString(runs) + "\n");
			bw31.write("runs=" + Integer.toString(runs) + "\n");
			bw12.write("runs=" + Integer.toString(runs) + "\n");
			bw22.write("runs=" + Integer.toString(runs) + "\n");
			bw32.write("runs=" + Integer.toString(runs) + "\n");
			bw13.write("runs=" + Integer.toString(runs) + "\n");
			bw23.write("runs=" + Integer.toString(runs) + "\n");
			bw33.write("runs=" + Integer.toString(runs) + "\n");
			bw14.write("runs=" + Integer.toString(runs) + "\n");
			bw24.write("runs=" + Integer.toString(runs) + "\n");
			bw34.write("runs=" + Integer.toString(runs) + "\n");
			bw15.write("runs=" + Integer.toString(runs) + "\n");
			bw25.write("runs=" + Integer.toString(runs) + "\n");
			bw35.write("runs=" + Integer.toString(runs) + "\n");
			
			bw11.close();
			bw21.close();
			bw31.close();
			bw12.close();
			bw22.close();
			bw32.close();
			bw13.close();
			bw23.close();
			bw33.close();
			bw14.close();
			bw24.close();
			bw34.close();
			bw15.close();
			bw25.close();
			bw35.close();
			
			System.out.println("runs= " + Integer.toString(runs));

			for (int fold = 0; fold < 1; fold++) {

				// split corpus first
				splitCorpus(fold, v);
				bw11 = new FileWriter(mucFileName1, true);
				bw21 = new FileWriter(bcubeFileName1, true);
				bw31 = new FileWriter(eacfFileName1, true);
				bw12 = new FileWriter(mucFileName2, true);
				bw22 = new FileWriter(bcubeFileName2, true);
				bw32 = new FileWriter(eacfFileName2, true);
				bw13 = new FileWriter(mucFileName3, true);
				bw23 = new FileWriter(bcubeFileName3, true);
				bw33 = new FileWriter(eacfFileName3, true);
				bw14 = new FileWriter(mucFileName4, true);
				bw24 = new FileWriter(bcubeFileName4, true);
				bw34= new FileWriter(eacfFileName4, true);
				bw15 = new FileWriter(mucFileName5, true);
				bw25 = new FileWriter(bcubeFileName5, true);
				bw35= new FileWriter(eacfFileName5, true);
				
				bw11.write("folds=" + Integer.toString(fold) + "\n");
				bw21.write("folds=" + Integer.toString(fold) + "\n");
				bw31.write("folds=" + Integer.toString(fold) + "\n");
				bw12.write("folds=" + Integer.toString(fold) + "\n");
				bw22.write("folds=" + Integer.toString(fold) + "\n");
				bw32.write("folds=" + Integer.toString(fold) + "\n");
				bw13.write("folds=" + Integer.toString(fold) + "\n");
				bw23.write("folds=" + Integer.toString(fold) + "\n");
				bw33.write("folds=" + Integer.toString(fold) + "\n");
				bw14.write("folds=" + Integer.toString(fold) + "\n");
				bw24.write("folds=" + Integer.toString(fold) + "\n");
				bw34.write("folds=" + Integer.toString(fold) + "\n");
				bw15.write("folds=" + Integer.toString(fold) + "\n");
				bw25.write("folds=" + Integer.toString(fold) + "\n");
				bw35.write("folds=" + Integer.toString(fold) + "\n");
				System.out.println("folds= " + Integer.toString(fold));

				ETGraphCorefV2 et = new ETGraphCorefV2();

				for (FEATURE_SET = 0; FEATURE_SET < 3; FEATURE_SET ++) {
					System.out.println("FEATURE_SET= " + Integer.toString(FEATURE_SET));
				
					if (trainCorefModel) {
					et.trainCorefModel();
				}
				if (evaluate) {
					et.loadAllModels(aceModels);
				
					//for (CLUSTER_THRESHOLD = 0.85; CLUSTER_THRESHOLD < 0.95; CLUSTER_THRESHOLD += 0.05) {
							
				
					evaluateEventTagger(et, 0);
			
					/*MUCCorefEval ee1 = new MUCCorefEval();
					ee1.evalCoref(fileListTest);
				
					bw14.write(Double.toString(ee1.precision) + "\t"
						+ Double.toString(ee1.recall) + "\t"
						+ Double.toString(ee1.F) + "\n");
				// bw.write("BCube\n");
					BCubeCorefEval ee2 = new BCubeCorefEval();
					ee2.evalCoref(fileListTest);
					bw24.write(Double.toString(ee2.precision) + "\t"
						+ Double.toString(ee2.recall) + "\t"
						+ Double.toString(ee2.F) + "\n");*/
					CEAFCorefEval ee3 = new CEAFCorefEval();
					ee3.evalCoref(fileListTest);
					bw34.write(Double.toString(ee3.precision) + "\t"
							+ Double.toString(ee3.recall) + "\t"
							+ Double.toString(ee3.F) + "\n");
				
					CEAFCorefEval1 ee2 = new CEAFCorefEval1();
					ee2.evalCoref(fileListTest);
					bw34.write(Double.toString(ee2.precision) + "\t"
							+ Double.toString(ee2.recall) + "\t"
							+ Double.toString(ee2.F) + "\n");
					
					
				}
				}
				//}
				// }
				bw11.close();
				bw21.close();
				bw31.close();
				bw12.close();
				bw22.close();
				bw32.close();
				bw13.close();
				bw23.close();
				bw33.close();
				bw14.close();
				bw24.close();
				bw34.close();
				bw15.close();
				bw25.close();
				bw35.close();
				
			}
		}
	}

	private void trainEventModel() throws IOException {
		anchorMap.clear();

		processOnlyOddDocuments = true;
		// set arg models
		argFeatureWriter = new PrintStream(new FileOutputStream(
				argFeatureHalfFileName));
		roleFeatureWriter = new PrintStream(new FileOutputStream(
				roleFeatureHalfFileName));
		// train patterns on odd documents
		for (int pass = 0; pass <= 1; pass++) {
			train(fileListTrain, pass);
			/*
			 * et.train (fileListTrain1, pass); et.train (fileListTrain2, pass);
			 * et.train (fileListTrain3, pass); et.train (fileListTrain4, pass);
			 * et.train (fileListTrain5, pass); et.train (fileListTrain6, pass);
			 */
		}
		save(halfEventPatternFile);
		// build arg models (half) and load them
		argFeatureWriter.close();
		roleFeatureWriter.close();
		buildClassifierModel(argFeatureHalfFileName, argModelHalfFileName);
		buildClassifierModel(roleFeatureHalfFileName, roleModelHalfFileName);
		argModel = loadClassifierModel(argModelHalfFileName);
		roleModel = loadClassifierModel(roleModelHalfFileName);
		// set ev model
		eventFeatureWriter = new PrintStream(new FileOutputStream(
				eventFeatureFileName));
		// et.evTypeFeatureWriter = new PrintStream (new FileOutputStream
		// (evTypeFeatureFileName));
		// train event model on even documents
		processOnlyOddDocuments = false;
		processOnlyEvenDocuments = true;
		train(fileListTrain, 2);
		/*
		 * et.train (fileListTrain1, 2); et.train (fileListTrain2, 2); et.train
		 * (fileListTrain3, 2); et.train (fileListTrain4, 2); et.train
		 * (fileListTrain5, 2); et.train (fileListTrain6, 2);
		 */
		// build ev model
		eventFeatureWriter.close();
		// et.evTypeFeatureWriter.close();
		buildClassifierModel(eventFeatureFileName, eventModelFileName);
		// buildClassifierModel(evTypeFeatureFileName, evTypeModelFileName);
	}

	/*
	 * private void trainCorefModel () throws IOException { anchorMap.clear();
	 * argModel = loadClassifierModel(argModelHalfFileName); roleModel =
	 * loadClassifierModel(roleModelHalfFileName); eventModel =
	 * loadClassifierModel(eventModelFileName); load (halfEventPatternFile); //
	 * set coref model corefFeatureWriter = new PrintStream (new
	 * FileOutputStream (corefFeatureFileName)); // train coref model on even
	 * documents, using models from odd documents processOnlyEvenDocuments =
	 * true; train (fileListTrain, 3);
	 * 
	 * // build coref model corefFeatureWriter.close();
	 * buildClassifierModel(corefFeatureFileName, corefModelFileName); }
	 */

	public void trainCorefModel() throws IOException {

		// set coref model
		corefFeatureWriter = new PrintStream(new FileOutputStream(
				corefFeatureFileName));
		// train coref model on even documents, using models from odd documents
		// processOnlyEvenDocuments = true;
		train(fileListTrain, 3);

		// build coref model
		corefFeatureWriter.close();
		buildClassifierModel(corefFeatureFileName, corefModelFileName);
	}

	public static void evaluateEventTagger(ETGraphCorefV2 et, int choice)
			throws IOException {
		// et.assessPatterns (fileListTest);
		et.tag(fileListTest, choice);
	}

	public void loadAllModels(String modelDir) throws IOException {
		/*
		 * eventModel = loadClassifierModel(modelDir + "eventModel.log"); //
		 * evTypeModel = loadClassifierModel(evTypeModelFileName); argModel =
		 * loadClassifierModel(modelDir + "argModel.log"); roleModel =
		 * loadClassifierModel(modelDir + "roleModel.log");
		 */
		corefModel = loadClassifierModel(modelDir + "corefModel.log");
	}

}