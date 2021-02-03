import pickle
from keras.layers import LSTM, Input, Embedding, Dense, Dropout, Masking
from keras.models import Model
import numpy as np
import os
from random import shuffle
import matplotlib.pyplot as plt
import word2vec_models
from keras.callbacks import TensorBoard, ModelCheckpoint
from keras.preprocessing.sequence import pad_sequences
from nltk.corpus import stopwords
import pandas as pd
import re

#
maxlen = 2230
#
vocab_num = 8000
embed_dim = 512
epochs = 32
batch_size = 32
save_dir = './models/bbc/model_05/'

labels = ['tech', 'business', 'sport', 'entertainment', 'politics']
stopwords_eng = stopwords.words('english')


def sentence2words(sentence):
	sentence = sentence.replace(' ', ' ')
	sentence = sentence.replace('-', ' ')
	sentence = sentence.replace(':', ' ')

	sentence = re.sub(r'[^a-zA-Z0-9\' ,]', '', sentence).lower()
	sentence = sentence.replace(',', ' , ')
	sentence = re.sub(' +', ' ', sentence)
	sentence = sentence.strip(' ')
	words = sentence.split(' ')
	words = [w.strip('\'') for w in words]
	for w in words:
		if w in stopwords_eng:
			print(w)
	words = [w for w in words if w not in stopwords_eng]
	return words


def process_data():
	df = pd.read_csv('./dataset/bbc-text.csv')
	y = df['category'].tolist()
	x = df['text'].tolist()
	y = [labels.index(l) for l in y]
	x = [sentence2words(s) for s in x]
	print(x[0])
	print(y[0])
	l = [len(x) for s in x]
	print(max(l))
	pickle.dump([x, y], open('./dataset/bbc_temp.pkl', 'wb'))
	# word_embeddings = pickle.load(open('./dataset/bbc_word_vecs.pkl', 'rb'))
	# word_embeddings = pad_sequences(word_embeddings, maxlen=maxlen, dtype='float16')
	# print(word_embeddings[0])
	# print(word_embeddings.shape)


def load_data():
	x, y = pickle.load(open('bbc_8000.pkl', 'rb'))
	y = np.eye(len(labels))[y]
	x = pickle.load(open('./dataset/bbc_word_vecs.pkl', 'rb'))
	x = pad_sequences(x, maxlen=maxlen, dtype='float16')
	s = len(x) // 5
	train_x = x[:-s]
	train_y = y[:-s]
	test_x = x[-s:]
	test_y = y[-s:]
	return train_x, train_y, test_x, test_y


def create_model():
	x = Input(shape=(maxlen, embed_dim))

	# embed = Embedding(vocab_num, embed_dim, input_length=maxlen, mask_zero=True)(x)
	# word2vec_model = word2vec_models.load_model('./models/word2vec/bbc_01')
	# embedding_layer = word2vec_model.wv.get_keras_embedding()
	# embedding_layer.trainable = False
	# embedding_layer.mask_zero = True
	# embed = embedding_layer(x)

	# BiLSTM = Bidirectional(LSTM(128, return_sequences=True))(embed)
	# BiLSTM = LSTM(256, return_sequences=True, dropout=0.25)(embed)
	embed = Masking(mask_value=0.)(x)
	lstm = LSTM(256, dropout=0.25)(embed)
	dense_1 = Dense(64, activation='relu')(lstm)
	dense_1 = Dropout(0.2)(dense_1)
	dense_2 = Dense(32, activation='relu')(dense_1)
	dense_2 = Dropout(0.25)(dense_2)
	y = Dense(len(labels), activation='softmax')(dense_2)

	model = Model(x, y)
	return model


