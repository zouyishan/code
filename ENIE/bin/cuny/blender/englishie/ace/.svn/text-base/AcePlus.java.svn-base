
// to fix:  write charseq ... XML
package cuny.blender.englishie.ace;

//Author:       Ralph Grishman
//Date:         May 18, 2008

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.tipster.*;

/**
 *  contains code to write supplementary syntactic information into the
 *  APF file.
 */

public class AcePlus {
	
	static int nodeNo;

	/**
	 *  write sentence, token, and parse tree information on Document
	 *  <CODE>doc</CODE> on PrintWriter <CODE>w</CODE>.
	 * @throws IOException 
	 */

	static void write (Document doc, BufferedWriter w) throws IOException {
		// iterate over sentences
		Vector<Annotation> sentences = doc.annotationsOfType ("sentence");
		if (sentences == null)
			return;
		int sentNo = 0;
		for (Annotation sentence : sentences) {
			sentNo++;
			Span sentenceSpan = new Span(sentence.span().start(), sentence.span().end());
			doc.shrink(sentence);
			Span shrunkSentenceSpan = sentence.span();
			// get ID
			String sentenceID = (String) sentence.get("ID");
			if (sentenceID == null) {
				sentenceID = "S." + sentNo;
				System.out.println ("Generating sentence ID " + sentenceID);
			}
			// get SOURCESENTID (for MT output)
			String sourceSentIdFeature = "";
			String sourceSentID = (String) sentence.get("SOURCESENTID");
			if (sourceSentID != null)
				sourceSentIdFeature = " SOURCESENTID=\"" + sourceSentID + "\"";
			// write sentence tag
			w.append ("  <sentence ID=\"" + sentenceID + "\"" + sourceSentIdFeature + ">\n");
			w.append ("    <charseq START=\"" + shrunkSentenceSpan.start() + "\"" +
			                       " END=\"" + (shrunkSentenceSpan.end()-1) + "\"></charseq>\n");
			// iterate over tokens:  write token tag
			Vector<Annotation> tokens = doc.annotationsOfType ("token", sentenceSpan);
			int tokenNo = 0;
			if (tokens != null) {
				for (Annotation token : tokens) {
					tokenNo++;
					doc.shrink(token);
					Span tokenSpan = token.span();
					Span aceTokenSpan = new Span(tokenSpan.start(), tokenSpan.end()-1);
					w.append ("    <token ID=\"" + sentenceID + "." + tokenNo + "\">\n");
					AceEntityMention.writeCharseq (w, aceTokenSpan, doc.text(tokenSpan));
					w.append ("    </token>\n");
				}
			}
			// if parse, get root, write parse node (root, 0)
			Annotation root = (Annotation) sentence.get("parse");
			if (root != null) {
				w.append ("    <parse>\n");
				nodeNo = 0;
				writeParseNode (doc, root, 0, sentNo, w);
				w.append ("    </parse>\n");
			}
			w.append ("  </sentence>\n");
		}
	}
	
	static void writeParseNode (Document doc, Annotation node, int level, int sentNo, BufferedWriter w) throws IOException {
		if (node == null) return;
		// write <node>
		nodeNo++;
		w.append ("      ");
		for (int i=0; i<level; i++) w.append("  ");
		String cat = (String) node.get("cat");
		String id = "N-" + sentNo + "-" + nodeNo;
		w.append ("<node cat=\"" + cat + "\" ID=\"" + id + "\">\n");
		// if node has children,
		Annotation[] children = (Annotation[]) node.get("children");
		if (children != null) {
		//   iterate over children, write parse node
			for (Annotation child : children)
				writeParseNode (doc, child, level+1, sentNo, w);
		} else {
		//   else write charseq
			for (int i=0; i<level; i++) w.append("  ");
			doc.shrink(node);
			Span aceNodeSpan = new Span(node.start(), node.end()-1);
			AceEntityMention.writeCharseq (w, aceNodeSpan, doc.text(node));
		}
		// write </node>
		w.append ("      ");
		for (int i=0; i<level; i++) w.append("  ");
		w.append ("</node>\n");		
	}

}		