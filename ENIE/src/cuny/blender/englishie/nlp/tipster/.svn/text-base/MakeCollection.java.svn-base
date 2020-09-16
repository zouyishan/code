
package cuny.blender.englishie.nlp.tipster;

import java.util.*;
import java.io.*;

import cuny.blender.englishie.nlp.JetTest;

/**
 *  MakeCollection input-file collection-file collection-directory
 *
 *  converts a single file with multiple documents into a Jet collection
 *
 *  input-file:            a concatenated collection of documents in a single
 *                         file, each beginning with <DOC> and ending with </DOC>
 *  collection-file:       name of Collection file to write
 *  collection-directory:  directory into which to put document files
 *                         (relative to collection file)
 *  ext                    file extension for document files
 */

public class MakeCollection {

  public static void main (String[] args) {
    if (args.length != 4) {
      System.out.println ("MakeCollection must have 4 arguments: \n" +
                          "input-file collection-file collection-directory file-extension");
      System.exit(1);
    }
    String inputFileName = args[0];
    String collectionFileName = args[1];
    String directory = args[2];
    String ext = args[3];
    Document doc;
    Vector files = new Vector(100);
    try {
      BufferedReader rdr = new BufferedReader (new FileReader (inputFileName));
      File collectionFile = new File(collectionFileName);
      String collectionDirectory = collectionFile.getParent();
      while ((doc = JetTest.readDocument (rdr)) !=  null) {
			  if (JetTest.docId == "") {
				  System.out.println ("Error:  document without <DOCID>");
				} else {
				  String docFileName = directory + "/" + JetTest.docId + "." + ext;
				  files.add(docFileName);
				  File docFile = new File (collectionDirectory, docFileName);
				  PrintWriter docStream = new PrintWriter (new FileWriter (docFile));
				  docStream.println (doc.text());
				  docStream.close();
				}
			}

			PrintWriter colStream = new PrintWriter (new FileWriter (collectionFile));
      for (int i=0; i<files.size(); i++) {
      	colStream.println ("sgml|" + files.get(i));
      }
      colStream.close();
    } catch (IOException ioe) {
      System.err.println ("IO Error in MakeCollection: " + ioe);
      System.exit (1);
    }
  }

}
