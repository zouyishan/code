//Title:        JET
//Copyright:    2003, 2004, 2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkil
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cuny.blender.englishie.nlp.JetTest;

import javax.xml.parsers.*;

/**
 * an Ace Document, including entities, time expressions, relations, and values,
 * either obtained from an APF file or generated by the system.
 */

public class AceDocument {

	/**
	 * true for 2004 or 2005 APF format
	 */

	public static boolean ace2004 = true;

	/**
	 * true for 2005 APF format
	 */

	public static boolean ace2005 = true;

	private static DocumentBuilder builder = null;
	private String fileText;
	private StringBuffer fileTextWithXML;

	/**
	 * the name of the source file
	 */

	public String sourceFile;

	/**
	 * the type of source: newswire or bnews
	 */

	public String sourceType;

	/**
	 * the document ID
	 */

	public String docID;
	/**
	 * a list of the entities in the document
	 */
	public ArrayList<AceEntity> entities = new ArrayList<AceEntity>();
	/**
	 * a list of the time expressions in the document
	 */
	public ArrayList<AceTimex> timeExpressions = new ArrayList<AceTimex>();
	/**
	 * a list of the value expressions in the document
	 */
	public ArrayList<AceValue> values = new ArrayList<AceValue>();
	/**
	 * a list of the relations in the document
	 */
	public ArrayList<AceRelation> relations = new ArrayList<AceRelation>();
	/**
	 * a list of the events in the document
	 */
	public ArrayList<AceEvent> events = new ArrayList<AceEvent>();

	public static String encoding = "UTF-8";// "ISO-8859-1"; // default:
											// ISO-LATIN-1

	public AceDocument(String sourceFile, String sourceType, String docID,
			String docText) {
		this.sourceFile = sourceFile;
		this.sourceType = sourceType;
		this.docID = docID;
		fileText = docText;
	}

	/**
	 * create a new AceDocument from the source document in 'textFileName' and
	 * the APF file 'APFfileName'
	 */

