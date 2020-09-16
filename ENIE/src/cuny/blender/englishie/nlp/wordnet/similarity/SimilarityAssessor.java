package cuny.blender.englishie.nlp.wordnet.similarity ;

import java.io.IOException ;
import java.util.LinkedList ;

import org.apache.lucene.document.Document ;
//import org.apache.lucene.search.Hits ;
import org.apache.lucene.search.ScoreDoc;

import cuny.blender.englishie.nlp.wordnet.similarity.io.IndexBroker;





/**
 *
 * <p>Title: Java WordNet Similarity</p>
 * <p>Description: Assesses the semantic similarity between a pair of words
 * as described in Seco, N., Veale, T., Hayes, J. (2004) "An Intrinsic Information
 * Content Metric for Semantic Similarity in WordNet". In Proceedings of the
 * European Conference of Artificial Intelligence </p>
 * <p>This is the class that is responsible for the similarity calculations.
 * Please note that Documents in the context of this class
 * correspond to synsets. Each Document structure holds the synset offset
 * the list of words in the synset and a list containing all hypernym offsets.
 * For the sake of computational simplicity, in calculating the best MSCA, the list
 * of hypernyms also contains the  synset of the current document.  </p>
 * <p>Copyright: Nuno Seco Copyright (c) 2004</p>
 * @author Nuno Seco
 * @version 1.0
 */

public class SimilarityAssessor
{
  /**
   * Holds a reference to an instance of an Index Broker.
   */
  private IndexBroker _broker ;
  
  
  
  
  /**
   * The constructor. Obtains an instance of an Index Broker.
   */
  public SimilarityAssessor ()
  {
    _broker = IndexBroker.getInstance () ;
  }
  
  
  
  
  /**
   * Calculates the similarity between two specific senses.
   * @param word1 String
   * @param senseForWord1 int The sense number for the first word
   * @param word2 String
   * @param senseForWord2 int The sense number for the second word
   * @throws WordNotFoundException An exception is thrown if one of the words
   * is not contained in the WordNet dictionary.
   * @return double The degree of similarity between the words;
   * 0 means no similarity and 1 means that they may belong to the same synset.
   */
  public double getSenseSimilarity ( String word1 , int senseForWord1 , String word2 , int senseForWord2 )
  throws WordNotFoundException
  {
	  ScoreDoc[] synsets1 = _broker.getHits ( word1 + "." + senseForWord1 ) ;
	  ScoreDoc[] synsets2 = _broker.getHits ( word2 + "." + senseForWord2 ) ;
    
    if ( synsets1.length == 0 )
    {
      //throw new WordNotFoundException ( "Word " + word1 + "." + senseForWord1 + " is not in the dictionary." ) ;
    	return -1;
    }
    
    if ( synsets2.length == 0 )
    {
      //throw new WordNotFoundException ( "Word " + word2 + "." + senseForWord2 + " is not in the dictionary." ) ;
    	return -1;
    }
    
    try
    {
      //return getSimilarity ( synsets1.doc ( 0 ) , synsets2.doc ( 0 ) ) ;
    	Document d1 =_broker.getSearcher().doc(synsets1[0].doc);
    	Document d2 =_broker.getSearcher().doc(synsets2[0].doc);
    	return getSimilarity ( d1.get(IndexBroker.WORDS) , d2.get(IndexBroker.WORDS)) ;
    }
    catch ( IOException ex )
    {
      ex.printStackTrace () ;
      return 0.0 ;
    }
    
  }
  
  
  
  
  /**
   * Calculates the similarity between the two words, given as parameters,
   * according to the referenced paper.
   * @param word1 String
   * @param word2 String
   * @throws WordNotFoundException  An exception is thrown if one of the words
   * is not contained in the WordNet dictionary.
   * @return double The degree of similarity between the words;
   * 0 means no similarity and 1 means that they may belong to the same synset.
   */
  public double getSimilarity ( String word1 , String word2 )
  
