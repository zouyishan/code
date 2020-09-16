
//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkil
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;


import org.w3c.dom.*;
import org.xml.sax.*;

import cuny.blender.englishie.nlp.tipster.Span;

import javax.xml.parsers.*;

/**
 *  a mention of an (ACE) Timex2 time expression, with information from the APF ACE key.
 *  The 'id', 'extent', 'jetExtent', and 'text' fields are
 *  inherited from its superclass, AceMention.
 */

public class AceTimexMention extends AceMention {

	AceTimex timex;
	/**
	 *  create a new Timex mention with the specified id and extent.
	 */

	public AceTimexMention (String id, Span extent, String fileText) {
		this.id = id;
		this.extent = AceEntityMention.convertSpan(extent, fileText);
		jetExtent = extent;
		text = fileText.substring(this.extent.start(), this.extent.end()+1);
	}

	/**
	 *  create an AceTimexMention from the information in the APF file.
	 *  @param mentionElement the XML element from the APF file containing
	 *                       information about this mention
	 *  @param fileText      the text of the document, including XML tags
	 */

	public AceTimexMention (Element mentionElement, String fileText) {
		id = mentionElement.getAttribute("ID");
		NodeList extents = mentionElement.getElementsByTagName("extent");
		Element extentElement = (Element) extents.item(0);
		if (extentElement == null) {
			System.err.println ("*** AceTimexMention:  no extent.");
		} else {
			extent = AceEntityMention.decodeCharseq(extentElement);
			jetExtent = AceEntityMention.aceSpanToJetSpan(extent, fileText);
			if (extent.start() <= extent.end() && extent.end() < fileText.length()) {
				text = fileText.substring(extent.start(), extent.end()+1);
			} else {
				text = "";
				System.err.println ("*** AceTimexMention:  invalid extent.");
			}
		}
	}

	public AceEventArgumentValue getParent () {
		return timex;
	}

	public String getType () {
		return timex.getType(); // "Time";
	}

	/**
	 *  writes the AceTimexMention in APF format to 'w'.
	 * @throws IOException 
	 */

	void write (BufferedWriter w) throws IOException {
		w.append ("    <timex2_mention ID=\"" + id + "\">\n");
		w.append ("      <extent>\n");
		AceEntityMention.writeCharseq (w, extent, text);
		w.append ("      </extent>\n");
		w.append ("    </timex2_mention>\n");
	}

}
