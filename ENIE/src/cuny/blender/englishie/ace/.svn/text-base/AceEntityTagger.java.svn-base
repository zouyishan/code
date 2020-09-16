
package cuny.blender.englishie.ace;

//Author:       Ralph Grishman
//Date:         July 10, 2003

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.*;
import cuny.blender.englishie.nlp.format.InvalidFormatException;
import cuny.blender.englishie.nlp.format.PTBReader;
import cuny.blender.englishie.nlp.lex.Tokenizer;
import cuny.blender.englishie.nlp.lisp.*;
import cuny.blender.englishie.nlp.parser.AddSyntacticRelations;
import cuny.blender.englishie.nlp.parser.ParseTreeNode;
import cuny.blender.englishie.nlp.parser.SynFun;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.time.TimeAnnotator;
import cuny.blender.englishie.nlp.time.TimeMain;
import cuny.blender.englishie.nlp.tipster.*;
import cuny.blender.englishie.nlp.zoner.SpecialZoner;

/**
 *  procedures for generating ACE output for a Jet document.
 */
public class AceEntityTagger {

	public static boolean useParser = false;
	static final boolean useParseCollection = false;
	public static boolean perfectMentions = false;
	public static boolean perfectEntities = false;
	static final boolean asr = false;

	public static boolean preferRelations = false;
	public static boolean preferEntities = !preferRelations;

	public static boolean entityTrace = false;

	public static boolean monocase = false;

	static PrintWriter apf;
	public static ExternalDocument doc;
	public static Gazetteer gazetteer;
	static int aceEntityNo;
	static HashMap aceTypeDict;
	static final String suffix = ".apf.xml";
	
	/**
	 *  the DOCID field of the document currently being processed.  The file name
	 *  is used if no DOCID field is present.
	 */
	static String docId;
	static String sourceType = "text";
	// relational model
	public static RelationPatternSet eve = null;
	static ETGraphCorefSystem eventTagger = null;

 	static String docDir;
	static String outputDir;
	static String parseDir;
	static int docCount = 0;
	static String parseSuffix = ".sgm.sent.chout";
	/**
	 *  if true, output confidence for events and event arguments as part of APF
	 *  (non-standard APF).
	 */
	static boolean writeEventConfidence = false;

	/**
	 *  generate ACE annotation files (in APF) format for a list of documents.
	 *  Takes four to eight command line parameters:                             <BR>
	 *  propertyFile:  Jet properties file                                     <BR>
	 *  filelist:  a list of the files to be processed                         <BR>
	 *  docDirectory:  the path of the directory containing the input files    <BR>
	 *  outDirectory:  the path of the directory in which APF files are to
	 *                 be written                                              <BR>
	 *  parseDir:      the path of the directory containing parse trees
	 *                 [optional]                                              <BR>
	 *  glarfDir:      the path of the directory containing GLARF tuples
	 *                 [optional]                                              <BR>
	 *  glarfSuffix:   the file suffix for GLARF files                         <BR>
	 *  parseSuffix:   the file suffix for parse files                         <BR>
	 *  For each <I>file</I> in <I>filelist</I>, the document is read from
	 *  <I>docDirectory</file</I>.sgm and the APF file is written to
	 *  <I>outDirectory</file</I>.sgm.apf
	 */

