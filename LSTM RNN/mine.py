import pickle
from keras.layers import LSTM, Input, Embedding, Dense, Dropout
from keras.models import Model
import numpy as np
import os
from random import shuffle
import matplotlib.pyplot as plt
import word2vec_models
from keras.callbacks import TensorBoard, ModelCheckpoint
from sklearn.feature_extraction.text import TfidfVectorizer

# 369 93
maxlen = 93
# 1351 3046
vocab_num = 3046
embed_dim = 128
epochs = 30
batch_size = 32
save_dir = './models/mine/model_05/'

labels = ['瓦斯事故', '顶板事故', '运输事故', '工伤事故', '机电事故', '透水事故', '火灾事故', '爆炸事故']
stopwords_cn = [l.strip() for l in open('cn_stopwords.txt', 'r', encoding='utf-8').readlines()]


def process_data():
	data = pickle.load(open('C:\PyCharmProjects\dataset\mine\mine1.pkl', 'rb'))
	x = data[0]
	y = data[1]
	x = [s.split(' ') for s in x]
	x = [[w for w in s if w not in stopwords_cn]for s in x]
	l = [len(s) for s in x]
	print(max(l))
	del l

	vocab = [' ']
	for s in x:
		for w in s:
			if w not in vocab:
				vocab.append(w)
	print(len(vocab))

	x = [[0 for i in range(maxlen - len(s))] + [vocab.index(w) for w in s] for s in x]
	y = [labels.index(label) for label in y]
	y = np.eye(len(labels))[y]
	x = np.array(x)

	print(x.shape)
	print(y.shape)

	z = list(zip(x.tolist(), y.tolist()))
	shuffle(z)
	x = [xy[0] for xy in z]
	y = [xy[1] for xy in z]
	del z
	x = np.array(x)
	y = np.array(y)
	s = len(x) // 5
	train_x = x[:-s]
	train_y = y[:-s]
	test_x = x[-s:]
	test_y = y[-s:]
	print(len(train_x), len(test_x))
	print(train_y[:10])
	with open('dataset/mine_shuffled_pickle.pkl', 'wb') as f:
		pickle.dump([train_x, train_y, test_x, test_y], f)
		f.close()


def load_data():
	with open('dataset/mine_shuffled_pickle.pkl', 'rb') as f:
		train_x, train_y, test_x, test_y = pickle.load(f)
		f.close()
	return train_x, train_y, test_x, test_y


def create_model():
	x = Input(shape=(maxlen,))

	embed = Embedding(vocab_num, embed_dim, input_length=maxlen, mask_zero=True)(x)
	# word2vec_model = word2vec_models.load_model('./models/word2vec/mine_01')
	# embedding_layer = word2vec_model.wv.get_keras_embedding()
	# embedding_layer.trainable = False
	# embedding_layer.mask_zero = True
	# embed = embedding_layer(x)

	# lstm = LSTM(256, return_sequences=True, dropout=0.25)(embed)
	lstm = LSTM(128, dropout=0.25)(embed)

	dense_1 = Dense(64, activation='relu')(lstm)
	dense_1 = Dropout(0.2)(dense_1)
	dense_2 = Dense(32, activation='relu')(dense_1)
	dense_2 = Dropout(0.3)(dense_2)
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
	for cat in range(8):
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

	for i in range(len(p)):
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

	model, data = get_model(isTrain=False)
	train_x, train_y, test_x, test_y = data
	test_model(model, test_x, test_y)
