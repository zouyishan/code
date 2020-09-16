package cuny.blender.englishie.nlp.wordnet.similarity ;

/**
 *
 * <p>Title: Java WordNet Similarity</p>
 * <p>Description: Assesses the semantic similarity between a pair of words
 * as described in Seco, N., Veale, T., Hayes, J. (2004) "An Intrinsic Information
 * Content Metric for Semantic Similarity in WordNet". In Proceedings of the
 * European Conference of Artificial Intelligence </p>
 * <p>When a lookup for word not contained in the dictionary is issued
 * an exception of this type is thrown.
 * <p>Copyright: Nuno Seco Copyright (c) 2004</p>
 * @author Nuno Seco
 * @version 1.0
 */

public class WordNotFoundException
    extends Exception
{

  /**
   * The constructor.
   * @param err String The error message that is to be shown
   * with the exception.
   */
  public WordNotFoundException ( String err )
  {
    super ( err ) ;
  }

}
