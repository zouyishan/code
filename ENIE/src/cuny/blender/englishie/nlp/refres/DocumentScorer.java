
package cuny.blender.englishie.nlp.refres;

import cuny.blender.englishie.nlp.tipster.*;

/**
 *  abstract class for coreference scorers.
 */

public abstract class DocumentScorer {

	/**
	 *  compute a coreference score between two documents,
	 *  <CODE>responseDoc</CODE> and <CODE>keyDoc</CODE>.
	 */

	public abstract void score (Document responseDoc, Document keyDoc);

	/**
	 *  report to standard output the score for the most recently
	 *  processed document pair.
	 */

	public abstract void report ();

	/**
	 *  report the overall score for all documents processed so far.
	 */

	public abstract void summary ();

}
