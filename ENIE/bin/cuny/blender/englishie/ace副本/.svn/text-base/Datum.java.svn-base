
package cuny.blender.englishie.ace;

import java.util.*;

/**
 *  a data point, consisting of a set of features and an outcome, intended
 *  as part of the training set for a classifier.
 */

public class Datum {

	ArrayList features;

	String outcome;

	/**
	 *  create a new Datum.
	 */

	public Datum () {
		features = new ArrayList();
	}

	public String getFV(String feature){
		
		for (int i=0; i<features.size(); i++) {
			String str=(String)features.get(i);
			String left = str.substring(0,str.indexOf('='));
			String value = str.substring(str.indexOf('=')+1);
			if (left.equals(feature))
				return value;
		}
		String val="";
		return val;
	}
	/**
	 *  add feature <CODE>feature</CODE> to the Datum.
	 */

	public void addF (String feature) {
		features.add(feature);
	}

	/**
	 *  add feature <CODE>feature=value</CODE> to the Datum.
	 */

	public void addFV (String feature, String value) {
		if (value == null)
			features.add(feature);
		else
			features.add(feature + "=" + value);
	}

	/**
	 *  set the <CODE>outcome</CODE> for this set of features.
	 */

	public void setOutcome (String outcome) {
		this.outcome = outcome;
	}

	/**
	 *  return the Datum as a sequence of space-separated features, with the
	 *  outcome at the end.
	 */

	public String toString () {
		StringBuffer s = new StringBuffer();
		for (int i=0; i<features.size(); i++) {
			s.append((String)features.get(i));
			s.append(" ");
		}
		s.append(outcome);
		return s.toString();
	}

	/**
	 *  return the Datum as an array of features (with the outcome not included).
	 */

	public String[] toArray () {
		return (String[]) features.toArray(new String[features.size()]);
	}
}