  {
	  ScoreDoc[] synsets1 = _broker.getHits ( word1 + ".*" ) ;
	  ScoreDoc[] synsets2 = _broker.getHits ( word2 + ".*" ) ;
    
    if ( synsets1.length == 0 )
    {
      //throw new WordNotFoundException ( "Word " + word1 + " is not in the dictionary." ) ;
    	return -1;
    }
    
    if ( synsets2.length == 0 )
    {
      //throw new WordNotFoundException ( "Word " + word2 + " is not in the dictionary." ) ;
    	return -1;
    }
    
    double current = 0 ;
    double best = 0 ;
    
    try
    {
      for ( int i = 0 ; i < synsets1.length ; i++ )
      {
        for ( int j = 0 ; j < synsets2.length ; j++ )
        {
        	Document d1 =_broker.getSearcher().doc(synsets1[i].doc);
        	Document d2 =_broker.getSearcher().doc(synsets2[j].doc);
          current = getSimilarity ( d1 , d2 ) ;
          
          if ( current > best )
          {
            best = current ;
          }
        }
      }
    }
    catch ( IOException ex )
    {
      ex.printStackTrace () ;
    }
    
    return best ;
  }
  
  
  
  
  /**
   * Does the actual calculation between synsets.
   * @param synset1 Document
   * @param synset2 Document
   * @return double
   */
  private double getSimilarity ( Document synset1 , Document synset2 )
  {
    double msca = getBestMSCAValue ( synset1 , synset2 ) ;
    
    if ( msca == -1 )
    {
      return 0 ;
    }
    
    return 1 -
        ( ( Double.parseDouble ( synset1.get ( IndexBroker.INFORMATION_CONTENT ) ) +
        Double.parseDouble ( synset2.get ( IndexBroker.INFORMATION_CONTENT ) ) -
        2 * msca ) / 2 ) ;
  }
  
  
  
  
  /**
   * Discovers the best Most Specific Common Abstraction (MSCA) value for the two
   * given Synsets. Note that synsets are represented as Lucene documents.
   * @param doc1 Document One synset
   * @param doc2 Document Another synset
   * @return double The value of the MSCA with the highest IC value
   */
  private double getBestMSCAValue ( Document doc1 , Document doc2 )
  {
    double current = 0 ;
    double best = 0 ;
    String offset ;
    
    LinkedList intersection = getIntersection ( doc1.getValues ( IndexBroker.HYPERNYM )[ 0 ].split ( " " ) ,
        doc2.getValues ( IndexBroker.HYPERNYM )[ 0 ].split ( " " ) ) ;
    
    if ( intersection.isEmpty () )
    {
      return -1 ;
    }
    
    while ( !intersection.isEmpty () )
    {
      offset = intersection.removeFirst ().toString () ;
      
      current = getIC ( offset ) ;
      if ( current > best )
      {
        best = current ;
      }
    }
    
    return best ;
  }
  
  
  
  
  /**
   * Obtains the Information Content (IC) value for a given synset offset.
   * @param offset String  The offset to be queried
   * @return double  The IC value
   */
  private double getIC ( String offset )
  {
	  ScoreDoc[] synsets = _broker.getHits (IndexBroker.SYNSET + ":" + offset ) ;
		 
   // Hits synset = _broker.getHits ( IndexBroker.SYNSET + ":" + offset ) ;
    try
    {
    	Document d1 =_broker.getSearcher().doc(synsets[0].doc);
        
      return Double.parseDouble (d1.get ( IndexBroker.INFORMATION_CONTENT ) ) ;
    }
    catch ( Exception ex )
    {
      ex.printStackTrace () ;
      return 0.0 ;
    }
  }
  
  
  
  
  /**
   * Gets a list of strings that are contained in both arrays. The strings in the arrays
   * represent different synsets.
   * @param values1 String[] An array of synsets
   * @param values2 String[] Another array of synsets.
   * @return LinkedList The list of synsets common to each array
   */
  private LinkedList getIntersection ( String[] values1 , String[] values2 )
  {
    LinkedList intersection = new LinkedList () ;
    
    for ( int i = 0 ; i < values1.length ; i++ )
    {
      for ( int j = 0 ; j < values2.length ; j++ )
      {
        if ( values1[ i ].equals ( values2[ j ] ) )
        {
          intersection.add ( values1[ i ] ) ;
          break ;
        }
      }
    }
    
    return intersection ;
  }
  
}
