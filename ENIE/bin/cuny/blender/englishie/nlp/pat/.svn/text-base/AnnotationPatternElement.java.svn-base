
//Title:        JET
//Version:      1.00
//Copyright:    Copyright (c) 2000
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package cuny.blender.englishie.nlp.pat;

import java.util.*;

import cuny.blender.englishie.nlp.lisp.*;
import cuny.blender.englishie.nlp.tipster.*;

/**
 *  A pattern element which matches an annotation.
 */

public class AnnotationPatternElement extends AtomicPatternElement{
  String type;
  FeatureSet fs;
  Variable v;

  /**
   * creates an AnnotationPatternElement which matches an Annotation with type
   * <I>type</I> and features <I>fs</I>.
   */

  public AnnotationPatternElement(String type, FeatureSet fs) {
    this.type = type;
    this.fs = fs;
  }

  /**
   * creates an AnnotationPatternElement which matches an Annotation with type
   * <I>type</I> and features <I>fs</I> and, if the match is successful,
   * binds variable <I>v</I> to the annotation.
   */

  public AnnotationPatternElement(String type, FeatureSet fs, Variable v) {
    this.type = type;
    this.fs = fs;
    this.v = v;
  }

  public void eval (Document doc, int posn, String tokenString, HashMap bindings,
                    PatternApplication patap, PatternNode node) {
    Vector posnAnn = doc.annotationsAt (posn);
    if (posnAnn != null) {
      for (Iterator it = posnAnn.iterator (); it.hasNext ();) {
        Annotation ann = (Annotation) it.next ();
        if (ann.type ().equals (type) && ann.get("hidden")== null) {
          HashMap newbindings = Pat.matchFS (ann.attributes (), fs, bindings);
          if (newbindings != null) {
            int ic = ann.span ().end ();
            if (v != null) {
              newbindings = (HashMap) newbindings.clone();
              newbindings.put(v.name,ann);
            }
            node.eval (doc, ic, newbindings, patap);
          }
        }
      }
    }
  }

  public String toString () {
    return "[" +  type + fs.toSGMLString() + "]";
  }
}
