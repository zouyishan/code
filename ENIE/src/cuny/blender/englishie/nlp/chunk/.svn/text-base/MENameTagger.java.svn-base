
package cuny.blender.englishie.nlp.chunk;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.Ace;
import cuny.blender.englishie.ace.Gazetteer;
import cuny.blender.englishie.nlp.Console;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.hmm.*;
import cuny.blender.englishie.nlp.lex.*;
import cuny.blender.englishie.nlp.lisp.*;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.scorer.*;
import cuny.blender.englishie.nlp.tipster.*;
import cuny.blender.englishie.nlp.zoner.SentenceSplitter;

/**
 *  a Named Entity tagger based on a maximum entropy token classifier.
 */

public class MENameTagger implements NameTagger {

	public MaxEntNE mene;
	HMMannotator annotator;

	static String[] tagsToScore = {"ENAMEX"};

	static final String home = "C:/Documents and Settings/Ralph Grishman/My Documents/";
	static final String featureFile = home + "HMM/NE/ACEnameMEfeatures.txt";


	/**
	 *  creates a new MENameTagger.
	 */

	public MENameTagger () {
		mene = new MaxEntNE();
		mene.resetForTraining(featureFile);
		annotator = new HMMannotator(mene);
		annotator.setBItag (true);
		annotator.setAnnotateEachToken (false);
	}

	/**
	 *  initialize the tagger for training by loading from file <CODE>tagTableFile</CODE>
	 *  the list of valid annotations.
	 */

	public void initialize (String tagTableFile) {
		annotator.readTagTable (tagTableFile);
	}


	/**
	 *  train the tagger using the collection of Documents 'trainingCollection'.
	 *  The documents should have a TEXT zone marked;  training is done on all sentences
	 *  within this zone.
		 */

	public void train (String trainingCollection) throws IOException {
		DocumentCollection trainCol = new DocumentCollection(trainingCollection);
		trainCol.open();
		for (int i=0; i<trainCol.size(); i++) {
			ExternalDocument doc = trainCol.get(i);
			doc.setAllTags (true);
			doc.open();
			doc.stretchAll();
			System.out.println ("Training from " + doc.fileName());
			mene.newDocument();
			Vector textSegments = doc.annotationsOfType ("TEXT");
			Iterator it = textSegments.iterator ();
			while (it.hasNext ()) {
				Annotation ann = (Annotation)it.next ();
				Span textSpan = ann.span ();
				// check document case
				Ace.monocase = Ace.allLowerCase(doc);
				// System.out.println (">>> Monocase is " + Ace.monocase);
				// split into sentences
				SentenceSplitter.split (doc, textSpan);
			}
			Vector sentences = doc.annotationsOfType ("sentence");
			if (sentences == null) continue;
			Iterator is = sentences.iterator ();
			while (is.hasNext ()) {
				Annotation sentence = (Annotation)is.next ();
				Span sentenceSpan = sentence.span();
				Tokenizer.tokenize (doc, sentenceSpan);
				Lexicon.annotateWithDefinitions(doc, sentenceSpan.start(), sentenceSpan.end());
				annotator.trainOnSpan (doc, sentenceSpan);
			}
			//  free up space taken by annotations on document
			doc.clearAnnotations();
		}
	}

	/**
	 *  store the data associated with this tagger to file 'fileName'.
	 *  This data consists of the tag tables (recording the different annotations
	 *  which may be assigned by this tagger) and the data used by the
	 *  max ent token classifier.
	 */

	public void store (String fileName) throws IOException {
		BufferedWriter out = new BufferedWriter
			(new OutputStreamWriter
				(new FileOutputStream(fileName), JetTest.encoding));
		annotator.writeTagTable (out);
		out.write ("endtags");
		out.newLine ();
		mene.store (out);
	}

	/**
	 *  load the data associated with this tagger from file 'fileName'.
	 */

	public void load (String fileName) throws IOException {
		BufferedReader in = new BufferedReader
			(new InputStreamReader
				(new FileInputStream(fileName), JetTest.encoding));
		annotator.readTagTable(in);
		mene.load(in);
	}

