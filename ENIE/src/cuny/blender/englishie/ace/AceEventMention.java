
//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkil
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;

import cuny.blender.englishie.nlp.tipster.*;

/**
 *  an Ace event mention, with information from the ACE key.
 */

public class AceEventMention {

	/**
	 *  the ID of the mention
	 */
	public String id;
	/**
	 *  arguments of the event mention (each of type AceEventMentionArgument)
	 */
	public ArrayList<AceEventMentionArgument> arguments =
		new ArrayList<AceEventMentionArgument>();
	/**
	 *  the span of the extent of the event, with start and end positions based
	 *  on the ACE offsets (excluding XML tags).
	 */
	public Span extent;
	
	public Span ldc_extent;
	public String ldc_text;
	/**
	 *  the span of the extent of the event, with start and end positions based
	 *  on Jet offsets (and so including following whitespace).
	 **/
	public Span jetExtent;
	/**
	 *  the text of the extent of the event mention.
	 */
	public String text;
	
	
	/**
	 *  the span of the anchor of the event, with start and end positions based
	 *  on the ACE offsets (excluding XML tags).
	 */
	public Span anchorExtent;
	/**
	 *  the span of the anchor of the event, with start and end positions based
	 *  on Jet offsets (and so including following whitespace).
	 **/
	public Span anchorJetExtent;
	/**
	 *  the text of the anchor
	 */
	public String anchorText;
	/**
	 *  our confidence in the presence of this event mention
	 */
	public double confidence = 1.0;

	public AceEvent event;
	
	public AceEventMention (String id, Span jetExtent, Span anchorJetExtent, String fileText) {
		this.id = id;
		this.arguments = new ArrayList<AceEventMentionArgument>();
		this.extent = AceEntityMention.convertSpan(jetExtent, fileText);
		this.jetExtent = jetExtent;
		this.text = fileText.substring(this.extent.start(), this.extent.end()+1);
		this.anchorExtent = AceEntityMention.convertSpan(anchorJetExtent, fileText);
		this.anchorJetExtent = anchorJetExtent;
		this.anchorText = fileText.substring(this.anchorExtent.start(), this.anchorExtent.end()+1);
	}

	/**
	 *  create an AceEventMention from the information in the APF file.
	 *
	 *  @param mentionElement the XML element from the APF file containing
	 *                       information about this mention
	 *  @param acedoc        the AceDocument to which this relation mention
	 *                       belongs
	 */

	public AceEventMention (Element mentionElement, AceDocument acedoc, String fileText) {
		id = mentionElement.getAttribute("ID");
		String conf = mentionElement.getAttribute("p");
		
		if (conf.isEmpty())
			confidence = 1.0;
		else
			confidence = Double.parseDouble(conf);
		NodeList extents = mentionElement.getElementsByTagName("extent");
		Element extentElement = (Element) extents.item(0);
		extent = AceEntityMention.decodeCharseq(extentElement);
		
		/*NodeList ldc_scope = mentionElement.getElementsByTagName("ldc_scope");
		Element scope = (Element) ldc_scope.item(0);
		ldc_extent= AceEntityMention.decodeCharseq(scope);
		ldc_text = fileText.substring(ldc_extent.start(), ldc_extent.end()+1);*/
		
		jetExtent = AceEntityMention.aceSpanToJetSpan(extent, fileText);
		text = fileText.substring(extent.start(), extent.end()+1);
		// Span jetExtent = AceEntityMention.aceSpanToJetSpan(extent, fileText);
		NodeList anchors = mentionElement.getElementsByTagName("anchor");
		Element anchorElement = (Element) anchors.item(0);
		anchorExtent = AceEntityMention.decodeCharseq(anchorElement);
		anchorText = fileText.substring(this.anchorExtent.start(), this.anchorExtent.end()+1);
		anchorJetExtent = AceEntityMention.aceSpanToJetSpan(anchorExtent, fileText);
		NodeList arguments = mentionElement.getElementsByTagName("event_mention_argument");
		for (int j=0; j<arguments.getLength(); j++) {
			Element argumentElement = (Element) arguments.item(j);
			AceEventMentionArgument argument = new AceEventMentionArgument (argumentElement, acedoc);
			addArgument(argument);
		}
	}

	void addArgument (AceEventMentionArgument argument) {
		arguments.add(argument);
		argument.mention = this;
	}

	void setId (String id) {
		this.id = id;
	}

	/**
	 *  write the APF representation of the event mention to <CODE>w</CODE>.
	 * @throws IOException 
	 */
	 
	public void write (BufferedWriter w) throws IOException {
		w.append  ("    <event_mention ID=\"" + id + "\"");
		String str = String.format(" p=\"%5.3f\"", confidence);
		w.append(str);
	/*	if (Ace.writeEventConfidence)
			w.format(" p=\"%5.3f\"", confidence);*/
		w.append(">\n");
		w.append("      <extent>\n");
		AceEntityMention.writeCharseq (w, extent, text);
		w.append("      </extent>\n");
		w.append("      <anchor>\n");
		AceEntityMention.writeCharseq (w, anchorExtent, anchorText);
		w.append("      </anchor>\n");
		for (int i=0; i<arguments.size(); i++) {
			AceEventMentionArgument argument = (AceEventMentionArgument) arguments.get(i);
			argument.write(w);
		}
		w.append("    </event_mention>\n");
	}

	public boolean equals (Object o) {
		if (!(o instanceof AceEventMention))
			return false;
		AceEventMention p = (AceEventMention) o;
		if (!this.event.subtype.equals(p.event.subtype))
			return false;
		if (!this.anchorExtent.overlap(p.anchorExtent))
			return false;
		/*if (this.arguments.size()!=p.arguments.size())
			return false;
		for (int i=0;i<this.arguments.size();i++){
			if (!checkArg(this.arguments.get(i),p.arguments))
				return false;
		}*/
		return true;
	}
	
	public boolean checkArg(AceEventMentionArgument arg, ArrayList args){
		for (int i=0;i<args.size();i++){
			if (arg.equals(args.get(i)))
				return true;
		}
		return false;
	}
	
	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append(anchorText);
		// buf.append("[" + text + "]"); // display extent
		buf.append("(");
		for (int i=0; i<arguments.size(); i++) {
			if (i > 0) buf.append(", ");
			AceEventMentionArgument argument = (AceEventMentionArgument) arguments.get(i);
			buf.append(argument.toString());
		}
		buf.append(") ");
		return buf.toString();
	}

}
