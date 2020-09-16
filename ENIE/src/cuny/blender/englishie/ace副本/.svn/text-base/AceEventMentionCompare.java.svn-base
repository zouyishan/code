package cuny.blender.englishie.ace;

import java.util.Comparator;

public class AceEventMentionCompare implements Comparator {

	public int compare(Object o1, Object o2) {
		AceEventMention m1 = (AceEventMention) o1;
		AceEventMention m2 = (AceEventMention) o2;
		if (m1.anchorExtent.start() > m2.anchorExtent.start()) {
			return 1;
		} else {
			if (m1.anchorExtent.start() == m2.anchorExtent.start()) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}