	/**
	 *  tag document <CODE>doc</CODE> for named entities.
	 */

	public void tagDocument (Document doc) {
		mene.newDocument();
    Vector textSegments = doc.annotationsOfType ("TEXT");
    Iterator it = textSegments.iterator ();
    while (it.hasNext ()) {
        Annotation ann = (Annotation)it.next ();
        Span textSpan = ann.span ();
        SentenceSplitter.split (doc, textSpan);
    }
    Vector sentences = doc.annotationsOfType ("sentence");
  	Iterator is = sentences.iterator ();
  	while (is.hasNext ()) {
    	Annotation sentence = (Annotation)is.next ();
    	Span sentenceSpan = sentence.span();
    	Tokenizer.tokenize (doc, sentenceSpan);
			Lexicon.annotateWithDefinitions(doc, sentenceSpan.start(), sentenceSpan.end());
    	tag (doc, sentenceSpan);
	}
	}

	/**
	 *  tag span 'span' of Document 'doc' with Named Entity annotations.
	 */

	public void tag (Document doc, Span span) {
		annotator.annotateSpan (doc, span);
	}

	public static void main (String[] args) throws IOException {
		new AnnotationColor(home);
		Gazetteer gazetteer = new Gazetteer();
		gazetteer.load("data/loc.dict");
		// trainForMUC ();
		ace04trainTest ();
		// ace04test ();
	}

	public static void trainForMUC () throws IOException {
		MENameTagger nt = new MENameTagger();
		nt.initialize ("data/MUCnameTags.txt");
		String trainingCollection1 =  home + "HMM/NE/NE train Collection.txt";
		String testCollection = home + "HMM/NE/NE test Collection.txt";
		String keyCollection = home + "HMM/NE/NE key Collection.txt";
		// nt.load ("C:\\My Documents\\HMM\\NE\\nameBaseHMM.txt");
		// nt.train (trainingCollection1);
		// nt.mene.createModel();
		// nt.store (home + "HMM/NE/MUCnameMEmodel.txt");
		nt.load (home + "HMM/NE/MUCnameMEmodel.txt");
		NEScorer.scoreCollection (nt, testCollection, keyCollection, tagsToScore);
	}

	public static void ace04trainTest () throws IOException {
		MENameTagger nt = new MENameTagger();
		nt.initialize ("acedata/ACEnameTags.txt");
		String trainingCollection1 =  home + "HMM/NE/ACE BBN Collection.txt";
		String trainingCollection2 =  home + "HMM/NE/ACE training Collection.txt";
		// String trainingCollection3 =  home + "HMM/NE/ACE aug03 Collection.txt";
		String trainingCollection4 =  home + "ACE/training04 nwire 21andup ne.txt";
		String trainingCollection5 =  home + "ACE/training04 bnews 21andup ne.txt";

		String testCollection = home + "ACE/training04 nwire 20 sgm.txt";
		String keyCollection = home + "ACE/training04 nwire 20 ne.txt";
		for (int pass=1; pass <= 2; pass++) {
		MaxEntNE.pass = pass;
		MaxEntNE.trainingDocCount = 0;
		nt.train (trainingCollection1);
		nt.train (trainingCollection2);
		nt.train (trainingCollection4);
		nt.train (trainingCollection5);
		}
		nt.mene.createModel();
		nt.store ("acedata/ACEnameMEmodel.txt");
		// nt.load (home + "HMM/NE/ACEnameMEmodel.txt");
		NEScorer.scoreCollection (nt, testCollection, keyCollection, tagsToScore);
	}

	public static void ace04test () throws IOException {
		MENameTagger nt = new MENameTagger();
		nt.load ("acedata/ACEnameMEmodel.txt");
		String testCollection = home + "ACE/training04 nwire 20 sgm.txt";
		String keyCollection = home + "ACE/training04 nwire 20 ne.txt";
		NEScorer.scoreCollection (nt, testCollection, keyCollection, tagsToScore);
	}
}
