package cuny.blender.englishie.nlp.parser;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.apache.crimson.parser.Parser2;
import org.apache.log4j.Logger;

import cuny.blender.englishie.algorithm.graph.shortestpath.DijkstraAlgorithm;
import cuny.blender.englishie.algorithm.graph.shortestpath.Edge;
import cuny.blender.englishie.algorithm.graph.shortestpath.Graph;
import cuny.blender.englishie.algorithm.graph.shortestpath.Vertex;
import cuny.blender.englishie.nlp.util.Properties;

import edu.cmu.lti.javelin.util.DeltaRangeMap;
import edu.cmu.lti.javelin.util.RangeMap;
//import edu.stanford.nlp.ling.MapLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeGraphNode;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * Wrapper for the Stanford parser.
 * 
 * @author Justin Betteridge, Nico Schlaefer
 * @version 2007-10-30
 */
public class StanfordParser
{
    //protected static final Logger log = Logger.getLogger(StanfordParser.class);
    protected static final Pattern whitespace_pattern = Pattern.compile("\\s+");
    protected static final Pattern escaped_char_pattern = Pattern.compile("\\\\/");
    protected static final Pattern double_quote_lable_pattern = Pattern.compile("[`'][`']");
    protected static final Pattern bracket_label_pattern = Pattern.compile("-...-");

    public static final String BEGIN_KEY = "begin";
    public static final String END_KEY = "end";
    
    protected static class MutableInteger {
        public int value;
        public MutableInteger() { value = 0; }
        public MutableInteger(int i) { value = i; }
        public String toString() { return Integer.toString(value); }
        public int getValue() { return value; }
        public void setValue(int i) { value = i; }
    }

    protected static TreebankLanguagePack tlp = null;
    protected static LexicalizedParser parser = null;

    /**
     * Hide default ctor.
     */
    protected StanfordParser() {}

    /**
     * Initializes static resources.
     * 
     * @throws Exception
     */
    public static void initialize() throws Exception
    {
        if (parser != null) return;
        Properties properties = Properties.loadFromClassName(StanfordParser.class.getName());
        tlp = new PennTreebankLanguagePack();
        String modelFile = properties.getProperty("modelFile");
        if (modelFile == null)
            throw new Exception("Required property '" 
                + "modelFile' is undefined");
        parser = new LexicalizedParser(modelFile);
    }

    /**
     * Unloads static resources.
     * 
     * @throws Exception
     */
    public static void destroy() throws Exception
    {
        tlp = null;
        parser = null;
    }
    
    /**
     * Parses a sentence and returns a string representation of the parse tree.
     * 
     * @param sentence a sentence
     * @return Tree whose Label is a MapLabel containing correct begin and end
     * character offsets in keys BEGIN_KEY and END_KEY
     */
	@SuppressWarnings("unchecked")
    public static String parse(String sentence)
    {
		words = new ArrayList<Word>();
        if (tlp == null || parser == null)
            throw new RuntimeException("Parser has not been initialized");
        
        // parse the sentence to produce stanford Tree
      //  log.debug("Parsing sentence");
        Tree tree = null;
        synchronized (parser) {
            Tokenizer tokenizer = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
            words = tokenizer.tokenize();
            //System.out.println(words);
        //    log.debug("Tokenization: "+words);
            
            
            parser.parse(words);
            tree = parser.getBestParse();
           
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
            List<TypedDependency> tdl = gs.typedDependencies(true);
          
            for (int i=0;i<tdl.size();i++){
            	TypedDependency td = tdl.get(i);
            	System.out.println(td);
            }
        }
        //System.out.println(tree.toString().replaceAll(" \\[[\\S]+\\]",""));
        return tree.toString().replaceAll(" \\[[\\S]+\\]","");
    }
	
	
	
