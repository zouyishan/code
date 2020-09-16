
package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.Lexicon;
import cuny.blender.englishie.nlp.lisp.FeatureSet;
import cuny.blender.englishie.nlp.refres.Resolve;

/**
 *  a set of names of locations (countries, cities, etc.).
 *  <P>
 *  Since names often consist of several tokens, all name parameters are of type
 *  String[], with one array element for each token of a name.
 */

public class Gazetteer {

	HashMap nationalityToCountry;
	HashMap nationalToCountry;
	HashMap nationalsToCountry;
	HashMap aliasToCountry;
	HashMap capitalToCountry;
	// a map from the location name to its type:  country, continent, region
	HashMap<String,String> locations;
	LineNumberReader reader;
	boolean monocase = false;
	// map from monocase name to mixed-case name
	HashMap monocaseMap;

	/**
	 *  create a new, empty Gazetteer.
	 */

	public Gazetteer () {
		nationalityToCountry = new HashMap();
		nationalToCountry = new HashMap();
		nationalsToCountry = new HashMap();
		aliasToCountry = new HashMap();
		capitalToCountry = new HashMap();
		locations = new HashMap<String, String>();
		monocaseMap = new HashMap();
	}

	/**
	 *  load the Gazetteer from the file specified by parameter <CODE>Gazetteer.fileName</CODE>
	 *  of the configuration file.
	 */

	public void load () throws IOException {
		String fileName = JetTest.getConfigFile("Gazetteer.fileName");
		if (fileName != null) {
			load(fileName);
		} else {
			System.err.println ("Gazetteer.load:  no file name specified in config file");
		}
	}

	/**
	 *  load the Gazetteer from file <CODE>fileName</CODE>.
	 */

	public void load (String fileName) throws IOException {
		System.err.println ("Loading gazetteer.");
		reader = new LineNumberReader (new FileReader (fileName));
		StreamTokenizer tok = new StreamTokenizer(reader);
		while (tok.nextToken() != StreamTokenizer.TT_EOF) {
			readGazetteerEntry (tok);
		}
	}

	private void readGazetteerEntry (StreamTokenizer tok) throws IOException {
		String key;
		String value;
		String primaryName = "";
		String type = "";
		String nationality = "";
		String national = "";
		String nationals = "";
		do {
			if (tok.ttype == StreamTokenizer.TT_WORD) {
				key = tok.sval.intern();
			} else {
				int ln = reader.getLineNumber();
				System.err.println ("*** Syntax error in gazetteer: " + tok);
				return;
			}
			if (tok.nextToken() == '"') {
				value = tok.sval;
				monocaseMap.put(value.toLowerCase(), value);
			} else {
				int ln = reader.getLineNumber();
				System.err.println ("*** Syntax error in gazetteer, line" + ln);
				return;
			}
			if (key == "country" || key == "continent" || key == "region" ||
			    key == "usstate" || key == "city") {
				primaryName = value;
				type = key;
				locations.put(value, key);
			} else if (key == "nationality") {
				nationality = value;
				nationalityToCountry.put(nationality, primaryName);
			} else if (key == "aka") {
				aliasToCountry.put(value, primaryName);
				locations.put(value, type);
			} else if (key == "national") {
				national = value;
			} else if (key == "nationals") {
				nationals = value;
			} else if (key == "capital") {
				capitalToCountry.put(value, primaryName);
				locations.put(value, "city");
			} else {
				int ln = reader.getLineNumber();
				System.err.println ("*** Syntax error in gazetteer, line" + ln);
			}
		}	while (tok.nextToken() != ';');
		if (type == "country" && nationality != "") {
			if (national == "") national = nationality;
			if (nationals == "") {
				nationals = national + "s";
				monocaseMap.put(nationals.toLowerCase(), nationals);
			}
			nationalToCountry.put(national, primaryName);
			nationalsToCountry.put(nationals, primaryName);
		}
		// enter location names in lexicon with annotation type 'onoma'
		for (String location : locations.keySet()) {
			Lexicon.addEntry (splitAtWS(location),
			                  new FeatureSet ("type", locations.get(location)),
			                  "onoma");
		}
	}