	public static void main (String[] args) throws IOException {
		// get arguments
		String home = args[0];
		String propertyFile = home + args[1];//args[0];//"../props/MEace06.properties"
		String fileList = home+args[2];//home+ "../KBPEntity/docid_to_file_mapping2_2.tab";//args[1];
		docDir = home+args[3];//args[2];//data/wb
		outputDir = home+args[4];//args[3];"../KBPEntity/weboutput/"
		
		parseDir = null;

		// initialize Jet
		JetTest.initializeFromConfig (propertyFile);
		// get version of Ace
		String aceYear = JetTest.getConfig("Ace.Year");
		if (aceYear != null) {
			if (aceYear.equals("2003")) {
				AceDocument.ace2004 = false;
				AceDocument.ace2005 = false;
			} else if (aceYear.equals("2004")) {
				AceDocument.ace2004 = true;
				AceDocument.ace2005 = false;
			} else if (aceYear.equals("2005")) {
				AceDocument.ace2004 = true;
				AceDocument.ace2005 = true;
			} else { 
				System.err.println ("Unrecognized value " + aceYear + " for Ace.Year");
			}
		}
		if (JetTest.getConfig("Ace.PerfectEntities") != null) {
			perfectMentions = true;
			perfectEntities = true;
		}
		// set ACE mode for reference resolution
		Resolve.ACE = true;
		// Resolve.useMaxEnt = true;
		// load entity type dictionary
		EDTtype.readTypeDict();
		EDTtype.readGenericDict ();
		// load values dictionary
		String valueDictFile = JetTest.getConfigFile("Ace.Value.fileName");
		if (valueDictFile != null) {
			FindAceValues.readTypeDict(valueDictFile);
		} else {
			System.out.println ("Ace:  no value dictionary file name specified in config file.");
			System.out.println ("      Will not tag values.");
		}
		// load time annotation patterns
		String timeRulesFileName = JetTest.getConfigFile("Time.fileName");
		if (timeRulesFileName != null) {
			TimeMain.timeAnnotator = new TimeAnnotator(timeRulesFileName);
		} else {
			System.out.println ("Ace:  no time rules file name specified in config file.");
			System.out.println ("      Will not tag time expressions.");
		}
		
		// turn off traces
		Pat.trace = false;
		Resolve.trace = false;
		boolean debug = false;
		// open list of files
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		// debug = false ==> try to catch all exceptions and continue
		
		final String d = " : ";
		String currentDocPath;
		String currentDocPathBase;
		while ((currentDocPath = reader.readLine()) != null) {
			currentDocPathBase = currentDocPath.replace(".sgm", "");
			
			docCount++;
			if (debug) {
				processFile(currentDocPath,currentDocPathBase);
			} else {
				try {
					processFile(currentDocPath,currentDocPathBase);
				} catch (Exception e) {
					System.err.println("Error: " + fileList + d + docCount + d + currentDocPath + e.toString());
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	/**
	 *  if <CODE>paramName</CODE> is a numeric-valued parameter in the Jet configuration
	 *  file, returns that value as a <CODE>double</CODE>, else returns
	 *  <CODE>defaultValue</CODE>.
	 */
	
	static double getConfigDouble (String paramName, double defaultValue) {
		String paramValue = JetTest.getConfig(paramName);
		if (paramValue == null)
			return defaultValue;
		try {
			return Double.parseDouble(paramValue);
		} catch (NumberFormatException e) {
			System.err.println ("Error in Jet parameter " + paramName + " = " + paramValue);
			System.err.println (e.toString());
			return defaultValue;
		}
	}

	static void processFile (String currentDocPath,String currentDocPathBase) throws IOException{
		System.out.println ("\nProcessing document " + docCount + ": " + currentDocPath);
		String currentDocFileName = (new File(currentDocPath)).getName();

		doc = new ExternalDocument("sgml", docDir + currentDocPath/*.replace(".sgm", ".rel")*/);
		if (perfectMentions) {
			// doc = new ExternalDocument("sgml", docDir + "perfect parses/" + currentDocPath);
			String textFile = docDir + currentDocPath;
			String keyFile = docDir + currentDocPathBase + ".apf.xml";
			// String keyFile = docDir + currentDocPathBase + ".mentions.apf.xml"; //<< for corefeval
			// String keyFile = docDir + currentDocPathBase + ".entities.apf.xml"; //<< for rdreval
			AceDocument keyDoc = new AceDocument(textFile, keyFile);
			PerfectAce.buildEntityMentionMap (doc, keyDoc);
		}
		doc.setAllTags(true);
		// doc.setEmptyTags(new String[] {"W", "TURN"});
		doc.open();
		doc.stretchAll();
		// process document
		monocase = allLowerCase(doc);
		System.out.println (">>> Monocase is " + monocase);
		
		//gazetteer.setMonocase(monocase);
		cuny.blender.englishie.nlp.hmm.BigramHMMemitter.useBigrams = monocase;
		cuny.blender.englishie.nlp.hmm.HMMstate.otherPreference = monocase ? 1.0 : 0.0;
		if (doc.annotationsOfType("dateline") == null && 
		    doc.annotationsOfType("textBreak") == null)
			SpecialZoner.findSpecialZones (doc);
		Control.processDocument (doc, null, docCount == -1, docCount);
		if (parseDir != null && !parseDir.equals("-")) {
			// read in parses
			String parseFileName = parseDir + currentDocPathBase + parseSuffix;
			try {
				File f = new File(parseFileName);
				PTBReader ptbReader = new PTBReader();
				List<ParseTreeNode> trees = ptbReader.loadParseTrees (f);
				List<Integer> offsets = ptbReader.getOffsets();
				ptbReader.addAnnotations (trees, offsets, doc, "sentence",  new Span(0, doc.text().length()), true);
			} catch (InvalidFormatException e) {
				System.err.println ("Format error in reading parse tree from file " + parseFileName);
			} catch (IOException e) {
				System.err.println ("IO error reading parse tree from file " + parseFileName);
				System.err.println ("   " + e);
			}
			// resolve references
			Vector<Annotation> textSegments = doc.annotationsOfType("TEXT");
			if (textSegments != null) {
				for (Annotation textSegment : textSegments) {
					Vector<Annotation> sentences = doc.annotationsOfType("sentence", textSegment.span());
					if (sentences != null) {
						for (Annotation sentence : sentences) {
							AddSyntacticRelations.annotate(doc, sentence.span());
							Resolve.references (doc, sentence.span());
						}
					}
				}
			}
		}
		tagReciprocalRelations(doc);
		// new View (doc, docCount);
		// open apf file
		docId = getDocId(doc);
		if (docId == null)
			docId = removeFileExtension(currentDocFileName);
		sourceType = "text";
		Vector doctypes = doc.annotationsOfType("DOCTYPE");
		if (doctypes != null && doctypes.size() > 0) {
			Annotation doctype = (Annotation) doctypes.get(0);
			String source = (String) doctype.get("SOURCE");
			if (source != null)
				sourceType = source;
		}
		String apfFileName = outputDir + currentDocPathBase + suffix;
		BufferedWriter apf = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(apfFileName), "UTF-8"));
		// create empty Ace document
		AceDocument aceDoc =
			new AceDocument(currentDocFileName, sourceType, docId, doc.text());
		// build entities
		buildAceEntities (doc, aceDoc);
		
		// write APF file
		aceDoc.write(apf, doc);
		// break;
	}

	/**
	 *  returns the document ID of Document <CODE>doc</CODE>, if found,
	 *  else returns <CODE>null</CODE>.  It looks for <br>
	 *  the text of a <B>DOCID</B> annotation (as provided with ACE documents); <br>
	 *  the text of a <B>DOCNO</B> annotation (as provided with TDT4 and TDT5 documents); <br>
	 *  the <B>DOCID</B> feature of a <B>document</B> annotation
	 *       (for GALE ASR transcripts); or <br>
	 *  the <B>id</B> feature of a <B>doc</B> annotation (for newer LDC documents).
	 */

	public static String getDocId (Document doc) {
		Vector docIdAnns = doc.annotationsOfType ("DOCID");
		if (docIdAnns != null && docIdAnns.size() > 0) {
			Annotation docIdAnn = (Annotation) docIdAnns.get(0);
			return doc.text(docIdAnn).trim();
		}
		docIdAnns = doc.annotationsOfType ("DOCNO");
		if (docIdAnns != null && docIdAnns.size() > 0) {
			Annotation docIdAnn = (Annotation) docIdAnns.get(0);
			return doc.text(docIdAnn).trim();
		}
		Vector docAnns = doc.annotationsOfType ("document");
		if (docAnns != null && docAnns.size() > 0) {
			Annotation docAnn = (Annotation) docAnns.get(0);
			Object docId = docAnn.get("DOCID");
			if (docId != null && docId instanceof String) {
				return (String) docId;
			}
		}
		docAnns = doc.annotationsOfType ("DOC");
		if (docAnns != null && docAnns.size() > 0) {
			Annotation docAnn = (Annotation) docAnns.get(0);
			Object docId = docAnn.get("id");
			if (docId != null && docId instanceof String) {
				return (String) docId;
			}
		}
		return null;
	}

	public static void setPatternSet (String fileName) throws IOException {
		eve = new RelationPatternSet();
		eve.load(fileName, 0);
	}

	public static boolean allLowerCase (Document doc) {
		Vector<Annotation> textSegments = doc.annotationsOfType ("TEXT");
		Span span;
		if (textSegments != null && textSegments.size() > 0)
			span = textSegments.get(0).span();
		else
			span = doc.fullSpan();		
		return allLowerCase (doc, span);
	}

	public static boolean allLowerCase (Document doc, Span span) {
		boolean allLower = true;
		boolean allUpper = true;
		for (int i=span.start(); i<span.end(); i++) {
			if (Character.isUpperCase(doc.charAt(i)))
				allLower = false;
			if (Character.isLowerCase(doc.charAt(i)))
				allUpper = false;
			}
		return allLower || allUpper;
	}

	static String[] functionWord =
		{"a ", "an ", "the ", "his ", "her ", "its ",
		 "against ", "as ", "at ", "by ", "due to ", "for ", "from ", "in ", 
		 "into ", "of ", "over ", "to ", "with ", "within ",
		 "and ", "not ", "or ",
		 "can ", "be ", "will "};
		 
	/**
	 *  returns true if Span <CODE>span</CODE> of Document <CODE>doc</CODE>
	 *  appears to be capitalized as a title:  if there are no words
	 *  beginning with a lower-case letter except for a small list of
	 *  function words (articles, possessive pronouns, prepositions, ...).
	 */

	public static boolean titleCase (Document doc, Span span) {
		boolean allTitle = true;
		String text = doc.text(span);
	loop:
		for (int i=0; i<text.length()-1; i++) {
			if (Character.isWhitespace(text.charAt(i)) &&
			    Character.isLowerCase(text.charAt(i+1))) {
				for (int j=0; j<functionWord.length; j++)
					if (text.startsWith(functionWord[j], i+1))
						continue loop;
				allTitle = false;
			}
		}
		return allTitle;
	}

	/**
	 *  returns a file path after removing the extension (period and following
	 *  characters), if any, from the file name.
	 */

	static String removeFileExtension (String path) {
		String fileName = (new File(path)).getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0 && i < fileName.length()-1) {
			i+= path.length() - fileName.length();
			return path.substring(0, i);
		} else {
			return path;
		}
	}
	
	/**
	 *  create ACE entities from entity annotations produced by refres.
	 */

	public static void buildAceEntities (Document doc, AceDocument aceDoc) {
		aceEntityNo = 0;
		LearnRelations.resetMentions(); // for relations
		String docText = doc.text();
		Vector entities = doc.annotationsOfType("entity");
		if (entities != null) {
			for (int ientity=0; ientity<entities.size(); ientity++) {
				AceEntity aceEntity =
					buildEntity(((Annotation) entities.get(ientity)), ientity, doc, docText);
				if (aceEntity != null)
					aceDoc.addEntity(aceEntity);
			}
		}
		System.err.println ("buildAceEntities: generated " + aceDoc.entities.size() + " entities");
	}

	/**
	 *  create an AceEntity from <CODE>entity</CODE>.  If the
	 *  entity is not a valid EDT type, nothing is written.
	 */

	private static AceEntity buildEntity (Annotation entity, int ientity,
			Document doc, String docText) {
		Vector mentions = (Vector) entity.get("mentions");
		Annotation firstMention = (Annotation) mentions.get(0);
		String aceTypeSubtype;
		aceTypeSubtype = EDTtype.getTypeSubtype (doc, entity, firstMention);
		String aceType = EDTtype.bareType(aceTypeSubtype);
		if (entityTrace)
			System.out.println ("Type of " + Resolve.normalizeName(doc.text(firstMention)) + " is " + aceTypeSubtype);
		if (aceType.equals("OTHER")) return null;
		String aceSubtype = EDTtype.subtype(aceTypeSubtype);
		// don't tag items generic for Ace 2004 or later
		boolean generic = !AceDocument.ace2004 && isGeneric(doc, firstMention);

		if (generic) {
			System.out.println ("Identified generic mention " +
			                    Resolve.normalizeName(doc.text(firstMention)));
		}

		aceEntityNo++;
		if (entityTrace)
			System.out.println("Generating ace entity " + aceEntityNo +
			                   " (internal entity " + ientity + ") = " +
			                   Resolve.normalizeName(doc.text(firstMention)) +
			                   " [" + aceType + "]");
		String entityID = docId + "-" + aceEntityNo;
		AceEntity aceEntity = new AceEntity (entityID, aceType, aceSubtype, generic);
		for (int imention=0; imention<mentions.size(); imention++) {
			Annotation mention = (Annotation) mentions.get(imention);
			Annotation head = Resolve.getHeadC(mention);
			String mentionID = entityID + "-" + imention;
			AceEntityMention aceMention = buildMention (mention, head, mentionID, aceType, doc, docText);
			aceEntity.addMention(aceMention);
			LearnRelations.addMention (aceMention);
			boolean isNameMention = aceMention.type == "NAME";
			if (isNameMention) {
				aceEntity.addName(new AceEntityName(head.span(), docText));
			}
		}
		return aceEntity;
	}

	static final String[] locativePrepositions = {"in", "at", "to", "near"};

	/**
	 *  write the information for <CODE>mention</CODE> with head <CODE>head</CODE>
	 *  in APF format.
	 */

	private static AceEntityMention buildMention
			(Annotation mention, Annotation head, String mentionID, String entityType,
				Document doc, String docText) {
		Span mentionSpan = mention.span();
		Span headSpan = head.span();
		String mentionType = mentionType(head, mention);
		AceEntityMention m =
			new AceEntityMention (mentionID, mentionType, mentionSpan, headSpan, docText);
		if (entityType.equals("GPE")) {
			if (perfectMentions)
				m.role = PerfectAce.getMentionRole(head);
			else {
				String prep = governingPreposition(doc, mention);
				if ((prep != null && in(prep, locativePrepositions)) ||
				     // for location in dateline
				     Resolve.sentenceSet.sentenceNumber(mention.start()) == 0) {
					m.role = "LOC";
				} else {
					m.role = "GPE";
				}
			}
		}
		return m;
	}

	/**
	 *  determine the mention type of a mention (NOMINAL, PRONOUN, or NAME)
	 *  from its <CODE>head</CODE>.
	 */

	private static String mentionType (Annotation head, Annotation mention) {
		if (perfectMentions)
			return PerfectAce.getMentionType (head);
		String cat = (String) head.get("cat");
		String mcat = (String) mention.get("cat");
		// if (mention.get("preName-1") != null || mention.get("nameMod-1") != null ||
		//     cat == "adj")
		// 	return "PRE";
		// else
		if (cat == "n" || cat == "title" || cat == "tv" || cat == "v")
			return "NOMINAL";
		else if (cat == "pro" || cat == "det" /*for possessives - his, its */ ||
			       cat == "adj" || cat == "ven" || cat == "q" ||
		         cat == "np" /* for headless np's */ || cat == "wp" || cat == "wp$")
			return "PRONOUN";
		else // cat == "name"
			if (mention.get("nameWithModifier") != null)
				return "NOMINAL";
			else
				return "NAME";
	}

	static final String[] genericFriendlyDeterminers =
		{"no", "neither", "any", "many", "every", "each"};
	static final String[] clearGenericPronouns =
		{"everyone", "anyone", "everybody", "anybody",
		"something", "who", "whoever", "whomever",
		"wherever", "whatever", "where"};

	private static boolean isGeneric (Document doc, Annotation mention) {
		Annotation ngHead = getNgHead (mention);
		Annotation headC = Resolve.getHeadC (mention);
		if (headC.get("cat") == "n") {
			// is always generic
			String det = SynFun.getDet(mention);
			if (det != null && in(det, genericFriendlyDeterminers))
				return true;
			// OR is generic head
			if (!EDTtype.hasGenericHead(doc, mention)) return false;
			// if (pa.get("number") != "plural") return false;
			if (ngHead.get("poss") != null || det == "poss") return false;
			if (ngHead.get("quant") != null || det == "q") return false;
			//    AND is in generic environment
			Annotation vg = governingVerbGroup(mention);
			if (vg != null) {
				FeatureSet vpa = (FeatureSet) vg.get("pa");
				if (vpa != null && vpa.get("tense") != "past"
				                && vpa.get("aspect") == null) {
				    System.out.println ("Governing verb group = " + doc.text(vg));
				    System.out.println ("Verb group pa = " + vpa);
					return true;
				}
			}
			return false;
		} else if (headC.get("cat") == "pro" || headC.get("cat") == "np"
		                                     || headC.get("cat") == "det") {
			String pronoun = SynFun.getHead(doc, mention);
			return in(pronoun,clearGenericPronouns) ||
			       in(pronoun,genericFriendlyDeterminers);  // << added Oct. 10
		} else /* head is a name */ return false;
	}

	private static Annotation getNgHead (Annotation ng) {
		Annotation hd = ng;
		while (true) {
			ng = (Annotation) hd.get("headC");
			if (ng == null) return hd;
			if (ng.get("cat") != "np"  || ng.get("possPrefix") == "true") return hd;
			hd = ng;
		}
	}

	/**
	 *  tag the document <CODE>doc</CODE> for time expressions, and create
	 *  (and add to <CODE>aceDoc</CODE> an AceTimex object for each time
	 *  expression.
	 */

	private static void buildTimex (Document doc, AceDocument aceDoc, String docId) {
		TimeMain.processDocument (doc);
		Vector v = doc.annotationsOfType("TIMEX2");
		if (v != null) {
			System.out.println ( v.size() + " time expressions found.");
			for (int i=0; i<v.size(); i++) {
				Annotation ann = (Annotation) v.get(i);
				String docText = doc.text();
				String timeId = docId + "-T" + i;
				String val = (String) ann.get("VAL");
				if (val == null)
					System.err.println ("TIMEX " + timeId + " has no VAL.");
				AceTimexMention mention =
					new AceTimexMention (timeId + "-1", ann.span(), docText);
				AceTimex timex = new AceTimex (timeId, val);
				timex.addMention(mention);
				aceDoc.addTimeExpression(timex);
			}
		}
	}

	private static boolean in (Object o, Object[] array) {
		for (int i=0; i<array.length; i++)
			// if (array[i] == o) return true;
			if (array[i] != null && array[i].equals(o)) return true;
		return false;
	}

	/**
	 *  assigns reciprocal relations subject-1 and object-1
	 */

	public static void tagReciprocalRelations (Document doc) {
		Vector constits = doc.annotationsOfType("constit");
		if (constits != null) {
			for (int j = 0; j < constits.size();  j++) {
				Annotation ann = (Annotation) constits.elementAt(j);
				if (ann.get("subject") != null) {
					Annotation subject = (Annotation) ann.get("subject");
					if (subject.get("subject-1") == null) {
						subject.put("subject-1", ann);
					}
				}
				if (ann.get("object") != null) {
					Annotation object = (Annotation) ann.get("object");
					if (object.get("object-1") == null) {
						object.put("object-1", ann);
					}
				}
			}
		}
	}

	static Annotation governingVerbGroup (Annotation ann) {
		Annotation governingConstituent;
		if (ann.get("subject-1") != null) {
			governingConstituent = (Annotation) ann.get("subject-1");
		} else if (ann.get("object-1") != null) {
			governingConstituent = (Annotation) ann.get("object-1");
		} else return null;
		return Resolve.getHeadC(governingConstituent);
	}

	static String governingPreposition (Document doc, Annotation ann) {
		Annotation pp = (Annotation) ann.get("p-obj-1");
		if (pp == null)
			return null;
		Annotation[] ppChildren = (Annotation[]) pp.get("children");
		if (ppChildren.length != 2)
			return null;
		Annotation in = ppChildren[0];
		String prep = doc.text(in).trim();
		return prep;
	}

	static int getACEoffset (int posn) {
		return posn;
	}

	static int getJetOffset (int posn) {
		return posn;
	}

	// for David's code

	 /**
     * @param doc2
     */
    public static void addParentLinks(ExternalDocument doc2) {
        Vector allAnnotations = doc2.annotationsOfType("constit");
        if (allAnnotations == null) return;
        Iterator anns = allAnnotations.iterator();
        while (anns.hasNext()) {
            Annotation constit = (Annotation) anns.next();
            Annotation[] children = ParseTreeNode.children(constit);
            if (children == null) continue;
            for (int c=0; c < children.length; ++c)
                if (children[c] != null) children[c].put("parent", constit);
        }
    }
}