	public static List<TypedDependency> parse2(String sentence)
    {
		words = new ArrayList<Word>();
        if (tlp == null || parser == null)
            throw new RuntimeException("Parser has not been initialized");
        
        // parse the sentence to produce stanford Tree
        //log.debug("Parsing sentence");
        Tree tree = null;
        synchronized (parser) {
            Tokenizer tokenizer = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
            words = tokenizer.tokenize();
        //    System.out.println(sentence);
        //    log.debug("Tokenization: "+words);
            parser.parse(words);
            tree = parser.getBestParse();
            
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
            List<TypedDependency> tdl = gs.typedDependencies(true);
            /*for (int i=0;i<tdl.size();i++){
        		TypedDependency td = tdl.get(i);
        		System.out.println(td);
        	}*/
            return tdl;
            
           
            /*System.out.println("Path:");
            List<String> str = gs.getDependencyPath(18,3);*/
            /*TreeGraphNode node = gs.getNodeByIndex(3);
            System.out.println( gs.getGrammaticalRelation(3,9));
            TreeGraphNode gov = gs.getNodeByIndex(1);
            TreeGraphNode govH = gov.highestNodeWithSameHead();
            System.out.println(gov);
            System.out.println(govH);
            */
            //System.out.println(gs.getDependencyPath(1, 11));
          
            /*for (Dependency<Label, Label, Object> d : node.dependencies()) {
            	System.out.println(d);
            }*/
            //System.out.println(str);
        }
        
        
        // label tree with character extents
        //log.debug("Setting character extents");
        //updateTreeLabels(tree, tree, new MutableInteger(), new MutableInteger(-1));
        //log.debug("Creating offset mapping");
        //List<RangeMap> mapping = createMapping(sentence);
        //log.debug(mapping.toString());
        //log.debug("Applying offset mapping");
        //mapOffsets(tree, mapping);
        
        //return tree.toString().replaceAll(" \\[[\\S]+\\]","");
    }
	
	public static ArrayList<Integer> findPos (String query){
		ArrayList<Integer> list = new ArrayList<Integer>();
		Tokenizer tokenizer = tlp.getTokenizerFactory().getTokenizer(new StringReader(query));
        List<Word> qWords = tokenizer.tokenize();
        //Sentence q = new Sentence(qWords);
        //query = q.toString();
        query = Sentence.listToString(qWords);
		/*Sentence s = new Sentence(words);
		String sentence = s.toString();*/
   String sentence = Sentence.listToString(words);
		//System.out.println(sentence+"\n"+query);
		int pos = sentence.indexOf(query);
		if (pos<0)
			return list;
        String sub = sentence.substring(0,pos).trim();
        tokenizer = tlp.getTokenizerFactory().getTokenizer(new StringReader(sub));
        List<Word> subWords = tokenizer.tokenize();
       
        list.add(subWords.size()+1);
        list.add(subWords.size()+qWords.size());
        return list;
	}
	

	public static String getDepRelation1(){
		String s= Integer.toString(sourceNo);
		for (Edge e:edges){
			if (e.getDestination().getId().equals(s)&& e.getPath().contains("->")){
				return e.getPath();
			}	
		}
		return "";
	}
	
	public static String getDepRelation2(){
		String s= Integer.toString(destNo);
		for (Edge e:edges){
			if (e.getDestination().getId().equals(s)&& e.getPath().contains("->")){
				return e.getPath();
			}	
		}
		return "";
	}
	
	public static String getDepWord1(){
		String s= Integer.toString(sourceNo);
		for (Edge e:edges){
			if (e.getDestination().getId().equals(s)&& e.getPath().contains("->")){
				return e.getSource().getName();
			}	
		}
		return "";
	}
	
	public static String getDepWord2(){
		String s= Integer.toString(destNo);
		for (Edge e:edges){
			if (e.getDestination().getId().equals(s)&& e.getPath().contains("->")){
				return e.getSource().getName();
			}	
		}
		return "";
	}
	/*public static String getPath(String s,String query, String answer){
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		path = new LinkedList<Vertex> ();
		
		//Date start = new Date();
         
		List<TypedDependency> tdl = StanfordParser.parse2(s);
		Date end = new Date();
        System.out.println("parsing: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	
        start = new Date();
        
		for (int i=0;i<words.size();i++){
			Vertex v = new Vertex(Integer.toString(i+1), words.get(i).toString());
			nodes.add(v);
		}
		
		int edgeNo=0;
		for (int i=0;i<tdl.size();i++){
			TypedDependency td = tdl.get(i);
			String rel = td.reln().toString();
			int pos1 = td.gov().toString().lastIndexOf("-");
			String source = td.gov().toString().substring(pos1+1);
			pos1= td.dep().toString().lastIndexOf("-");
			String destination = td.dep().toString().substring(pos1+1);
			int sourceNo = Integer.parseInt(source);
			int destNo = Integer.parseInt(destination);
			edgeNo++;
			addEdge(Integer.toString(edgeNo),sourceNo,destNo,1,rel+"->");
			edgeNo++;
			addEdge(Integer.toString(edgeNo),destNo,sourceNo,1,rel+"<-");
		}
		
		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(nodes, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		
		
		int sourceNo = findPos(query).get(1);
		int destNo = findPos(answer).get(1);
		
		dijkstra.execute(nodes.get(sourceNo-1));
		path = dijkstra.getPath(nodes.get(destNo-1));
		
		
		end = new Date();
        System.out.println("path: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	
		String pathStr = "";
		for (int i=0;i<path.size()-1;i++) {
			pathStr+=findEdgePath(path.get(i).getId(),path.get(i+1).getId());
		}
		//System.out.println(pathStr);
		return pathStr;
		pathStr = path.get(0).getName();
		for (int i=0;i<path.size()-1;i++) {
			pathStr+="("+findEdgePath(path.get(i).getId(),path.get(i+1).getId())+")"+path.get(i+1).getName();
		}
		System.out.println(pathStr);
	}*/
	
