
//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkil
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

  /**
   *  holds information on the frequency with which an EventPattern
   *  triggered correct or incorrect events.
   */

public class PatternEvaluation {

	int successCount = 0;

	int failureCount = 0;

	// map   role name -->  success count when match includes that role name
	HashMap successWithArg = new HashMap();

	// map   role name -->  failure count when match includes that role name
	HashMap failureWithArg = new HashMap();

	public PatternEvaluation () {
	}

	public void recordSuccess (ArrayList arguments) {
		recordSuccess (arguments, 1);
	}

	public void recordSuccess (ArrayList arguments, int count) {
		successCount += count;
		for (int iarg=0; iarg<arguments.size(); iarg++) {
			AceEventMentionArgument arg = (AceEventMentionArgument) arguments.get(iarg);
			String role = arg.role;
			increment(successWithArg, role, count);
		}
	}

	public void recordFailure (ArrayList arguments) {
		recordFailure (arguments, 1);
	}

	public void recordFailure (ArrayList arguments, int count) {
		failureCount += count;
		for (int iarg=0; iarg<arguments.size(); iarg++) {
			AceEventMentionArgument arg = (AceEventMentionArgument) arguments.get(iarg);
			String role = arg.role;
			increment(failureWithArg, role, count);
		}
	}

	private void increment (HashMap h, String key, int incr) {
		int count = (h.get(key) == null) ? 0 : ((Integer)h.get(key)).intValue();
		h.put(key, new Integer(count+incr));
	}

	public String toString () {
		StringBuffer buf = new StringBuffer();
		buf.append (successCount + "+/" + failureCount + "-(");
		TreeSet roles = new TreeSet(successWithArg.keySet());
		Set failureRoles = failureWithArg.keySet();
		roles.addAll(failureRoles);
		Iterator it = roles.iterator();
		while (it.hasNext()) {
			String role = (String) it.next();
			int s = (successWithArg.get(role) == null) ?
			        0 : ((Integer)successWithArg.get(role)).intValue();
			int f = (failureWithArg.get(role) == null) ?
			        0 : ((Integer)failureWithArg.get(role)).intValue();
			buf.append (" " + role + ":" + s + "+/" + f + "-");
		}
		buf.append(")");
		return buf.toString();
	}

	public int test (ArrayList arguments) {
		if (1 * successCount >= failureCount)
			// return successCount;
			return (50 * successCount) / (successCount + failureCount + 10);
		for (int iarg=0; iarg<arguments.size(); iarg++) {
			AceEventArgument arg = (AceEventArgument) arguments.get(iarg);
			String role = arg.role;
			int s = (successWithArg.get(role) == null) ?
			        0 : ((Integer)successWithArg.get(role)).intValue();
			int f = (failureWithArg.get(role) == null) ?
			        0 : ((Integer)failureWithArg.get(role)).intValue();
			if (1 * s >= f)
				// return s;
				return (50 * s) / (s + f + 10);
		}
		return -1;
	}

	public void write (PrintWriter pw) {
		pw.println ("noArg | " + successCount + " | " + failureCount);
		TreeSet roles = new TreeSet(successWithArg.keySet());
		Set failureRoles = failureWithArg.keySet();
		roles.addAll(failureRoles);
		Iterator it = roles.iterator();
		while (it.hasNext()) {
			String role = (String) it.next();
			int s = (successWithArg.get(role) == null) ?
			        0 : ((Integer)successWithArg.get(role)).intValue();
			int f = (failureWithArg.get(role) == null) ?
			        0 : ((Integer)failureWithArg.get(role)).intValue();
			pw.println(role + " | " + s + " | " + f);
		}
		pw.println("$evalEnd");
	}

	public PatternEvaluation (BufferedReader reader) throws IOException {
		String line = reader.readLine();
		while (line != null && !line.equals("$evalEnd")) {
			String[] field = line.split(" \\| ");
			if (field.length != 3) {
				System.err.println ("PatternEvaluation:  invalid input " + line);
				return;
			}
			String role = field[0];
			try {
				int s = Integer.parseInt(field[1]);
				int f = Integer.parseInt(field[2]);
				if (role.equals("noArg")) {
					successCount = s;
					failureCount = f;
				} else {
					successWithArg.put(role, new Integer(s));
					failureWithArg.put(role, new Integer(f));
				}
			} catch (NumberFormatException e) {
				System.err.println ("PatternEvaluation:  invalid input " + line);
				return;
			}
			line = reader.readLine();
		}
	}


}