	/**
	 *  sets the <CODE>monocase</CODE> which, when true, ignores case for
	 *  Gazetteer predicates.
	 */

	public void setMonocase (boolean monocase) {
		this.monocase = monocase;
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is a nationality
	 *  adjective.
	 */

	public boolean isNationality (String[] s) {
		return nationalityToCountry.containsKey(foldArg(s));
	}

	/**
	 *  if <CODE>s</CODE> is a nationality adjective, returns the
	 *  associated country name, else <CODE>null</CODE>.
	 */

	public String[] nationalityToCountry (String[] s) {
		return (String[]) splitAtWS((String) nationalityToCountry.get(foldArg(s)));
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is the name of a national.
	 */

	public boolean isNational (String[] s) {
		return nationalToCountry.containsKey(foldArg(s));
	}

	/**
	 *  if <CODE>s</CODE> is the name of a national, returns the
	 *  associated country name, else <CODE>null</CODE>.
	 */

	public String[] nationalToCountry (String[] s) {
		return (String[]) splitAtWS((String) nationalToCountry.get(foldArg(s)));
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is the name of a set of
	 *  nationals.
	 */

	public boolean isNationals (String[] s) {
		return nationalsToCountry.containsKey(foldArg(s));
	}

	/**
	 *  if <CODE>s</CODE> is the name of a set of nationals, returns the
	 *  associated country name, else <CODE>null</CODE>.
	 */

	public String[] nationalsToCountry (String[] s) {
		return (String[]) splitAtWS((String) nationalsToCountry.get(foldArg(s)));
	}

	/**
	 *  if <CODE>s</CODE> is the name of the capital of a country, returns the
	 *  associated country name, else <CODE>null</CODE>.
	 */

	public String[] capitalToCountry (String[] s) {
		return (String[]) splitAtWS((String) capitalToCountry.get(foldArg(s)));
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is the name of any type of location
	 *  (either the primary name or an alias).
	 */

	public boolean isLocation (String[] s) {
		return locations.containsKey(foldArg(s));
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is the name of a country
	 *  (either the primary name or an alias).
	 */

	public boolean isCountry (String[] s) {
		return locations.get(foldArg(s)) == "country";
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is an alias of a
	 *  location name.
	 */

	public boolean isCountryAlias (String[] s) {
		return aliasToCountry.containsKey(foldArg(s));
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is the name of a U.S.
	 *  state (either the primary name or an alias).
	 */

	public boolean isState (String[] s) {
		return locations.get(foldArg(s)) == "usstate";
	}

	/**
	 *  returns <CODE>true</CODE> if <CODE>s</CODE> is the name of a region
	 *  or continent (either the primary name or an alias).
	 */

	public boolean isRegionOrContinent (String[] s) {
		String type = (String) locations.get(foldArg(s));
		return type == "region" || type == "continent";
	}

	/**
	 *  if <CODE>s</CODE> is the alias of a location name, returns the
	 *  primary location name, else <CODE>null</CODE>.
	 */

	public String[] canonicalCountryName (String[] s) {
		return (String[]) splitAtWS((String) aliasToCountry.get(foldArg(s)));
	}

	public static void main (String[] args) throws IOException {
		Gazetteer g = new Gazetteer();
		g.load ("data/loc.dict");
		g.setMonocase(true);
		System.out.println (g.isNationals(new String[]{"palestinians"}));
	}

	public static String[] splitAtWS (String s) {
		if (s == null) return null;
		StringTokenizer st = new StringTokenizer(s);
		int length = st.countTokens();
		String[] splitS = new String[length];
		for (int i=0; i<length; i++)
			splitS[i] = st.nextToken();
		return splitS;
	}

	private String foldArg (String[] s) {
		String x = Resolve.concat(s);
		if (monocase && monocaseMap.containsKey(x))
			x = (String) monocaseMap.get(x);
		return x;
	}
}
