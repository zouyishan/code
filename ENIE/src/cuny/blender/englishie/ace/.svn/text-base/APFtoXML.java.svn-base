
package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lisp.*;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;
import cuny.blender.englishie.nlp.zoner.SentenceSplitter;
import cuny.blender.englishie.nlp.zoner.SpecialZoner;


/**
 *  convert a set of ACE files to XML files containing in-line markup for
 *  the entity mentions, names, and time expressions specified by the APF file.  
 *  This version can handle 2002, 2003, 2004, or 2005 format APF.
 */

class APFtoXML {

	static String ACEdir, outputDir;
	static String year;
	static String apfExtension;
	static String outputExtension;
	static Set<String> flags = new HashSet<String>();
	
	static Gazetteer gazetteer;

	static TreeSet<String> unknownPre = new TreeSet<String>();
	static HashMap<String, String> preDict = new HashMap<String, String>();

  /**
   *  convert a list of APF files to XML files containing in-line markup for
   *  sentences, timex, mentions, entity types, and names.  Takes the following 
   *  arguments:
   *  <ul>
   *  <li> year:  the year (2002, 2003, 2004, 2005) the APF file was created,
   *       which determines its format and file extension
   *  <li> ACEdir:    the directory containing the text and APF files
   *  <li> NEdir:     the directory containing the NE files
   *  <li> filelist:  a file containing a list of document names
   *  <li> gazetteer: (for 2004 files only) a Jet gazetteer, used to resolve
   *       the name/noun distinction for GPE words categorized as PRE
   *  <li> PREdict:   (for 2004 files only) a name vs. noun list, used to resolve
   *       the name/noun distinction for non-GPE words categorized as PRE
   *  <li> flag ... : one or more of 'sentences', 'timex', 'mentions', 'types'
   *                  or 'names', indicating the information to be included
   *                  in the output file
   *  </ul>
   */
   
	public static void main (String [] args) throws IOException  {
		if (args.length == 0) argErr();
		JetTest.encoding = "UTF-8";
		year = args[0];
		apfExtension = ".apf.xml";
		AceDocument.ace2004 = false;
		AceDocument.ace2005 = false;
		int requiredArgs = 5;
		if (year.equals("2002")) {
			apfExtension = ".sgm.tmx.rdc.xml";
		} else if (year.equals("2003")) {
		} else if (year.equals("2004")) {
			requiredArgs = 7;
			String gazFile = args[4];
			String preDict = args[5];
			gazetteer = new Gazetteer();
			gazetteer.load(gazFile);
			loadPreDict (preDict);
			AceDocument.ace2004 = true;
		} else if (year.equals("2005")) {
			AceDocument.ace2004 = true;
			AceDocument.ace2005 = true;
		} else {
			System.err.println ("Invalid year:  must be 2002-2005");
			System.exit (1);
		}
		if (args.length <= requiredArgs) argErr();
		ACEdir = args[1];
		if (!ACEdir.endsWith("/")) ACEdir += "/";
		outputDir = args[2];
		if (!outputDir.endsWith("/")) outputDir += "/";
		String fileList = args[3];
		outputExtension = args[4];
		for (int i = requiredArgs; i < args.length; i++)
			flags.add(args[i]);
		processFileList (fileList);
		if (year.equals("2004")) {
			System.out.println ("\nUnclassified items:  " + unknownPre.size());
			for (String word : unknownPre)
				System.out.println (word);
		}
	}
	
	private static void argErr () {
		System.err.println ("APFtoXML arguments:");
		System.err.println 
		  ("  year apf-directory  output-directory  filelist output-suffix [gazetteer pre-dictionary] flag ...");
		System.err.println ("gazetteer and pre-dictionary needed for year = 2004");
		System.err.println ("possible flags:  sentences timex mentions types names");
		System.exit (1);
	}
		
