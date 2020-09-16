//Author:       Zheng Chen
//Description:  Evalution metric called BCube for event coreference resolution

package cuny.blender.englishie.evaluation.event;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.ace.*;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.parser.SyntacticRelationSet;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;

/**
 * CorefEval evaluates an event tagger using simple metrics of events and
 * arguments found, missing, and spurious.
 */

public class BCubeCorefEval {

	// static final String home = "/jar/workspace/blender/corpus/ACE05/";
	// static final String home = "/jar/workspace/blender/corpus/ACE05/";
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	// tatic final String home ="/home/zheng/Application/Coref/ex2/ACE05/";
	// static final String home
	// ="/home/zheng/Application/Coref/graph/ex1/ACE05/";
	static final String ace = home + "source/";
	static final String fileListTest = home + "testfilelist";

	public StringBuffer log = new StringBuffer();

	public int N = 0;
	public double precision = 0;
	public double recall = 0;
	public double F = 0;
	public double W1 = 0;
	public double W2 = 0;
	public double M1 = 0;
	public double M2 = 0;

	public static void main(String[] args) throws IOException {
		// evalCoref(fileListTest);

	}

	/**
	 * evaluate the event tagger using the documents list in file
	 * <CODE>fileList</CODE>.
	 */
	public void evalCoref(String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;

		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.err.println("\nEvaluate file " + Integer.toString(docCount)
					+ ":" + currentDocPath);

			String textFile = ace + currentDocPath;
			String keyFile = ace
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			;
			String testFile = home + "output/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");

