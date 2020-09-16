
/**
 *
 */
package cuny.blender.englishie.nlp.ne;

import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;

public interface MatchRule {
	public boolean accept(Document doc, Annotation[] tokens, int n);
}
