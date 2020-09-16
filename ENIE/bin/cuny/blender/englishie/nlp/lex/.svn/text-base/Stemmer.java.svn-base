package cuny.blender.englishie.nlp.lex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;
import cuny.blender.englishie.nlp.tipster.Span;
import cuny.blender.englishie.nlp.util.IOUtils;


/**
 * Stemmer provides method for getting stem of word.
 * 
 * Stemmer uses stem dictionary which written in plain text. Each line of stem
 * dictionary will be as following
 * 
 * <pre>
 *  do	did does doing done
 * </pre>
 * 
 * Each word is separated by whitepsace characters. First word is stem and other
 * words are inflected word.
 * 
 * @author Akira ODA
 */
public class Stemmer {
	private static final String DICT_ENCODING = "US-ASCII";

	private static Stemmer defaultStemmer = loadDefaultStemmer();

	private HashMap<String, String> dict = new HashMap<String, String>();

	public Stemmer() {
	}

	/**
	 * Returns default stemmer.
	 * 
	 * @return
	 */
	public static Stemmer getDefaultStemmer() {
		return defaultStemmer;
	}

	/**
	 * Loads default stem dictionary.
	 * 
	 * @return
	 */
	private static Stemmer loadDefaultStemmer() {
		InputStream in = null;
		try {
			in = Stemmer.class.getClassLoader().getResourceAsStream(
					"cuny/blender/englishie/nlp/lex/resources/stem.dict");
			Reader reader = new InputStreamReader(in, DICT_ENCODING);
			Stemmer stemmer = new Stemmer();
			stemmer.loadDictionary(reader);

			return stemmer;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Loads stem dictonary.
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void loadDictionary(File file) throws IOException {
		BufferedReader in = IOUtils.getBufferedReader(file, DICT_ENCODING);
		try {
			loadDictionary(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	public void loadDictionary(Reader reader) throws IOException {
		BufferedReader in = null;
		if (reader instanceof BufferedReader) {
			in = (BufferedReader) reader;
		} else {
			in = new BufferedReader(reader);
		}

		String line;
		Pattern delimiter = Pattern.compile("\\s+");
		while ((line = in.readLine()) != null) {
			String[] splitted = delimiter.split(line);
			String stem = splitted[0].intern();
			for (int i = 1; i < splitted.length; i++) {
				dict.put(splitted[i].intern(), stem);
			}
		}
	}

	/**
	 * Added stem feature to each token annotation if token text and stem are
	 * difference.
	 * 
	 * @param doc
	 * @param span
	 */
	public void tagStem(Document doc, Span span) {
		Vector<Annotation> tokens = doc.annotationsOfType("token", span);
		Vector<String> posTags = getPosTags(doc, tokens);

		assert tokens.size() == posTags.size();

		for (int i = 0; i < tokens.size(); i++) {
			Annotation token = tokens.get(i);
			String word = doc.text(token).trim();
			String pos = posTags.get(i);
			String stem = getStem(word, pos);
			if (stem != word) {
				token.put("stem", stem);
			}
		}
	}

	public String getStem(String word) {
		String lower = word.toLowerCase();
		
		String stem;
		if ((stem = dict.get(lower)) != null) {
			// known word
			return stem;
		}
		return word;
	}
	
	/**
	 * Returns stem of <code>word</code>
	 * 
	 * @param word
	 * @param pos
	 *            part of speech of <code>word</code>
	 * @return stem of <code>word</code>.
	 */
	public String getStem1(String word, String pos) {
		if (pos.equals("I") || pos.equals("NNP") || pos.equals("NNPS")) {
			return word;
		}

		String lower = word.toLowerCase();
		boolean allLower = lower.equals(word);
		String stem;
		if ((stem = dict.get(lower)) != null) {
			// known word
			return stem;
		}

		if (any(lower, "NNS", "VBZ")) {
			return getStemInternal(word, lower, "s", allLower);
		}

		if (any(lower, "VBD", "VBN")) {
			return getStemInternal(word, lower, "ed", allLower);
		}

		if (lower.equals("VBG")) {
			return getStemInternal(word, lower, "ing", allLower);
		}

		if (!allLower) {
			return lower;
		}

		return word;
	}

	public String getStem(String word, String pos) {
		if (pos.equals("I") || pos.equals("NNP") || pos.equals("NNPS")) {
			return word;
		}

		String lower = word.toLowerCase();
		boolean allLower = lower.equals(word);
		String stem;
		if ((stem = dict.get(lower)) != null) {
			// known word
			return stem;
		}

		if (any(pos, "n", "tv")) {
			return getStemInternal(word, lower, "s", allLower);
		}

		if (any(pos, "tv", "ven")) {
			return getStemInternal(word, lower, "ed", allLower);
		}

		if (pos.equals("ving")) {
			return getStemInternal(word, lower, "ing", allLower);
		}

		if (!allLower) {
			return lower;
		}

		return word;
	}
	
	private String getStemInternal(String word, String lowerWord, String suffix, boolean allLower) {
		if (lowerWord.endsWith(suffix)) {
			return lowerWord.substring(0, lowerWord.length() - suffix.length());
		} else if (!allLower) {
			return lowerWord;
		} else {
			return word;
		}
	}

	private Vector<String> getPosTags(Document doc, Vector<Annotation> tokens) {
		Vector<String> result = new Vector<String>();

		for (Annotation token : tokens) {
			Vector<Annotation> constitList = doc.annotationsOfType("constit", token.span());
			result.add(getPosTag(constitList));
		}

		return result;
	}

	private String getPosTag(Vector<Annotation> constitList) {
		if (constitList == null || constitList.size() == 0) {
			return null;
		} else if (constitList.size() == 1) {
			return ((String) constitList.get(0).get("cat")).toUpperCase();
		} else {
			for (Annotation constit : constitList) {
				Annotation[] children = (Annotation[]) constit.get("children");
				if (children != null) {
					continue;
				}
				String cat = (String) constit.get("cat");
				if (cat != null) {
					return cat.toUpperCase();
				}
			}
			return null;
		}
	}

	private static boolean any(String pos, String... candidates) {
		for (String candidate : candidates) {
			if (pos.equals(candidate)) {
				return true;
			}
		}
		return false;
	}
}