	public static String getPath(String dep,String query, String answer){
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		path = new LinkedList<Vertex> ();
		
		
		//Date start = new Date();
         
		
		/*Date end = new Date();
        System.out.println("parsing: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	
        start = new Date();*/
        
		for (int i=0;i<words.size();i++){
			Vertex v = new Vertex(Integer.toString(i+1), words.get(i).toString());
			nodes.add(v);
		}
		
		int edgeNo=0;
		String [] arr = dep.split("),");
		
		for (int i=0;i<arr.length;i++){
			String s = arr[i];
			int index1 = s.indexOf('(');
			
			String rel = s.substring(0,index1);
			int index2 = s.indexOf(',');
			String sgov = s.substring(index1+1,index2);
			String sdep;
			if (i==arr.length-1)
				sdep= s.substring(index2+1,arr.length-1);
			else
				sdep= s.substring(index2+1);
					
			int pos1 = sgov.lastIndexOf("-");
			String source = sgov.substring(pos1+1);
			pos1= sdep.lastIndexOf("-");
			String destination = sdep.substring(pos1+1);
			int sourceNo = Integer.parseInt(source);
			int destNo = Integer.parseInt(destination);
			edgeNo++;
			addEdge(Integer.toString(edgeNo),sourceNo,destNo,1,rel+"->");
			edgeNo++;
			addEdge(Integer.toString(edgeNo),destNo,sourceNo,1,rel+"<-");
		}
		
		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(nodes, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		
		ArrayList<Integer> list = findPos(query);
		if (list.size()==0)
			return "";
		int sourceNo = list.get(1);
		
		list = findPos(answer);
		if (list.size()==0)
			return "";
		int destNo = list.get(1);
		if (sourceNo>nodes.size()||destNo>nodes.size())
			return "";
		
		dijkstra.execute(nodes.get(sourceNo-1));
		path = dijkstra.getPath(nodes.get(destNo-1));
		
		
		/*end = new Date();
        System.out.println("path: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	*/
		String pathStr = "";
		if (path==null)
			return pathStr;
		if (path.size()==0)
			return pathStr;
			
		for (int i=0;i<path.size()-1;i++) {
			pathStr+=findEdgePath(path.get(i).getId(),path.get(i+1).getId());
		}
		//System.out.println(pathStr);
		return pathStr;
		/*pathStr = path.get(0).getName();
		for (int i=0;i<path.size()-1;i++) {
			pathStr+="("+findEdgePath(path.get(i).getId(),path.get(i+1).getId())+")"+path.get(i+1).getName();
		}
		System.out.println(pathStr);*/
	}
	
	public static String getPath(String sentence,String dep,String query, String answer){
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		path = new LinkedList<Vertex> ();
		words = new ArrayList<Word>();
        
        Tokenizer tokenizer = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
        words = tokenizer.tokenize();
	
        
		for (int i=0;i<words.size();i++){
			Vertex v = new Vertex(Integer.toString(i+1), words.get(i).toString());
			nodes.add(v);
		}
		
		int edgeNo=0;
		String [] arr = dep.split("##");
		
		for (int i=0;i<arr.length;i++){
			String s = arr[i];
			s=s.replaceAll(",000", "000");
			int index1 = s.lastIndexOf('(');
			String rel = s.substring(0,index1);
			int index2 = s.indexOf(", ");
			String sgov = s.substring(index1+1,index2);
			int index3 = s.lastIndexOf(')');
			String sdep=s.substring(index2+2,index3);
			
			//System.out.println(sdep);
			int pos1 = sgov.lastIndexOf("-");
			String source = sgov.substring(pos1+1);
			pos1= sdep.lastIndexOf("-");
			String destination = sdep.substring(pos1+1);
			int sourceNo = Integer.parseInt(source);
			int destNo = Integer.parseInt(destination);
			edgeNo++;
			addEdge(Integer.toString(edgeNo),sourceNo,destNo,1,rel+"->");
			edgeNo++;
			addEdge(Integer.toString(edgeNo),destNo,sourceNo,1,rel+"<-");
		}
		
		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(nodes, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		
		ArrayList<Integer> list = findPos(query);
		if (list.size()==0)
			return "";
		sourceNo = list.get(1);
		
		list = findPos(answer);
		if (list.size()==0)
			return "";
		destNo = list.get(1);
		if (sourceNo>nodes.size()||destNo>nodes.size())
			return "";
		
		dijkstra.execute(nodes.get(sourceNo-1));
		path = dijkstra.getPath(nodes.get(destNo-1));
		
		
		/*end = new Date();
        System.out.println("path: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	*/
		String pathStr = "";
		if (path==null)
			return pathStr;
		if (path.size()==0)
			return pathStr;
			
		for (int i=0;i<path.size()-1;i++) {
			pathStr+=findEdgePath(path.get(i).getId(),path.get(i+1).getId());
		}
		//System.out.println(pathStr);
		return pathStr;
		/*pathStr = path.get(0).getName();
		for (int i=0;i<path.size()-1;i++) {
			pathStr+="("+findEdgePath(path.get(i).getId(),path.get(i+1).getId())+")"+path.get(i+1).getName();
		}
		System.out.println(pathStr);*/
	}
	
	
	public static String getPath(List<TypedDependency> tdl,String query, String answer){
		nodes = new ArrayList<Vertex>();
		edges = new ArrayList<Edge>();
		path = new LinkedList<Vertex> ();
		
		//Date start = new Date();
         
		
		/*Date end = new Date();
        System.out.println("parsing: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	
        start = new Date();*/
        
		for (int i=0;i<words.size();i++){
			Vertex v = new Vertex(Integer.toString(i+1), words.get(i).toString());
			nodes.add(v);
		}
		
		int edgeNo=0;
		for (int i=0;i<tdl.size();i++){
			TypedDependency td = tdl.get(i);
			String rel = td.reln().toString();
			int pos1 = td.gov().toString().lastIndexOf("-");
			String source = td.gov().toString().substring(pos1+1);
			pos1= td.dep().toString().lastIndexOf("-");
			String destination = td.dep().toString().substring(pos1+1);
			int sourceNo = Integer.parseInt(source);
			int destNo = Integer.parseInt(destination);
			edgeNo++;
			addEdge(Integer.toString(edgeNo),sourceNo,destNo,1,rel+"->");
			edgeNo++;
			addEdge(Integer.toString(edgeNo),destNo,sourceNo,1,rel+"<-");
		}
		
		// Lets check from location Loc_1 to Loc_10
		Graph graph = new Graph(nodes, edges);
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
		
		ArrayList<Integer> list = findPos(query);
		if (list.size()==0)
			return "";
		int sourceNo = list.get(1);
		
		list = findPos(answer);
		if (list.size()==0)
			return "";
		int destNo = list.get(1);
		if (sourceNo>nodes.size()||destNo>nodes.size())
			return "";
		
		dijkstra.execute(nodes.get(sourceNo-1));
		path = dijkstra.getPath(nodes.get(destNo-1));
		
		
		/*end = new Date();
        System.out.println("path: "+ (end.getTime() - start.getTime()) + " total milliseconds");
    	*/
		String pathStr = "";
		if (path==null)
			return pathStr;
		if (path.size()==0)
			return pathStr;
			
		for (int i=0;i<path.size()-1;i++) {
			pathStr+=findEdgePath(path.get(i).getId(),path.get(i+1).getId());
		}
		//System.out.println(pathStr);
		return pathStr;
		/*pathStr = path.get(0).getName();
		for (int i=0;i<path.size()-1;i++) {
			pathStr+="("+findEdgePath(path.get(i).getId(),path.get(i+1).getId())+")"+path.get(i+1).getName();
		}
		System.out.println(pathStr);*/
	}
	/**
	 * Parses a sentence and returns the PCFG score as a confidence measure.
	 * 
	 * @param sentence a sentence
	 * @return PCFG score
	 */
	@SuppressWarnings("unchecked")
	public static double getPCFGScore(String sentence) {
        if (tlp == null || parser == null)
            throw new RuntimeException("Parser has not been initialized");
        
        // parse the sentence to produce PCFG score
    //    log.debug("Parsing sentence");
        double score;
        synchronized (parser) {
            Tokenizer tokenizer = tlp.getTokenizerFactory().getTokenizer(new StringReader(sentence));
            List<Word> words = tokenizer.tokenize();
       //     log.debug("Tokenization: "+words);
            parser.parse(words);
            score = parser.getPCFGScore();
        }
        
        return score;
	}
    
    protected static void updateTreeLabels(Tree root, Tree tree, MutableInteger offset, MutableInteger leafIndex)
    {
        if (tree.isLeaf()) {
            leafIndex.value++;
            return;
        }
        String labelValue = tree.label().value().toUpperCase();
        int begin = root.leftCharEdge(tree);
        int end = root.rightCharEdge(tree);
        //System.out.println(labelValue+"("+begin+","+end+")");
        int length = end - begin;
        
        // apply offset to begin extent
        begin += offset.value;
        
        // calculate offset delta based on label
        if (double_quote_lable_pattern.matcher(labelValue).matches() && length > 1) {
            offset.value--;
       //     log.debug("Quotes label pattern fired: "+offset);
        } else if (bracket_label_pattern.matcher(labelValue).matches()) {
            offset.value -= 4;
       //     log.debug("Bracket label pattern fired: "+offset);
        } else if (tree.isPreTerminal()) {
            Tree leaf = tree.firstChild();
            String text = leaf.label().value();
            Matcher matcher = escaped_char_pattern.matcher(text);
            while (matcher.find()) {
                offset.value--;
            }
        }
        
        for (Tree child : tree.children())
            updateTreeLabels(root, child, offset, leafIndex);

        // apply offset to end extent
        end += offset.value;

        /*// set begin and end offsets on node
        MapLabel label = new MapLabel(tree.label());
        label.put(BEGIN_KEY, begin);
        label.put(END_KEY, end);
        label.put(MapLabel.INDEX_KEY, leafIndex.value);
        tree.setLabel(label);*/
    }

    /**
     * @param sentence
     * @return a list of RangeMap objects which define a mapping of character
     * offsets in a white-space depleted version of the input string back into
     * offsets in the input string.
     */
    protected static List<RangeMap> createMapping(String sentence)
    {
        List<RangeMap> mapping = new LinkedList<RangeMap>();
        Matcher whitespace_matcher = whitespace_pattern.matcher(sentence);
        DeltaRangeMap delta_rmap = null;

        // find all sequences of whitespace chars
        while (whitespace_matcher.find()) {
            int start = whitespace_matcher.start();
            int end = whitespace_matcher.end();
            int length = end - start;

            if (delta_rmap == null) {
                // create a new RangeMap object whose start begins at current
                // match start, and whose end is at the moment undefined. The
                // delta here is taken to be the length of the whitespace
                // sequence.
                delta_rmap = new DeltaRangeMap(start, 0, length);
            } else {
                // we've found the next sequence of whitespace chars, so we
                // finalize the end extent of the previous RangeMap, and make a
                // new RangeMap to describe the mapping from this point forward.
                delta_rmap.end = start - delta_rmap.delta;
                mapping.add(delta_rmap);
                delta_rmap = new DeltaRangeMap(delta_rmap.end, 0, delta_rmap.delta + length);
            }
        }

        // process trailing DeltaRangeMap if it exists
        if (delta_rmap != null) {
            delta_rmap.end = sentence.length() - delta_rmap.delta;
            mapping.add(delta_rmap);
        }

        return mapping;
    }

    /**
     * Maps Tree node offsets using provided mapping.
     * @param tree the Tree whose begin and end extents should be mapped.
     * @param mapping the list of RangeMap objects which defines the mapping.
     */
    /*protected static void mapOffsets(Tree tree, List<RangeMap> mapping)
    {
        // if mapping is empty, then assume 1-to-1 mapping.
        if (mapping == null || mapping.size() == 0) return;

        int begin_map_index = 0;
        RangeMap begin_rmap = mapping.get(begin_map_index);
        TREE: for (Tree t : tree) {
            if (t.isLeaf()) continue;
            MapLabel label = (MapLabel) t.label();
            int begin = (Integer) label.get(BEGIN_KEY);
            // "end" must be index of last char in range            
            int end = (Integer) label.get(END_KEY) - 1;

            // find the first rangemap whose end is greater than the
            // beginning of current annotation.
            // log.debug("Finding RangeMap whose extents include
            // annotation.begin");
            while (begin_rmap.end <= begin) {
                begin_map_index++;
                if (begin_map_index >= mapping.size()) break TREE;
                begin_rmap = mapping.get(begin_map_index);
            }

            // if beginning of current rangemap is greater than end of
            // current annotation, then skip this annotation (default
            // mapping is 1-to-1).
            if (begin_rmap.begin > end) {
                // log.debug("Skipping annotation (assuming 1-to-1 offset
                // mapping)");
                continue;
            }

            // if beginning of current annotation falls within current range
            // map, then map it back to source space.
            int new_begin = begin;
            if (begin_rmap.begin <= new_begin) {
                // log.debug("Applying RangeMap to begin offset");
                new_begin = begin_rmap.map(new_begin);
            }

            // find the first rangemap whose end is greater than the end of
            // current annotation.
            // log.debug("Finding RangeMap whose extents include
            // annotation.end");
            int end_map_index = begin_map_index;
            RangeMap end_rmap = begin_rmap;
            END_OFFSET: while (end_rmap.end <= end) {
                end_map_index++;
                if (end_map_index >= mapping.size()) break END_OFFSET;
                end_rmap = mapping.get(end_map_index);
            }

            // if end of current annotation falls within "end" range map,
            // then map it back to source space.
            int new_end = end;
            if (end_rmap.begin <= end) {
                // log.debug("Applying RangeMap to end offset");
                new_end = end_rmap.map(end);
            }

            label.put(BEGIN_KEY, new_begin);
            label.put(END_KEY, new_end + 1);
        }
    }*/
    
//  private static void printOffsets(String sentence, Tree tree)
//  {
//      if (tree.isLeaf()) return;
//      MapLabel label = (MapLabel) tree.label();
//      int begin = (Integer) label.get(BEGIN_KEY);
//      int end = (Integer) label.get(END_KEY);
//      int index = (Integer) label.index();
//      String str = null;
//      if (begin < 0 || begin > sentence.length() || end < begin || end > sentence.length()) {
//          str = "error";
//      } else {
//          str = sentence.substring(begin, end);
//      }
//      System.out.println(label.value()+"("+index+":"+begin+","+end+"): "+str);
//      for (Tree child : tree.children())
//          printOffsets(sentence, child);
//  }
    
    public static void main(String[] args) throws Exception
    {
        /*if (args.length != 1) {
            System.out.println("USAGE: StanfordParser <inputSentencesFile>");
            System.out.println("Output stored in: <inputSentencesFile>.parses");
            System.exit(0);
        }
        StanfordParser.initialize();
        List<String> sentences = new ArrayList<String> ();
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        BufferedWriter out = new BufferedWriter(new FileWriter(args[0]+".parses"));
        String sentence;
        while ((sentence = in.readLine()) != null) {
            sentences.add(sentence);
        }
        for (String s : sentences) {
            out.append(StanfordParser.parse(s)+"\n");
        }
        out.close();
        in.close();*/
    	
    	
    	StanfordParser.initialize();
    	String s= "It's now just one day until I hop in the car and head back in time to the loveliest village on the Plains...Auburn, Alabama.";
    	System.out.println(s);
    	String query = "Auburn";
    	String answer = "Alabama";
    	System.out.println(StanfordParser.parse(s));
    	//System.out.println(getDepWord1());
    	//System.out.println(getPath(s,query, answer));
    	//parse(s);
    }
    
    private static List<Vertex> nodes;
	private static List<Edge> edges;
	private static List<Word> words;
	private static LinkedList<Vertex> path;
	private static int sourceNo;
	private static int destNo;

	private static void addEdge(String edgeId, int sourceNo, int destNo,int weight, String path) {
		Edge e = new Edge(edgeId,nodes.get(sourceNo-1), nodes.get(destNo-1), weight, path);
		edges.add(e);
	}
	
	private static String findEdgePath(String sourceNo, String destNo){
		for (Edge e:edges){
			if (e.getSource().getId().equals(sourceNo) && e.getDestination().getId().equals(destNo))
				return e.getPath();
		}
		return "";
	}
}
