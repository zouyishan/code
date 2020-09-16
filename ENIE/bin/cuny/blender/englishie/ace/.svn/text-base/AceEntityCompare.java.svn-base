package cuny.blender.englishie.ace;

import java.util.Comparator;

public class AceEntityCompare implements Comparator {

	public int compare(Object o1, Object o2) {
		AceEntityMention m1 = (AceEntityMention) o1;
		AceEntityMention m2 = (AceEntityMention) o2;
		if (m1.extent.start() > m2.extent.start()) {
			return 1;
		} else {
			if (m1.extent.start() == m2.extent.start()) {
				if (m1.extent.end()>m2.extent.end())
					return 1;
				else if (m1.extent.end()<m2.extent.end())
					return -1;
				else 
					return 0;
			} else {
				return -1;
			}
		}
	}
}