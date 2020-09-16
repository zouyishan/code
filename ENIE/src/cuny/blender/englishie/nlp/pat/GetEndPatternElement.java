
//Title:        JET
//Version:      1.00
//Copyright:    Copyright (c) 2000
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package cuny.blender.englishie.nlp.pat;

import java.util.HashMap;

import cuny.blender.englishie.nlp.lisp.Variable;
import cuny.blender.englishie.nlp.tipster.*;

public class GetEndPatternElement extends AtomicPatternElement {

  Variable variable;

  public GetEndPatternElement(Variable v) {
    variable = v;
  }

  public String toString () {
    return variable.toString() + ".end=* ";
  }

  public void eval (Document doc, int posn, String tokenString, HashMap bindings,
                    PatternApplication patap, PatternNode node) {
    Integer start = (Integer) bindings.get(variable.name);
    /*** should check that variable.name is bound ! */
    bindings = (HashMap) bindings.clone();
    bindings.put(variable.name,new Span(start.intValue(),posn));
    node.eval(doc, posn, bindings, patap);
  }
}
