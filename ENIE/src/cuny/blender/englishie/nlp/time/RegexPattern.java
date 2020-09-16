
package cuny.blender.englishie.nlp.time;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;


public class RegexPattern extends PatternItem {
	Pattern pattern;

	public RegexPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public PatternMatchResult match(Document doc, List<Annotation> tokens, int offset) {
		String token = doc.normalizedText(tokens.get(offset));
		Matcher m = pattern.matcher(token);

		if (m.matches()) {
			return new PatternMatchResult(m, tokens.get(offset).span());
		}
		else {
			return null;
		}
	}
}
