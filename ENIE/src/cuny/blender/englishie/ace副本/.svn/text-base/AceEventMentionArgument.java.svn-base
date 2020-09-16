
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

public class AceEventMentionArgument {

	/**
	 *  the role of the argument in the event
	 */
	public String role;
	/**
	 *  the value of the argument:  an AceEntityMention or AceTimexMention
	 */
	public AceMention value;
	/**
	 *  our confidence in the presence of this argument
	 */
	public double confidence = 1.0;
	/**
	 *  our confidence in this role assignment for this argument
	 */
	public double roleConfidence = 1.0;

	public AceEventMentionArgument (AceMention value, String role) {
		this.value = value;
		this.role = role;
	}

	AceEventMention mention;
	/**
	 *  create an AceEventMentionArgument from the information in the APF file.
	 *  @param argumentElement the XML element from the APF file containing
	 *                       information about this argument
	 *  @param acedoc  the AceDocument of which this AceEvent is a part
	 */

	public AceEventMentionArgument (Element argumentElement, AceDocument acedoc) {
			role = argumentElement.getAttribute("ROLE");
			String mentionid = argumentElement.getAttribute("REFID");
			value = acedoc.findMention(mentionid);
			String conf = argumentElement.getAttribute("p");
			
			if (conf.isEmpty())
				confidence = 1.0;
			else
				confidence = Double.parseDouble(conf);
			conf = argumentElement.getAttribute("pRole");
			if (conf.isEmpty())
				roleConfidence = 1.0;
			else
				roleConfidence = Double.parseDouble(conf);
	}

	/**
	 *  write the APF representation of the event mention argument to <CODE>w</CODE>.
	 * @throws IOException 
	 */
	 
	public void write (BufferedWriter w) throws IOException {
		w.append  ("      <event_mention_argument REFID=\"" + value.id + "\" ROLE=\"" + role + "\"");
		
		//if (Ace.writeEventConfidence) {
			w.append(String.format(" p=\"%5.3f\"", confidence));
			w.append(String.format(" pRole=\"%5.3f\"", roleConfidence));
			w.append(">\n");
		//}
			w.append("      	<extent>\n");
			AceEntityMention.writeCharseq (w, value.extent, value.text);
			w.append("      	</extent>\n");
			w.append("      </event_mention_argument>\n");
	}

	public String toString () {
		return role + ":" + ((value == null) ? "?" : value.getHeadText());
	}

	public boolean equals (Object o) {
		if (!(o instanceof AceEventMentionArgument))
			return false;
		AceEventMentionArgument p = (AceEventMentionArgument) o;
		if (!this.mention.event.subtype.equals(p.mention.event.subtype))
			return false;
		Span sp1 = this.value.extent;
		AceEventArgumentValue arg = p.value.getParent();
		boolean bOffset = false;
		if (arg instanceof AceEntity){
			for (int i=0;i<((AceEntity)arg).mentions.size();i++){
				AceEntityMention m = (AceEntityMention)((AceEntity)arg).mentions.get(i);
				if (sp1.equals(m.extent)){
					bOffset = true;
					break;
				}
			}
		}
		if (arg instanceof AceTimex){
			for (int i=0;i<((AceTimex)arg).mentions.size();i++){
				AceTimexMention m = (AceTimexMention)((AceTimex)arg).mentions.get(i);
				if (sp1.equals(m.extent)){
					bOffset = true;
					break;
				}
			}
		}
		if (arg instanceof AceValue){
			for (int i=0;i<((AceValue)arg).mentions.size();i++){
				AceValueMention m = (AceValueMention)((AceValue)arg).mentions.get(i);
				if (sp1.equals(m.extent)){
					bOffset = true;
					break;
				}
			}
		}
		if (!this.role.equals(p.role))
			return false;
		
		return bOffset;
	}
}