def get_model(isTrain=True, isBest=True):
	# train_s, train_tags, test_s, test_tags = load_data(domain)
	train_x, train_y, test_x, test_y = load_data()
	model = create_model()
	model.summary()

	import tensorflow as tf
	import keras.backend.tensorflow_backend as KTF

	config = tf.ConfigProto()
	config.gpu_options.allow_growth = True  # 不全部占满显存, 按需分配
	sess = tf.Session(config=config)
	KTF.set_session(sess)  # 设置session

	if isTrain:
		if not os.path.exists(save_dir):
			os.makedirs(save_dir)

		model.compile('adam', loss='categorical_crossentropy', metrics=['accuracy'])

		checkpoit = ModelCheckpoint(filepath=save_dir + '_trained_best_model.h5', monitor='val_acc',
									save_best_only='True', mode='max', period=1)
		tensorboard = TensorBoard(log_dir=save_dir + '/log')
		callback_lists = [tensorboard, checkpoit]  # 因为callback是list型,必须转化为list

		history = model.fit(train_x, train_y, validation_data=[test_x, test_y],	batch_size=batch_size,
									  epochs=epochs, callbacks=callback_lists)
		model.save_weights(save_dir + '_trained_model.h5')

		fig = plt.figure()  # 新建一张图
		plt.plot(history.history['acc'], label='training acc')
		plt.plot(history.history['val_acc'], label='val acc')
		plt.title('model accuracy')
		plt.ylabel('accuracy')
		plt.xlabel('epoch')
		plt.legend(loc='lower right')
		fig.savefig(save_dir + '_acc.png')
		fig = plt.figure()
		plt.plot(history.history['loss'], label='training loss')
		plt.plot(history.history['val_loss'], label='val loss')
		plt.title('model loss')
		plt.ylabel('loss')
		plt.xlabel('epoch')
		plt.legend(loc='upper right')
		fig.savefig(save_dir + '_loss.png')
	else:
		if isBest:
			model.load_weights(save_dir + '_trained_best_model.h5')
		else:
			model.load_weights(save_dir + '_trained_model.h5')
	return model, [train_x, train_y, test_x, test_y]


def test_model(model, test_x, test_y):
	y_pre = model.predict(test_x)
	y_pre = y_pre.tolist()
	y_pre = [s.index(max(s)) for s in y_pre]
	test_y = test_y.tolist()
	test_y = [s.index(max(s)) for s in test_y]

	p = []
	r = []
	f1 = []
	tp_sum = 0
	fp_sum = 0
	fn_sum = 0
	for cat in range(len(labels)):
		tp = 0
		tn = 0
		fp = 0
		fn = 0
		for pre, true in zip(y_pre, test_y):
			if true == cat:
				if pre == true:
					tp += 1
				else:
					fn += 1
			else:
				if pre == cat:
					fp += 1
				else:
					tn += 1
		# print(tp, tn, fp, fn)
		if tp + fp == 0:
			precision = 0
		else:
			precision = tp / (tp + fp)
		if tp + fn == 0:
			recall = 0
		else:
			recall = tp / (tp + fn)
		if precision + recall == 0:
			feature1 = 0
		else:
			feature1 = 2 * precision * recall / (precision + recall)
		p.append(precision)
		r.append(recall)
		f1.append(feature1)

		tp_sum += tp
		fp_sum += fp
		fn_sum += fn

	for i in range(len(labels)):
		print(labels[i], p[i], r[i], f1[i])
	# p = [precision for precision in p if precision != 0]
	# r = [recall for recall in r if recall != 0]
	# f1 = [feature1 for feature1 in f1 if feature1 != 0]
	print('marco', np.mean(p), np.mean(r), np.mean(f1))
	p_micro = tp_sum / (tp_sum + fp_sum)
	r_micro = tp_sum / (tp_sum + fn_sum)
	f1_micro = 2 * p_micro * r_micro / (p_micro + r_micro)
	print('micro', p_micro, r_micro, f1_micro)
	return p, r, f1


if __name__ == '__main__':
	# process_data()
	model, data = get_model(isTrain=True)
	# model, data = get_model(isTrain=False)
	# model, data = get_model(isTrain=False, isBest=False)
	train_x, train_y, test_x, test_y = data
	test_model(model, test_x, test_y)

	# model, data = get_model(isTrain=False)
	# train_x, train_y, test_x, test_y = data
	# test_model(model, test_x, test_y)

	# x, y = pickle.load(open('./dataset/bbc_temp.pkl', 'rb'))
	# print(x[408])
	# print(len(x[408]))


