from gensim.models import Word2Vec
import pickle
import numpy as np


def train_word2vec_model(model_name, word_lists, embed_dim):
	word_lists[0].insert(0, ' ')
	model = Word2Vec(word_lists, sg=1, size=embed_dim, window=5, min_count=1, negative=3, sample=0.001, hs=1,
					 workers=4, sorted_vocab=0)
	model.save(model_name)
	return model


def get_mine_model():
	data = pickle.load(open('C:\PyCharmProjects\dataset\mine\mine1.pkl', 'rb'))
	wl = data[0]
	wl = [s.split(' ') for s in wl]
	print(wl)
	model = train_word2vec_model('./models/word2vec/mine_01', wl, 256)
	print(model.wv.index2word)
	print(model.wv.similarity('思想', '意识'))
	return model


def get_bbc_model():
	x, y = pickle.load(open('./dataset/bbc_temp.pkl', 'rb'))
	word_list = x
	word_list[0].insert(0, ' ')
	# for s in word_list:
	# 	if ' ' in s:
	# 		print(s)

	model = train_word2vec_model('./models/word2vec/bbc_01', word_list, 256)
	print(model.wv.index2word)
	print(model.wv.similarity('tv', 'tvs'))
	return model


def load_model(model_name):
	model = Word2Vec.load(model_name)
	return model


def get_bert_embedding(model, tokenizer, word):
	token_ids, segment_ids = tokenizer.encode(word)
	token_ids = token_ids[1:-1]
	segment_ids = segment_ids[1:-1]
	y_pre = model.predict([np.array([token_ids]), np.array([segment_ids])])
	if len(token_ids) > 1:
		y_sum = np.sum(y_pre[0], axis=0)
		return np.array(y_sum / len(token_ids), dtype='float16')
	else:
		# print(word, y_pre.shape)
		return np.array(y_pre[0][0], dtype='float16')


def get_bert(wordlists):
	from bert4keras.models import build_transformer_model
	from bert4keras.tokenizers import Tokenizer
	from keras import Model
	import numpy as np

	config_path = 'C:/PyCharmProjects/python37gpu/nlp/bert_model/uncased_L-8_H-512_A-8/bert_config.json'
	checkpoint_path = 'C:/PyCharmProjects/python37gpu/nlp/bert_model/uncased_L-8_H-512_A-8/bert_model.ckpt'
	dict_path = 'C:/PyCharmProjects/python37gpu/nlp/bert_model/uncased_L-8_H-512_A-8/vocab.txt'

	tokenizer = Tokenizer(dict_path, do_lower_case=True)  # 建立分词器
	model = build_transformer_model(config_path, checkpoint_path)  # 建立模型，加载权重
	token_model = Model(inputs=model.input, outputs=model.get_layer('Embedding-Token').output)
	del model
	word_vectors = []
	for s in wordlists:
		embed = []
		for w in s:
			if w != '':
				embed.append(get_bert_embedding(token_model, tokenizer, w))
		# embed = embed + [[0 for j in range(embed_dim)] for i in range(maxlen - len(embed))]
		word_vectors.append(embed)

	return np.array(word_vectors)


def get_bert_bbc():
	x, y = pickle.load(open('./dataset/bbc_temp.pkl', 'rb'))
	word_list = x
	del x
	del y
	word_embeddings = get_bert(word_list)
	pickle.dump(word_embeddings, open('./dataset/bbc_word_vecs.pkl', 'wb'))
	print(word_embeddings.shape)
	print(word_embeddings[0][0])
	print(word_embeddings[0])


def get_bert_medical():
	with open('./dataset/medical_splited_wordlists.pkl', 'rb') as f:
		train_x, train_y, test_x, test_y = pickle.load(f)
		f.close()
	del train_y, test_y

	train_x = get_bert(train_x)
	test_x = get_bert(test_x)
	pickle.dump([train_x, test_x], open('./dataset/medical_word_vecs.pkl', 'wb'))
	print(train_x.shape)
	print(train_x[0][0])
	print(train_x[0])


if __name__ == '__main__':
	# get_mine_model()
	# get_bbc_model()
	import tensorflow as tf
	import keras.backend.tensorflow_backend as KTF

	config = tf.ConfigProto()
	config.gpu_options.allow_growth = True  # 不全部占满显存, 按需分配
	sess = tf.Session(config=config)
	KTF.set_session(sess)  # 设置session
	# get_bert_bbc()
	get_bert_medical()

