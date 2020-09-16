
//Title:        JET
//Version:      1.30
//Copyright:    Copyright (c) 2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package cuny.blender.englishie.nlp.zoner;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.Ace;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.tipster.*;

/**
 *  write a Document out, one sentence per line, with named entity tags.
 *  Each sentence is preceded by the character offset of the sentence in
 *  the document.
 */

public class TaggedSentenceWriter {

	static final String home =
	  "C:/Documents and Settings/Ralph Grishman/My Documents/";
	static final String ACEdir =
	  home + "ACE 05/V4/"; // "ACE 05/eval/";
	static String propertyFile =
	  "props/ace 07 tag names.properties";
	static String dataDir =
		ACEdir;
	static String outputDir =
		home + "Ace 07/ne sents/";
	static String fileList =
		ACEdir + "allSgm.txt"; // "evalSgm.txt";
	
	/**
	 *  generate sentence list files for a list of documents.
	 *  Takes 3 or 4 command line parameters:                                  <BR>
	 *  filelist:  a list of the files to be processed                         <BR>
	 *  dataDir:  the path of the directory containing the document            <BR>
	 *  outputDir:  the path of the directory to contain the output            <BR>
	 *  writeXML:  if present, sentences are written in an XML format with
	 *            offsets only                                                 <BR>
	 *  For each <I>file</I> in <I>filelist</I>, the document is read from
	 *  <I>dataDir/file</I>;  if the input file has no sentence tags, the
	 *  sentence splitter is invoked to add sentence tags;  and then
	 *  the sentence file is written to <I>outputDir/file</I>.sent .  
	 *  Only sentences within TEXT XML elements are written.
	 */

	public static void main (String[] args) throws IOException {
		if (args.length > 0) {
			if (args.length != 4) {
				System.out.println ("TaggedSentenceWriter must have 4 arguments:");
				System.out.println ("  propertyFile  filelist  dataDirectory  outputDirectory");
				System.exit(1);
			}
			propertyFile = args[0];
			fileList  = args[1];
			dataDir   = args[2];
			outputDir = args[3];
		}
		JetTest.initializeFromConfig (propertyFile);
		processFileList (fileList);
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
			String textFileName = dataDir + currentDoc;
			ExternalDocument doc = new ExternalDocument("sgml", textFileName);
			doc.setAllTags(true);
			doc.open();
			Control.processDocument (doc, null, false, docCount);
			doc.shrink("ENAMEX");
			String sentFileName = outputDir + currentDoc + ".nesent";
			PrintWriter writer = new PrintWriter (new FileWriter (sentFileName));
			writeSents (doc, currentDoc, writer);
			writer.close();
		}
	}

	private static void writeSents (ExternalDocument doc, String currentDocPath, PrintWriter writer) {
		doc.annotateWithTag ("TEXT");
		SpecialZoner.findSpecialZones (doc);
		Vector textSegments = doc.annotationsOfType ("TEXT");
		if (textSegments == null) {
			System.out.println ("No <TEXT> in " + currentDocPath + ", skipped.");
			return;
		}
		Vector priorSentences = doc.annotationsOfType ("sentence");
		if (priorSentences == null || priorSentences.size() == 0) {
			Iterator it = textSegments.iterator ();
			while (it.hasNext ()) {
				Annotation ann = (Annotation)it.next ();
				Span textSpan = ann.span ();
				// check document case
				Ace.monocase = Ace.allLowerCase(doc);
				// split into sentences
				SentenceSplitter.split (doc, textSpan);
			}
		}
		Vector sentences = doc.annotationsOfType ("sentence");
		if (sentences == null) return;
		Iterator is = sentences.iterator ();
		while (is.hasNext ()) {
			Annotation sentence = (Annotation)is.next ();
			Span sentenceSpan = sentence.span();
			String sentenceText = doc.writeSGML("ENAMEX", sentenceSpan)
			                         .toString().trim().replace('\n',' ');
			writer.println (sentenceSpan.start() + " " + sentenceText);
		}
	}
}
