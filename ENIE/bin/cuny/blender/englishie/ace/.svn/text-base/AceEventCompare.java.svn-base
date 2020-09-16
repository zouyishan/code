package cuny.blender.englishie.ace;

import java.util.Comparator;

public class AceEventCompare implements Comparator {

	public int compare(Object o1, Object o2) {
		AceEvent m1 = (AceEvent) o1;
		AceEvent m2 = (AceEvent) o2;
		if (m1.mentions.get(0).anchorExtent.start() > m2.mentions.get(0).anchorExtent.start()) {
			return 1;
		} else {
			if (m1.mentions.get(0).anchorExtent.start() == m2.mentions.get(0).anchorExtent.start()) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}