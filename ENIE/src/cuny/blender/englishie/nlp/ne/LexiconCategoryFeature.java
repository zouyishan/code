
package cuny.blender.englishie.nlp.ne;

import cuny.blender.englishie.nlp.lex.Lexicon;
import cuny.blender.englishie.nlp.lisp.FeatureSet;
import edu.umass.cs.mallet.base.pipe.Pipe;
import edu.umass.cs.mallet.base.types.Instance;
import edu.umass.cs.mallet.base.types.Token;
import edu.umass.cs.mallet.base.types.TokenSequence;

public class LexiconCategoryFeature extends Pipe {
	private String prefix;

	public LexiconCategoryFeature(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Instance pipe(Instance carrier) {
		TokenSequence tokens = (TokenSequence) carrier.getData();
		for (int i = 0 ; i < tokens.size(); i++) {
			Token token = tokens.getToken(i);
			String word = token.getText().toLowerCase();
			FeatureSet[] definitions = Lexicon.lookUp(new String[] { word } );

			if (definitions != null) {
				for (FeatureSet fs : definitions) {
					String name = prefix + fs.get("cat");
					token.setFeatureValue(name.intern(), 1.0);
				}
			}
		}

		return carrier;
	}

}
