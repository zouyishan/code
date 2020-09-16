
//Title:        JET
//Version:      1.00
//Copyright:    Copyright (c) 2000
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package cuny.blender.englishie.nlp.pat;

import java.util.*;

import cuny.blender.englishie.nlp.tipster.*;

/**
 *  an arc in the graph representation of a pattern.
 */

public class PatternArc {

  public AtomicPatternElement pe;
  public PatternNode target;
  private boolean visited;

  public PatternArc(AtomicPatternElement e, PatternNode t) {
    pe = e;
    target = t;
    visited = false;
  }

  public void visit() {
    visited = true;
  }

  public boolean visited() {
    return visited;
  }

  public void eval (Document doc, int posn, String tokenString, HashMap bindings,
                    PatternApplication patap) {
    pe.eval(doc,posn,tokenString,bindings,patap,target);
  }
}