	public AceDocument(String textFileName, String APFfileName) {
		try {
			// initialize APF reader
			if (builder == null) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setValidating(false);
				builder = factory.newDocumentBuilder();
				
				
				
				builder.setEntityResolver(new EntityResolver() {
			        public InputSource resolveEntity(String publicId, String systemId)
			                throws SAXException, IOException {
			            if (systemId.contains("apf.v5.1.1.dtd")) {
			                return new InputSource(new StringReader(""));
			            } else {
			                return null;
			            }
			        }
			    });
			}
			analyzeDocument(textFileName, APFfileName);
		} catch (SAXException e) {
			System.err
					.println("AceDocument:  Exception in initializing APF reader: "
							+ e);
		} catch (IOException e) {
			System.err
					.println("AceDocument:  Exception in initializing APF reader: "
							+ e);
		} catch (ParserConfigurationException e) {
			System.err
					.println("AceDocument:  Exception in initializing APF reader: "
							+ e);
		}
	}

	private void analyzeDocument(String textFileName, String APFfileName)
			throws SAXException, IOException {
		Document apfDoc = builder.parse(APFfileName);
		fileTextWithXML = readDocument(textFileName);
		fileText = eraseXML(fileTextWithXML);
		readAPFdocument(apfDoc, fileText);
	}

	/**
	 * read APF document and create entities and relations
	 */

	void readAPFdocument(Document apfDoc, String fileText) {
		NodeList sourceFileElements = apfDoc
				.getElementsByTagName("source_file");
		Element sourceFileElement = (Element) sourceFileElements.item(0);
		sourceFile = sourceFileElement.getAttribute("URI");
		sourceType = sourceFileElement.getAttribute("SOURCE");

		NodeList documentElements = apfDoc.getElementsByTagName("document");
		Element documentElement = (Element) documentElements.item(0);
		docID = documentElement.getAttribute("DOCID");

		if (Ace.perfectMentions & !Ace.perfectEntities) {
			readPerfectMentions(apfDoc, fileText);
			return;
		}

		NodeList entityElements = apfDoc.getElementsByTagName("entity");
		for (int i = 0; i < entityElements.getLength(); i++) {
			Element entityElement = (Element) entityElements.item(i);
			AceEntity entity = new AceEntity(entityElement, fileText);
			addEntity(entity);
		}
		NodeList valueElements = apfDoc.getElementsByTagName("value");
		for (int i = 0; i < valueElements.getLength(); i++) {
			Element valueElement = (Element) valueElements.item(i);
			AceValue value = new AceValue(valueElement, fileText);
			addValue(value);
		}
		NodeList timexElements = apfDoc.getElementsByTagName("timex2");
		for (int i = 0; i < timexElements.getLength(); i++) {
			Element timexElement = (Element) timexElements.item(i);
			AceTimex timex = new AceTimex(timexElement, fileText);
			addTimeExpression(timex);
		}
		NodeList relationElements = apfDoc.getElementsByTagName("relation");
		for (int i = 0; i < relationElements.getLength(); i++) {
			Element relationElement = (Element) relationElements.item(i);
			AceRelation relation = new AceRelation(relationElement, this,
					fileText);
			addRelation(relation);
		}
		NodeList eventElements = apfDoc.getElementsByTagName("event");
		for (int i = 0; i < eventElements.getLength(); i++) {
			Element eventElement = (Element) eventElements.item(i);
			AceEvent event = new AceEvent(eventElement, this, fileText);
			addEvent(event);
		}
	}

	public void addEntity(AceEntity entity) {
		entities.add(entity);
		allMentionsSet.addAll(entity.mentions);
		allMentionsList = new ArrayList<AceMention>(allMentionsSet);
	}

	public void addValue(AceValue value) {
		values.add(value);
		allMentionsSet.addAll(value.mentions);
		allMentionsList = new ArrayList<AceMention>(allMentionsSet);
	}

	public void addTimeExpression(AceTimex timex) {
		timeExpressions.add(timex);
		allMentionsSet.addAll(timex.mentions);
		allMentionsList = new ArrayList<AceMention>(allMentionsSet);
	}

	public void addRelation(AceRelation relation) {
		relations.add(relation);
	}

	public void addEvent(AceEvent event) {
		events.add(event);
	}

	/* assumes elementType is a leaf element type */

	static String getElementText(Element e, String elementType) {
		NodeList typeList = e.getElementsByTagName(elementType);
		Element typeElement = (Element) typeList.item(0);
		String text = (String) typeElement.getFirstChild().getNodeValue();
		return text;
	}

	void readPerfectMentions(Document apfDoc, String fileText) {
		NodeList mentionElements = apfDoc
				.getElementsByTagName("entity_mention");
		for (int i = 0; i < mentionElements.getLength(); i++) {
			Element mentionElement = (Element) mentionElements.item(i);
			String entityId = "E" + mentionElement.getAttribute("ID");
			String type = mentionElement.getAttribute("ENTITY_TYPE");
			if (AceEntity.standardType.containsKey(type))
				type = (String) AceEntity.standardType.get(type);
			String subtype = mentionElement.getAttribute("ENTITY_SUBTYPE");
			// adjust for missing subtypes in training data
			if ((!type.equals("PERSON")) && (!type.equals(""))
					&& subtype.equals(""))
				subtype = "Other";
			AceEntityMention mention = new AceEntityMention(mentionElement,
					fileText);
			AceEntity entity = new AceEntity(entityId, type, subtype, false);
			entity.addMention(mention);
			addEntity(entity);
		}
	}

	/**
	 * read file 'fileName' and return its contents as a StringBuffer
	 */

	static StringBuffer readDocument(String fileName) throws IOException {
		File file = new File(fileName);
		String line;
		BufferedReader reader = new BufferedReader(
		// (new FileReader(file));
				new InputStreamReader(new FileInputStream(file), encoding));
		StringBuffer fileText = new StringBuffer();
		while ((line = reader.readLine()) != null)
			fileText.append(line + "\n");
		reader.close();
		
		int pos = fileText.indexOf("<TEXT>");
		if (pos<0){
			String str = fileText.toString();
			fileText = new StringBuffer();
			fileText.append( "<TEXT>\n"+str+"</TEXT>");
		}
		return fileText;
	}

	/**
	 * compute ACEoffsetMap, a map from ACE offsets (which exclude XML tags to
	 * Jet offsets (which include all characters in the file)
	 */

	static String eraseXML(StringBuffer fileTextWithXML) {
		boolean inTag = false;
		int length = fileTextWithXML.length();
		StringBuffer fileText = new StringBuffer();
		for (int i = 0; i < length; i++) {
			char c = fileTextWithXML.charAt(i);
			if (c == '<'){
				String str = fileTextWithXML.substring(i+1,i+5);
				if (str.contains("http")){
					inTag = false;
				}
				else{
					inTag = true;
				}
			}
			if (!inTag)
				fileText.append(c);
			if (c == '>')
				inTag = false;
		}
		return fileText.toString();
	}

	public cuny.blender.englishie.nlp.tipster.Document JetDocument() {
		cuny.blender.englishie.nlp.tipster.Document doc = new cuny.blender.englishie.nlp.tipster.Document(
				fileTextWithXML.toString());
		doc.annotateWithTag("TEXT");
		return doc;
	}

	/**
	 * returns the AceEntity with ID 'id', or null if no such AceEntity.
	 */

	AceEntity findEntity(String id) {
		for (int i = 0; i < entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			if (entity.id.equals(id)) {
				return entity;
			}
		}
		System.err.println("*** unable to find entity with id " + id);
		return null;
	}

	AceEventArgumentValue findEntityValueTimex(String id) {
		for (int i = 0; i < values.size(); i++) {
			AceValue value = (AceValue) values.get(i);
			if (value.id.equals(id)) {
				return value;
			}
		}
		for (int i = 0; i < timeExpressions.size(); i++) {
			AceTimex timex = (AceTimex) timeExpressions.get(i);
			if (timex.id.equals(id)) {
				return timex;
			}
		}
		return findEntity(id);
	}

	/**
	 * returns the AceEntityMention with ID 'id', or null if no such AceEntity.
	 */

	AceEntityMention findEntityMention(String id) {
		for (int i = 0; i < entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			AceEntityMention mention = entity.findMention(id);
			if (mention != null) {
				return mention;
			}
		}
		System.err.println("*** unable to find entity mention with id " + id);
		return null;
	}

	/**
	 * returns the AceEntityMention, AceValueMention, or AceTimexMention with ID
	 * 'id', or null if no such object exists.
	 */

	AceMention findMention(String id) {
		for (int i = 0; i < values.size(); i++) {
			AceValue value = (AceValue) values.get(i);
			AceValueMention mention = value.findMention(id);
			if (mention != null) {
				return mention;
			}
		}
		for (int i = 0; i < timeExpressions.size(); i++) {
			AceTimex timex = (AceTimex) timeExpressions.get(i);
			AceTimexMention mention = timex.findMention(id);
			if (mention != null) {
				return mention;
			}
		}
		return findEntityMention(id);
	}

	TreeSet<AceMention> allMentionsSet = new TreeSet<AceMention>();
	ArrayList<AceMention> allMentionsList = new ArrayList<AceMention>();

	/**
	 * returns an ArrayList of all entity, value, and timex mentions in the
	 * document, ordered according to the position of their heads (the order
	 * defined for EntityMentions). Note: this assumes that entities, values,
	 * and time expressions are added only using methods addEntity, addValue,
	 * and addTimeExpression.
	 */

	ArrayList<AceMention> getAllMentions() {
		return allMentionsList;
	}

	/*
	 * private void updateAllMentions () { allMentionsSet = new TreeSet(); for
	 * (int i=0; i<entities.size(); i++) { AceEntity entity = (AceEntity)
	 * entities.get(i); allMentionsSet.addAll(entity.mentions); } for (int i=0;
	 * i<values.size(); i++) { AceValue value = (AceValue) values.get(i);
	 * allMentionsSet.addAll(value.mentions); } for (int i=0;
	 * i<timeExpressions.size(); i++) { AceTimex timex = (AceTimex)
	 * timeExpressions.get(i); allMentionsSet.addAll(timex.mentions); }
	 * allMentionsList = new ArrayList(allMentionsSet); }
	 */

	/**
	 * writes the AceDocument to 'w' in APF format.
	 * 
	 * @throws IOException
	 */

	public void write(BufferedWriter w,
			cuny.blender.englishie.nlp.tipster.Document doc) throws IOException {
		w.append("<?xml version=\"1.0\"?>\n");
		w.append("<!DOCTYPE source_file SYSTEM \"apf.v5.1.1.dtd\">\n");
		w.append("<source_file URI=\"" + sourceFile + "\"");
		w.append(" SOURCE=\"" + sourceType + "\" TYPE=\"text\">\n");
		w.append("<document DOCID=\"" + docID + "\">\n");
		String extendedAPF = JetTest.getConfig("Ace.extendedAPF");
		if (extendedAPF != null)
			AcePlus.write(doc, w);
		for (int i = 0; i < entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			entity.write(w);
		}
		for (int i = 0; i < values.size(); i++) {
			AceValue value = (AceValue) values.get(i);
			value.write(w);
		}
		for (int i = 0; i < timeExpressions.size(); i++) {
			AceTimex timex = (AceTimex) timeExpressions.get(i);
			timex.write(w);
		}
		for (int i = 0; i < relations.size(); i++) {
			AceRelation relation = (AceRelation) relations.get(i);
			relation.write(w);
		}
		for (int i = 0; i < events.size(); i++) {
			AceEvent event = (AceEvent) events.get(i);
			event.write(w);
		}
		w.append("</document>\n");
		w.append("</source_file>\n");
		w.close();
	}

	public static void main(String[] args) throws IOException {
		String home = "C:/Users/blender/Desktop/";
		// test ACE 04 file
		/*
		 * String ace = home + "ace/"; String xmlFile = ace +
		 * "training04/English/nwire/APW20001001.2021.0521.apf.xml"; String
		 * textFile = ace +
		 * "training04/English/nwire/APW20001001.2021.0521.sgm"; ace2005 =
		 * false;
		 */
		// test old-style (ACE 03 and before) file
		/*
		 * xmlFile = ace + "training/nwire/APW19980213.1302.sgm.tmx.rdc.xml";
		 * textFile = ace + "training/nwire/APW19980213.1302.sgm"; ace2004 =
		 * false;
		 */
		// test ACE 05 file
		String ace = home;
		String xmlFile = ace + "1.asr.apf.xml";
		String textFile = ace + "1.asr.txt";

		AceDocument ad = new AceDocument(textFile, xmlFile);
		ad.write(new BufferedWriter(new OutputStreamWriter(System.out)), null);
	}
}
