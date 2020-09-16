
package cuny.blender.englishie.nlp.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cuny.blender.englishie.nlp.parser.ParseTreeNode;
import cuny.blender.englishie.nlp.tipster.Document;


public class Treebank {
	private Document document;

	private List<ParseTreeNode> parseTreeList;

	public Treebank(Document doc, List<ParseTreeNode> parseTreeList) {
		this.document = doc;
		this.parseTreeList = parseTreeList;
	}

	public Document getDocument() {
		return document;
	}

	public List<ParseTreeNode> getParseTreeList() {
		return parseTreeList;
	}

	public ParseTreeNode getParseTree(int i) {
		return parseTreeList.get(i);
	}
}
