
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

import cuny.blender.englishie.nlp.lex.Tokenizer;
import cuny.blender.englishie.nlp.tipster.Span;

import javax.xml.parsers.*;

/**
 *  an Ace Entity Mention, with information from the APF ACE key.
 *  The 'id', 'extent', 'jetExtent', and 'text' fields are
 *  inherited from its superclass, AceMention.
 */


public class AceEntityMention extends AceMention {

	/**
	 *  the type of the mention:  NAME, NOMINAL, or PRONOUN.
	 */
	public String type;
	/**
	 *  the span of the head of the mention, with start and end positions based
	 *  on the ACE offsets (excluding XML tags).
	 */
	public Span head;
	/**
	 *  the span of the head of the mention, with start and end positions based
	 *  on Jet offsets (and so including following whitespace).
	 */
	Span jetHead;
	/**
	 *  the text of the head.
	 */
	String headText;
	/**
	 *  for entities of type GPE, the role of the mention (ORG, LOC, GPE, or PER).
	 */
	String role;
	/**
	 *  the entity of which this is a mention.
	 */
	public AceEntity entity;

	public AceEntityMention (String id, String type, Span extent, Span head,
	    String fileText) {
		this.id = id;
		this.type = type;
		this.extent = convertSpan(extent, fileText);
		jetExtent = extent;
		this.head = convertSpan(head, fileText);
		jetHead = head;
		entity = null;
		text = fileText.substring(this.extent.start(), this.extent.end()+1);
		headText = fileText.substring(this.head.start(), this.head.end()+1);
	}

	/**
	 *  create an AceEntityMention from the information in the APF file.
	 *  @param mentionElement the XML element from the APF file containing
	 *                       information about this mention
	 *  @param fileText      the text of the document, including XML tags
	 */

	public AceEntityMention (Element mentionElement, String fileText) {
		id = mentionElement.getAttribute("ID");
		type = mentionElement.getAttribute("TYPE");
		role = mentionElement.getAttribute("ROLE");
		if (role == null)
			role = "";
		// System.out.print ("Found mention " + id + " of type " + type);
		NodeList heads = mentionElement.getElementsByTagName("head");
		Element headElement = (Element) heads.item(0);
		head = decodeCharseq(headElement);
		jetHead = aceSpanToJetSpan(head, fileText);
		
		headText = fileText.substring(head.start(), head.end()+1);
		
		NodeList extents = mentionElement.getElementsByTagName("extent");
		Element extentElement = (Element) extents.item(0);
		if (extentElement == null) {
			extent = head;
			jetExtent = jetHead;
			text = headText;
		} else {
			extent = decodeCharseq(extentElement);
			jetExtent = aceSpanToJetSpan(extent, fileText);
			text = fileText.substring(extent.start(), extent.end()+1);
		}
	}


	/**
	 *  converts a jet Span to an APF span.  The end of the APF span does
	 *  not include trailing whitespace, and points to the last character,
	 *  not one past the last character.
	 */

	public static Span convertSpan (Span jetSpan, String fileText) {
		int start = jetSpan.start();
		int end = jetSpan.end() - 1;
		while (end > start && Character.isWhitespace(fileText.charAt(end)))
			end--;
		return new Span(start, end);
	}

	/**
	 *  convert a Span as used within an AceDocument (when 'end' is the position of the last
	 *  character of the sequence) to a Span as used within Jet (where 'end' is one past the
	 *  last whitespace following the sequence).
	 */

	static Span aceSpanToJetSpan (Span aceSpan, String fileText) {
		if (aceSpan == null) return null;
		int start = aceSpan.start();
		int aceEnd = aceSpan.end();
		int jetEnd = Tokenizer.skipWS (fileText, aceEnd+1, fileText.length());
		Span jetSpan = new Span (start, jetEnd);
		return jetSpan;
	}

	/**
	 *  returns the entity of which this is a mention.
	 */

	public AceEventArgumentValue getParent () {
		return entity;
	}

	public String getType () {
		return entity.type;
	}

	public Span getJetHead () {
		return jetHead;
	}

	public String getHeadText () {
		return headText;
	}

	/**
	 *  writes the AceEntityMention in APF format to 'w'.
	 * @throws IOException 
	 */

	void write (BufferedWriter w) throws IOException {
		String apfType = AceDocument.ace2004 ? type.substring(0,3) : type;
		w.append   ("    <entity_mention TYPE=\"" + apfType + "\" ID=\"" + id + "\"");
		if (role != null && !role.equals(""))
			w.append (" ROLE=\"" + role + "\"");
		w.append (">\n");
		w.append ("      <extent>\n");
		writeCharseq (w, extent, text);
		w.append ("      </extent>\n");
		w.append ("      <head>\n");
		writeCharseq (w, head, headText);
		w.append ("      </head>\n");
		w.append ("    </entity_mention>\n");
	}

	static Span decodeCharseq (Element e) {
		String startS, endS;
		if (AceDocument.ace2004) {
			NodeList charseqs = e.getElementsByTagName("charseq");
			Element charseq = (Element) charseqs.item(0);
			startS = charseq.getAttribute("START");
			endS = charseq.getAttribute("END");
		} else {
			startS = AceDocument.getElementText (e, "start");
			endS = AceDocument.getElementText (e, "end");
		}
		int start = Integer.parseInt(startS);
		int end = Integer.parseInt(endS);
		Span span = new Span (start, end);
		return span;
	}

	/**
	 *  writes the charseq XML element for the APF encoding of Span 's',
	 *  spanning text 'txt', to 'w'.
	 * @throws IOException 
	 */

	static void writeCharseq (BufferedWriter w, Span s, String txt) throws IOException {
		if (AceDocument.ace2004) {
			w.append   ("			<charseq START=\"" + s.start() + "\"" +
			                             " END=\"" + s.end() + "\">");
			txt = txt.replaceAll("&", "&amp;");
		    txt = txt.replaceAll("<", "&lt;");
		    txt = txt.replaceAll(">", "&gt;");
		    txt = txt.replaceAll("\"", "&quot;");
		    txt = txt.replaceAll("'", "&apos;");
			w.append   (txt);
			w.append ("</charseq>\n");
		} else {
			w.append ("        <charseq>\n");
			w.append ("          <start>" + s.start() + "</start>\n");
			w.append ("          <!-- string = \"" + txt + "\" -->\n");
			w.append ("          <end>" + s.end() + "</end>\n");
			w.append ("        </charseq>\n");
		}
	}

	/**
	 *  returns 'true' if 'o' is an AceEntityMention with the same head span.
	 */

	public boolean equals (Object o) {
 		return (o instanceof AceEntityMention) && (((AceEntityMention)o).head).equals(head);
	}

	/**
	 *  returns a positive, zero, or negative integer depending on whether the
	 *  start of the head of 'o' follows, is the same as, or precedes the head
	 *  of this AceEntityMention.
	 */

	 /* replaced by defn in AceMention
	public int compareTo (Object o) {
		if (!(o instanceof AceEntityMention)) throw new ClassCastException();
		return head.compareTo(((AceEntityMention)o).head);
	}
	*/

}
