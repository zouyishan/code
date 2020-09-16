
package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;


import org.w3c.dom.*;
import org.xml.sax.*;

import cuny.blender.englishie.nlp.tipster.Span;

import javax.xml.parsers.*;

/**
 *  an Ace Entity Name, with information from the APF ACE key.
 */

public class AceEntityName {
	/**
	 *  the extent of the mention, with start and end positions based on
	 *  ACE offsets (excluding XML tags).
	 */
	public Span extent;

	public String text;

	public AceEntityName (Span extent, String fileText) {
		this.extent = AceEntityMention.convertSpan(extent, fileText);
		text = fileText.substring(this.extent.start(), this.extent.end()+1);
	}

	/**
	 *  create an AceEntityName from the information in the APF file.
	 *  @param nameElement   the XML element from the APF file containing
	 *                       information about this mention
	 *  @param fileText      the text of the document, including XML tags
	 */

	public AceEntityName (Element nameElement, String fileText) {
		extent = AceEntityMention.decodeCharseq(nameElement);
		text = fileText.substring(extent.start(), extent.end()+1).replace('\n',' ').replace('"',' ');
	}

	/*
	 *  write the name in the format required for an APF file
	 */

	void write (BufferedWriter w) throws IOException {
		String cleanText = text;
		cleanText = cleanText.replaceAll("&", "&amp;");
		cleanText = cleanText.replaceAll("<", "&lt;");
		cleanText = cleanText.replaceAll(">", "&gt;");
		cleanText = cleanText.replaceAll("\"", "&quot;");
		cleanText = cleanText.replaceAll("'", "&apos;");
		cleanText = cleanText.replaceAll("\n", " ");
		     
		w.append ("      <name NAME=\"" + cleanText + "\">\n");
		AceEntityMention.writeCharseq (w, extent, text);
		w.append ("      </name>\n");
	}
}
