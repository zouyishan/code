package cuny.blender.englishie.nlp.ne;

import java.util.regex.Pattern;

import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;


public class RegexpRule {
	private MatchType matchType;

	private Pattern pattern;

	public RegexpRule(MatchType type, String pattern) {
		this.matchType = type;
		this.pattern = Pattern.compile(pattern);
	}

	public boolean accept(Document doc, Annotation[] tokens, int pos) {
		if (this.matchType == MatchType.ANY) {
			return true;
		}

		String token = doc.normalizedText(tokens[pos]);
		boolean matched = pattern.matcher(token).matches();
		boolean ret;

		switch (matchType) {
		case NORMAL:
			ret = matched;
			break;

		case NOT:
			ret = !matched;
			break;

		default:
			throw new InternalError();
		}

		return ret;
	}
}
