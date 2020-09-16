
package cuny.blender.englishie.nlp.time;

import java.util.List;

import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;


public class StringPattern extends PatternItem {
	private String str;

	public StringPattern(String str) {
		this.str = str;
	}

	public PatternMatchResult match(Document doc, List<Annotation> tokens, int offset) {
		Annotation token = tokens.get(offset);
		String tokenStr = doc.normalizedText(token);

		if (tokenStr.equals(str)) {
			return new PatternMatchResult(tokenStr, token.span());
		} else {
			return null;
		}
	}
}
