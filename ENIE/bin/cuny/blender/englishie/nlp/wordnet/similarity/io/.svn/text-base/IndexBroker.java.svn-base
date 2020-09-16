package cuny.blender.englishie.nlp.wordnet.similarity.io ;

import java.io.File;
import java.io.IOException ;

import org.apache.lucene.analysis.WhitespaceAnalyzer ;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException ;
import org.apache.lucene.queryParser.QueryParser ;
//import org.apache.lucene.search.Hits ;
import org.apache.lucene.search.IndexSearcher ;
import org.apache.lucene.search.Query ;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher ;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;




/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class IndexBroker
{
  public static final String POS_TAG = "pos" ;

  public static final String SYNSET = "synset" ;

  public static final String WORDS = "word" ;

  public static final String GLOSS_WORDS = "gloss" ;

  public static final String ANTONYM = "antonmym" ;

  public static final String IMMEDIATE_HYPERNYM = "ihypernym" ;

  public static final String HYPERNYM = "hypernym" ;

  public static final String HYPONYM = "hyponym" ;

  public static final String INFORMATION_CONTENT = "ic" ;

  public static final String MEMBER_HOLONYM = "member_holonym" ;

  public static final String SUBSTANCE_HOLONYM = "substance_holonym" ;

  public static final String PART_HOLONYM = "part_holonym" ;

  public static final String MEMBER_MERONYM = "member_meronym" ;

  public static final String SUBSTANCE_MERONYM = "substance_meronym" ;

  public static final String PART_MERONYM = "part_meronym" ;

  public static final String ATTRIBUTE = "attribute" ;

  public static final String DERIVATION = "derivation" ;

  public static final String DOMAIN_CATEGORY = "domain_category" ;

  public static final String MEMBER_CATEGORY = "member_category" ;

  public static final String DOMAIN_REGION = "domain_region" ;

  public static final String MEMBER_REGION = "member_region" ;

  public static final String DOMAIN_USAGE = "domain_usage" ;

  public static final String MEMBER_USAGE = "member_usage" ;

  public static final String ENTAILMENT = "entailment" ;

  public static final String CAUSE = "cause" ;

  public static final String ALSO_SEE = "also_see" ;

  public static final String VERB_GROUP = "verb_group" ;

  public static final String SIMILAR_TO = "similar_to" ;

  public static final String PARTICIPLE = "participle" ;

  private static final String PERTAINYM = "pertainym" ;

  private final String INDEX_DIR = "wn_index" ;

  private Searcher _searcher ;

  private QueryParser _parser ;

  private int MAX_RESULTS_PERQUERY = 30;
  /**
   * A static reference to an instance of an Index Broker.
   * This variable guarantees that only one instance of the broker
   * will be allowed for each Java Virtual Machine launched.
   */
  private static IndexBroker _instance ;



  public IndexBroker ()
  {
    try
    {
      IndexReader luceneReader;
      luceneReader = IndexReader.open(FSDirectory.open(new File(INDEX_DIR)), true);
		
      _searcher = new IndexSearcher ( /*System.getProperty("jetHome")+*/luceneReader ) ;
      //_parser = new QueryParser ( WORDS , new WhitespaceAnalyzer () ) ;
      _parser = new QueryParser(Version.LUCENE_CURRENT, WORDS, new WhitespaceAnalyzer ());
      //_parser.setOperator ( QueryParser.DEFAULT_OPERATOR_AND ) ;

    }
    catch ( IOException ex )
    {
      ex.printStackTrace () ;
    }
  }

 public  Searcher getSearcher (){
	 return _searcher;
 }

  /**
   * Static method that allows other objects to aquire
   * a reference to an existing broker. If no broker exists
   * than a new one is created.
   * @return IndexBroker
   */
  public static IndexBroker getInstance ()
  {
    if ( _instance == null )
    {
      _instance = new IndexBroker () ;
    }

    return _instance ;
  }
  
  public ScoreDoc[] getHits ( String query )
  {
    Query q ;
    try
    {
      q = _parser.parse ( query ) ;
      return _searcher.search ( q,MAX_RESULTS_PERQUERY ).scoreDocs ;
    }
    catch ( ParseException ex )
    {
      ex.printStackTrace () ;
    }
    catch ( IOException ex )
    {
      ex.printStackTrace () ;
    }
    return null ;
  }

  public boolean isSynset ( String string )
  {
    if ( string.indexOf ( "." ) != -1 )
    {
      return true ;
    }

    return false ;
  }

}

