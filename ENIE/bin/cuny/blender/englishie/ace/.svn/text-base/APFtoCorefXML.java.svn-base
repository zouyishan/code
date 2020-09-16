
package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.lisp.*;
import cuny.blender.englishie.nlp.tipster.*;


/**
 *  convert a set of ACE APF files to XML files containing mentions
 *  specified by the APF file, and the entity number for each mention.
 *  Each name is marked with "mention entity=n".  Only the head of the
 *  mention is tagged.
 */

class APFtoCorefXML {

	static final String home =
	  "C:/Documents and Settings/Ralph Grishman/My Documents/";
	static final String ACEdir =
	  // home + "ACE 05/V4/";
	  home + "ACE 05/V4/perfect-parses/";
	static final String outputDir =
		// "coref/2005/";
		"coref/2005-parsed/";
	static final String fileList =
		ACEdir + "allSgm.txt";

	public static void main (String [] args) throws IOException  {
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
			String textFileName = ACEdir + currentDoc;
			ExternalDocument doc = new ExternalDocument("sgml", textFileName);
			doc.setAllTags(true);
			doc.open();
			String APFfileName = textFileName.replaceFirst(".sgm", ".apf.xml");
			AceDocument aceDoc = new AceDocument(textFileName, APFfileName);
			addMentionTags (doc, aceDoc);
			doc.saveAs(outputDir, currentDoc + ".co.txt");
		}
	}

	static void addMentionTags (Document doc, AceDocument aceDoc) {
		ArrayList entities = aceDoc.entities;
		for (int i=0; i<entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			ArrayList mentions = entity.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceEntityMention mention = (AceEntityMention) mentions.get(j);
				Span jetSpan = mention.jetHead;
				doc.annotate ("mention", jetSpan, new FeatureSet("entity", new Integer(i)));
			}
		}
	}

}