			String docId = currentDocPath.replaceFirst(".sgm", "");

			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);

			evalCoref(aceKeyDoc, aceTestDoc);

		}
		precision = precision / N;
		recall = recall / N;
		F = 2 * precision * recall / (precision + recall);
		System.out.println("Precision=" + Double.toString(precision));
		System.out.println("Recall=" + Double.toString(recall));
		System.out
				.println("F="
						+ Double.toString(2 * precision * recall
								/ (precision + recall)));
	}

	public void evalCoref(String home, String fileList) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;

		String currentDocPath;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.err.println("\nEvaluate file " + Integer.toString(docCount)
					+ ":" + currentDocPath);

			String textFile = home + "source/" + currentDocPath;
			String keyFile = home + "source/"
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			;
			String testFile = home + "output/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");

			String docId = currentDocPath.replaceFirst(".sgm", "");

			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);

			evalCoref(aceKeyDoc, aceTestDoc);
		}
		precision = precision / N;
		recall = recall / N;
		F = 2 * precision * recall / (precision + recall);
		System.out.println("Precision=" + Double.toString(precision));
		System.out.println("Recall=" + Double.toString(recall));
		System.out
				.println("F="
						+ Double.toString(2 * precision * recall
								/ (precision + recall)));
	}

	public void evalCoref(String sourceDir, String apfDir, String outDir,
			List<String> fileNames) throws IOException {
		for (int i = 0; i < fileNames.size(); i++) {
			String fileName = fileNames.get(i);
			// System.err.println ("\nEvaluate file " +
			// Integer.toString(docCount)+":"+currentDocPath);

			
			String textFile = sourceDir + fileName;
			String keyFile = apfDir + fileName.replaceFirst(".sgm", ".apf.xml");
			
			String testFile = outDir
					+ fileName.replaceFirst(".sgm", ".apf.xml");

			String docId = fileName.replaceFirst(".sgm", "");
			AceDocument aceKeyDoc = new AceDocument(textFile, keyFile);
			AceDocument aceTestDoc = new AceDocument(textFile, testFile);

			evalCoref(aceKeyDoc, aceTestDoc);

		}

		precision = precision / N;
		recall = recall / N;
		F = 2 * precision * recall / (precision + recall);
		// System.out.println("Precision="+Double.toString(precision));
		// System.out.println("Recall="+Double.toString(recall));
		// System.out.println("F="+Double.toString(2*precision*recall/(precision+recall)));
	}

	public void evalCoref(AceDocument aceKeyDoc, AceDocument aceTestDoc) {
		ArrayList testEvents = aceTestDoc.events;

		log.append("\nDocument:" + aceTestDoc.docID + "\n");
		
		// ArrayList wrongs= new ArrayList();
		// ArrayList miss= new ArrayList();
		for (int i = 0; i < testEvents.size(); i++) {
			AceEvent testEvent = (AceEvent) testEvents.get(i);
			/*
			 * if
			 * (!testEvent.genericity.equals("Specific")||!testEvent.polarity.
			 * equals("Positive")) continue;
			 */
			N += testEvent.mentions.size();
			log.append("\nCluster " + Integer.toString(i + 1) + "\n");
			for (int j = 0; j < testEvent.mentions.size(); j++) {
				ArrayList<AceEventMention> testMentions = new ArrayList(
						testEvent.mentions);
				AceEventMention testMention = (AceEventMention) testEvent.mentions
						.get(j);
				AceEvent e = findEvent(aceKeyDoc, testMention);
				if (e != null) {

					int w1 = 0;
					int w2 = 0;
					int miss1 = 0;
					int miss2 = 0;
					ArrayList<AceEventMention> m1 = new ArrayList(testMentions);
					m1.removeAll(e.mentions);

					for (int k = 0; k < m1.size(); k++) {
						AceEvent e1 = findEvent(aceKeyDoc, m1.get(k));
						if (e1 == null) {
							w1++;
						} else {
							w2++;
						}
					}
					// wrongs.addAll(m1);

					ArrayList<AceEventMention> m2 = new ArrayList(e.mentions);
					m2.removeAll(testMentions);

					for (int k = 0; k < m2.size(); k++) {
						AceEvent e1 = findEvent(aceTestDoc, m2.get(k));
						if (e1 == null) {
							miss1++;
						} else {
							miss2++;
						}

					}
					testMentions.retainAll(e.mentions);
					String str;
					if (m1.size() > 0 || m2.size() > 0) {
						str = testMention.text.replace("\n", " ");
						log.append("----------------\nmention "
								+ Integer.toString(j + 1) + "("
								+ testMention.event.type + "/"
								+ testMention.event.subtype + "):" + str + "\n");
						// log.append("----------"+"\n");

						int count = 0;
						for (AceEventMention mention : testMentions) {
							count++;
							str = mention.text.replace("\n", " ");
							log.append("Correct " + Integer.toString(count)
									+ ":" + mention.toString() + "/" + str
									+ "\n");
						}
					}

					if (m1.size() > 0) {
						int count = 0;
						for (AceEventMention mention : m1) {
							count++;
							str = mention.text.replace("\n", " ");
							log.append("Wrong " + Integer.toString(count) + ":"
									+ mention.toString() + "/" + str + "\n");
						}
					}

					// miss.addAll(m2);
					if (m2.size() > 0) {
						// log.append("--------------------------\n");
						int count = 0;
						for (AceEventMention mention : m2) {
							count++;
							str = mention.text.replace("\n", " ");
							log.append("Miss " + Integer.toString(count) + ":"
									+ mention.toString() + "/" + str + "\n");
						}
					}
					W1 += (double) w1 / testEvent.mentions.size();
					W2 += (double) w2 / testEvent.mentions.size();
					M1 += (double) miss1 / e.mentions.size();
					M2 += (double) miss2 / e.mentions.size();

					precision += (double) testMentions.size()
							/ testEvent.mentions.size();
					recall += (double) testMentions.size() / e.mentions.size();
				} else {
					W1 += 1;
					M1 += 1;
				}
			}
		}

	}

	public static Collection Union(Collection coll1, Collection coll2) {
		Set union = new HashSet(coll1);
		union.addAll(new HashSet(coll2));
		return new ArrayList(union);
	}

	public static ArrayList<AceEventMention> GetUniqueValues(Collection values) {
		return (ArrayList<AceEventMention>) Union(values, values);
	}

	public static AceEvent findEvent(AceDocument aceDoc,
			AceEventMention testMention) {
		ArrayList events = aceDoc.events;
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			if (!event.subtype.equals(testMention.event.subtype))
				continue;
			for (int j = 0; j < event.mentions.size(); j++) {
				if (testMention.equals(event.mentions.get(j)))
					return event;
			}
		}
		return null;
	}
}