	private static void loadPreDict (String dictFile) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dictFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String preType = line.substring(0,1);
				String word = line.substring(2);
				preDict.put(word, preType);
			}
		} catch (IOException e) {
			System.err.print("Unable to load dictionary due to exception: ");
			System.err.println(e);
		}
	}

	private static void processFileList (String fileList) throws IOException {
		// open list of files
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println ("\nProcessing document " + docCount + ": " + currentDoc);
			String textFileName = ACEdir + currentDoc + ".sgm";
			ExternalDocument doc = new ExternalDocument("sgml", textFileName);
			doc.setAllTags(true);
			if (year.equals("2003") || year.equals("2004"))
				doc.setEmptyTags(new String[] {"TURN"});
			doc.open();
			boolean monocase = Ace.allLowerCase(doc);
			if (year.equals("2004"))
				gazetteer.setMonocase(monocase);
			String APFfileName = ACEdir + currentDoc + ".sgm.apf";
			AceDocument aceDoc = new AceDocument(textFileName, APFfileName);
			if (flags.contains("sentences"))
				addSentences (doc);
			if (flags.contains("timex"))
				addTimexTags (doc, aceDoc);
			if (flags.contains("mentions"))
				addMentionTags (doc, aceDoc);
			if (flags.contains("names"))
				addENAMEXtags (doc, aceDoc);
			doc.setSGMLwrapMargin(0);
			doc.saveAs(outputDir, currentDoc + ".sgm.iapf");
		}
	}
	
	static void addSentences (Document doc) {
		SpecialZoner.findSpecialZones (doc);
		Vector<Annotation> textSegments = doc.annotationsOfType ("TEXT");
		if (textSegments == null) {
			System.out.println ("No <TEXT> in document");
			return;
		}
		for (Annotation ann : textSegments) {
			Span textSpan = ann.span ();
			// check document case
			Ace.monocase = Ace.allLowerCase(doc);
			// split into sentences
			SentenceSplitter.split (doc, textSpan);
		}
		Vector<Annotation> sentences = doc.annotationsOfType ("sentence");
		if (sentences != null) {
			int sentNo = 0;
			for (Annotation sentence : sentences) {
				sentNo++;
				sentence.put("ID", "SENT-" + sentNo);
			}
		}
		doc.removeAnnotationsOfType ("dateline");
		doc.removeAnnotationsOfType ("textBreak");
		doc.shrink("sentence");
	}
	
	static void addTimexTags (Document doc, AceDocument aceDoc) {
		List<AceTimex> timeExpressions = aceDoc.timeExpressions;
		for (AceTimex timex : timeExpressions) {
			AceTimexMention mention = (AceTimexMention) timex.mentions.get(0);
			Span aceSpan = mention.extent;
			Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
			FeatureSet features = new FeatureSet();
			if (timex.val != null && !timex.val.equals(""))
				features.put ("val", timex.val);
			if (timex.anchorVal != null && !timex.anchorVal.equals(""))
				features.put ("anchor_val", timex.anchorVal);
			if (timex.anchorDir != null && !timex.anchorDir.equals(""))
				features.put ("anchor_dir", timex.anchorDir);
			if (timex.set != null && !timex.set.equals(""))
				features.put ("set", timex.set);
			if (timex.mod != null && !timex.mod.equals(""))
				features.put ("mod", timex.mod);
			doc.annotate ("timex2", jetSpan, features);
		}
	}

	/**
	 *  write 'fileText' out as file 'XMLfileName' with ENAMEX tags for the
	 *  names in the document
	 */

	static void addENAMEXtags (Document doc, AceDocument aceDoc) {
		ArrayList entities = aceDoc.entities;
		for (int i=0; i<entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			ArrayList names = entity.names;
			for (int j=0; j<names.size(); j++) {
				AceEntityName name = (AceEntityName) names.get(j);
				Span aceSpan = name.extent;
				Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
				doc.annotate ("ENAMEX", jetSpan, new FeatureSet("TYPE", entity.type));
			}
			// for 2004 we have to examine PRE mentions and decide which are names
			if (year.equals("2004")) {
				ArrayList mentions = entity.mentions;
				for (int j=0; j<mentions.size(); j++) {
					AceEntityMention  mention = (AceEntityMention) mentions.get(j);
					String htext = Resolve.normalizeName(mention.headText);
					String[] mentionName = Gazetteer.splitAtWS(htext);
					String preClass = (String) preDict.get(htext.toLowerCase());
					if (mention.type.equals("PRE")) {
						if (gazetteer.isNationality(mentionName) || gazetteer.isLocation(mentionName) ||
						    "N".equals(preClass)) {
							Span aceSpan = mention.head;
							Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
							doc.annotate ("ENAMEX", jetSpan, new FeatureSet("TYPE", entity.type));
						} else if (preClass != null) {
							// do nothing
						} else {
							System.out.println ("Unclassified PRE: " + mention.text + " {" + mention.headText + ")");
							unknownPre.add(htext.toLowerCase());
						}
					}
				}
			}
		}
	}
	
	/**
	 *  generate mention annotations (with entity numbers) based on the ACE
	 *  entities and mentions.
	 */

	static void addMentionTags (Document doc, AceDocument aceDoc) {
		ArrayList entities = aceDoc.entities;
		for (int i=0; i<entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			ArrayList mentions = entity.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceEntityMention mention = (AceEntityMention) mentions.get(j);
				// we compute a jetSpan not including trailing whitespace
				Span aceSpan = mention.head;
				// skip mentions in ChEnglish APF not aligned to any English text
				if (aceSpan.start() < 0) continue;
				Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
				FeatureSet features = new FeatureSet("entity", new Integer(i));
				if (flags.contains("types")) {
					features.put("type", entity.type.substring(0,3));
					if (entity.subtype != null)
						features.put("subtype", entity.subtype);
				}					
				doc.annotate ("mention", jetSpan, features);
			}
		}
	}

}