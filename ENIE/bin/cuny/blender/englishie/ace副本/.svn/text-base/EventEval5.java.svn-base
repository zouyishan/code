
//evaluation of event extraction with event coreference information
package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.parser.SyntacticRelationSet;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;


/**
 * EventEval evaluates an event tagger using simple metrics of events and
 * arguments found, missing, and spurious.
 */

public class EventEval5 {

	int correctArgs, roleErrorArgs, missingArgs, spuriousArgs, weirdArgs;
	int correctEvents, typeErrorEvents, missingEvents, spuriousEvents;

	double TIP,TIR,TIF;
	double TCP,TCR,TCF;
	double AIP,AIR,AIF;
	double ACP,ACR,ACF;
	int MENTION_COUNT=0;
	int EVENT_COUNT = 0;
	StringBuffer log = new StringBuffer();
	static String encoding = "UTF-8";
	//static final String home = "/jar/workspace/boost/corpus/ACE05/";
	static final String home = "C:/Users/blender/workspace/blender/corpus/ACE05/";
	//static String home = "C:/Users/blender/GALE/ACE05/";
	/*static final String home =
	    "C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";*/
	//static final String home = "/home/zheng/Application/EngET/ct1/";
	//static final String home = "C:/Users/blender/Workspace/GALE/workspace/ENEventTagger/ACE05/";
	//static final String home = "/home/zheng/Application/CT2/ACE05/";
	//static final String home = "C:/Users/blender/GALE/ACECorpus/";
	static String keyDir = home+"rel/";
	static String testDir = home+"output/";
	static String devFileList = home + "devfilelist";
	static String logFileName = home + "relate_events";
	static String testFileList = home + "testfilelist";
	private static AceEvent baseEvent;

	static final String fileListTest = home + "testfilelist";
	
	public static void main(String[] args) throws IOException {
		EventEval5 ee1 = new EventEval5();
		ee1.evaluate(fileListTest);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(logFileName), "UTF-8"));
		bw.flush();
		bw.write(ee1.log.toString());
		bw.close();
	}

	/**
	 * evaluate the event tagger using the documents list in file
	 * <CODE>fileList</CODE>.
	 */

	public void evaluate(String fileList) throws IOException {
		

		BufferedReader reader = new BufferedReader(new FileReader(fileList));
		int docCount = 0;
		String currentDocPath;
		int emCount = 0;
		while ((currentDocPath = reader.readLine()) != null) {
			docCount++;
			System.out.println("Evaluate file " + docCount + ":"
					+ currentDocPath);
			String textFile = home + "sources/" + currentDocPath.replace(".sgm", ".sgm");
			String xmlFile = home + "sources/"
					+ currentDocPath.replaceFirst(".sgm", ".apf.xml");
			String testFile = home+"output/output3/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			
			String reltextFile = home + "rel/" + currentDocPath.replace(".sgm", ".rel");
			String relFile = home + "output/output2/"
					+ currentDocPath.replaceFirst(".sgm", ".apf");
			
			/*ExternalDocument doc = new ExternalDocument("sgml", textFile);
			doc.setAllTags(true);
			doc.open();
			doc.stretchAll();*/
			
			AceDocument testDoc = new AceDocument(textFile, testFile);
			ArrayList testEvents = testDoc.events;
			
			AceDocument trueDoc = new AceDocument(textFile, xmlFile);
			ArrayList trueEvents = trueDoc.events;
			
			AceDocument relDoc = new AceDocument(reltextFile, relFile);
			ArrayList relEvents = relDoc.events;
			
			for (int i = 0;i<testEvents.size();i++){
				AceEvent e = (AceEvent)testEvents.get(i); 
				for (int j=0;j<e.mentions.size();j++){
					emCount++;
					AceEventMention em = e.mentions.get(j);
					boolean b=(findLooseEventMention(em,trueEvents)!=null);
					
					if (b==true)
						log.append("Event Mention"+emCount+" (Correct)\n");
					else
						log.append("Event Mention"+emCount+" (Spurious)\n");
					log.append("Context:"+em.text+"\n");
					int num = findCorefEventMentions(em,relEvents);
					System.out.println(b+"\t"+num);
					log.append("Corefered event mentions in related docs:"+num+"\n");
					
				}
			}
		}
		reader.close();
	}

	private int findCorefEventMentions(AceEventMention em,
			ArrayList events) {
		for (int i = 0; i < events.size(); i++) {
			baseEvent = (AceEvent) events.get(i);
			ArrayList mentions = baseEvent.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention mention = (AceEventMention) mentions.get(j);
				String str1= em.text.replace("\n", " ");
				if (mention.text.equals(str1)){
					return baseEvent.mentions.size()-1;
				}
			}
		}
		return -1;
	}
	private AceEventMention findLooseEventMention(AceEventMention em,
			ArrayList events) {
		for (int i = 0; i < events.size(); i++) {
			baseEvent = (AceEvent) events.get(i);
			if (!em.event.subtype.equals(baseEvent.subtype))
				continue;
			ArrayList mentions = baseEvent.mentions;
			for (int j = 0; j < mentions.size(); j++) {
				AceEventMention mention = (AceEventMention) mentions.get(j);
				Span keyAnchorExtent = mention.anchorExtent;
				/* loose match */
				if (em.anchorExtent.overlap(keyAnchorExtent)
						|| keyAnchorExtent.overlap(em.anchorExtent))
					return mention;
			}
		}
		return null;
	}
}

