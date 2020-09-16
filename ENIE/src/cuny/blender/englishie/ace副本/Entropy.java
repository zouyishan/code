//Title:        JET
//Copyright:    2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Toolkit
//              (ACE extensions)

package cuny.blender.englishie.ace;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.evaluation.event.BCubeCorefEval;
import cuny.blender.englishie.evaluation.event.CEAFCorefEval;
import cuny.blender.englishie.evaluation.event.MUCCorefEval;
import cuny.blender.englishie.nlp.Control;
import cuny.blender.englishie.nlp.JetTest;
import cuny.blender.englishie.nlp.lex.PorterStemmer;
import cuny.blender.englishie.nlp.parser.*;
import cuny.blender.englishie.nlp.pat.Pat;
import cuny.blender.englishie.nlp.refres.Resolve;
import cuny.blender.englishie.nlp.tipster.*;
import cuny.blender.englishie.nlp.wordnet.similarity.SimilarityAssessor;


import opennlp.maxent.*;
import opennlp.maxent.io.*;


/**
 * assigns ACE events to a Document, given the entities, times, and values.
 */

public class Entropy {
	static final int M = 5;
	static final int K = 3;
	static final int N= 14;
	static void main (String[] args) throws IOException {

		int[][] a = new int[][]{{4,0,0,0},{1,2,0,0,0},{0,4,1,1,1}};
		double entropy_C = 0;
		for (int i=0;i<K;i++){
			double b = 0;
			for (int j=0;j<M;j++){
				b+= a[i][j];
			}
			entropy_C+= (b/N)*Math.log(b/N);
		}
		entropy_C=-entropy_C;
		System.out.println(entropy_C);
		
	}

}
