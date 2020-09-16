package cuny.blender.englishie.ace;

import java.io.File;

import cuny.blender.englishie.nlp.lex.Tokenizer;
import cuny.blender.englishie.nlp.lisp.FeatureSet;
import cuny.blender.englishie.nlp.scorer.NameTagger;
import cuny.blender.englishie.nlp.tipster.Annotation;
import cuny.blender.englishie.nlp.tipster.Document;
import cuny.blender.englishie.nlp.tipster.Span;
import cuny.blender.tagger.SentenceTagger;
import cuny.blender.utility.MyProperties;

public class CRFNameTagger implements NameTagger {

	SentenceTagger tagger;
	
	/**
	 * 
	 * @param model_name: the model name of the crf model under directory "data/"
	 */
	public CRFNameTagger(String model_name)
	{
		// Set property NER_HOME as where you locate the related data, props, etc.
		System.setProperty(MyProperties.NER_HOME, System.getProperty("jetHome")+"/modules/ner/");
		
		String currentPath = System.getProperty(MyProperties.NER_HOME);
		File modelFile = new File(currentPath + File.separator + "data" + File.separator + model_name);
		tagger = new SentenceTagger(modelFile);
	}
	
	public void tagDocument(Document doc) 
	{
		;
	}

	/**
	 *  tag span 'span' of Document 'doc' with Named Entity annotations.
	 */
	public void tag (Document doc, Span span) 
	{
		// gather tokens in textSpan;  if none, return
		Annotation[] tokens = Tokenizer.gatherTokens(doc, span);
		
		if (tokens.length == 0)
		{
			return;
		}
		
		String[] tokens_str = new String[tokens.length];
		for(int i=0; i<tokens.length; i++)
		{
			tokens_str[i] = doc.text(tokens[i]).trim();
		}
		
		// predict tags using CRF name tagger
		String[] tags = tagger.tagSentence(tokens_str);
		
		// if Viterbi decoder found no path through HMM, return
		if (tags == null) return;
		tagsToAnnotations(doc, tokens, tags);
	}
	
	private void tagsToAnnotations (Document doc, Annotation[] tokens, String[] tags) 
	{
		// convert tags to annotations
		// USE B/I/L/O/U tagging, e.g. B-PER is the first token of PER entity. we have three entites (PER, GPE, ORG)
		int start = -1; 				  // start an entity mention
		String type = null;    			  // one of PER, ORG, GPE
		for(int i=0; i<tags.length; i++)
		{
			String tag = tags[i];
			char prefix = tag.charAt(0);  // one of B, I, L, O, U
			
			if(prefix == 'B')
			{
				start = i;
				type = tag.substring(2);
			}
			else if(prefix == 'U')
			{
				type = tag.substring(2);
				annotateForTag(doc, type, tokens, i, i);
			}
			else if(start!=-1 && prefix == 'L')
			{
				annotateForTag(doc, type, tokens, start, i);
				start = -1;
				type = null;
			}
		}
	}
	
	private void annotateForTag (Document doc, String tag, Annotation[] tokens, int first, int last) 
	{
		int start = tokens[first].start();
		int end = tokens[last].end();
		Span span = new Span(start, end);
		
		// the tag table of HMMannotator
//		ENAMEX TYPE PERSON person
//		ENAMEX TYPE ORGANIZATION organization
//		ENAMEX TYPE GPE gpe
//		ENAMEX TYPE LOCATION location
//		ENAMEX TYPE FACILITY facility
		
		String tagEntry_1 = "TYPE";
		String tagEntry_2 = "";
		if(tag.equals("GPE"))
		{
			tagEntry_2 = "GPE";
		}
		else if(tag.equals("PER"))
		{
			tagEntry_2 = "PERSON";
		}
		else if(tag.equals("ORG"))
		{
			tagEntry_2 = "ORGANIZATION";
		}
		else
		{
			System.err.println("invalid entity type: " + tag);
			return;
		}
		
		FeatureSet fs = new FeatureSet(tagEntry_1, tagEntry_2);
        Annotation ann = new Annotation ("ENAMEX", span, fs);
        doc.addAnnotation(ann);
	}
}
