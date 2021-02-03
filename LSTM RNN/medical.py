import pickle
from keras.layers import LSTM, Input, Embedding, Dense, Dropout, Masking
from keras.models import Model
import numpy as np
import os
from random import shuffle
import matplotlib.pyplot as plt
from keras.callbacks import TensorBoard, ModelCheckpoint
from keras.preprocessing.sequence import pad_sequences
import re
from nltk.corpus import stopwords

maxlen = 487
# 38892
vocab_num = 38892
embed_dim = 512
epochs = 50
batch_size = 100
save_dir = './models/medical/model_01/'

labels = ['Arthroskopie', 'Bundesgesundheitsblatt', 'DerAnaesthesist', 'DerChirurg', 'DerGynaekologe', 'DerHautarzt',
          'DerInternist', 'DerNervenarzt', 'DerOpthalmologe', 'DerOrthopaede', 'DerPathologe', 'DerRadiologe',
          'DerSchmerz', 'DerUnfallchirurg', 'DerUrologeA', 'EthikInDerMedizin', 'ForumDerPsychoanalyse',
          'Gefaesschirurgie', 'Herz', 'Herzschrittmachertherapie', 'HNO', 'IntensiveMedizin',
          'KlinischeNeuroradiologie', 'ManuelleMedizin', 'MedizinischeKlinik', 'MonatsschriftKinderheilkunde',
          'MundKieferGesichtschirurgie', 'Notfall+Rettungsmedizin', 'OperativeOrthopaedie', 'PerinatalMedizin',
          'Psychotherapeut', 'Rechtsmedizin', 'Reproduktionsmedizin', 'Strahlentherapie+Onkologie',
          'Trauma+Berufskrankheit', 'ZfuerGerontologie+Geriatrie', 'ZfuerHerzThoraxGefaesschirurgie',
          'ZfuerKardiologie', 'ZfuerRheumatologie']
stopwords_eng = stopwords.words('english')


def split_data():
	with open('wordlist_label.pkl', 'rb') as f:
		x = pickle.load(f)
		y = x[1]
		x = x[0]
		x = [[w for w in s if w not in stopwords_eng]for s in x]
		f.close()
		length = [len(s) for s in x]
		print(max(length))
	z = list(zip(x, y))
	z.sort(key=lambda t:t[1])
	train = []
	test = []
	cn = [0 for l in labels]
	for l in y:
		cn[labels.index(l)] += 1

	for i in range(len(labels)):
		s = cn[i] // 5
		if s == 0:
			s = 1
		n = 0
		for xy in z:
			if xy[1] == labels[i]:
				if n < s:
					test.append(xy)
				else:
					train.append(xy)
				n += 1
	train_x = [xy[0] for xy in train]
	train_y = [xy[1] for xy in train]
	test_x = [xy[0] for xy in test]
	test_y = [xy[1] for xy in test]
	print(train_x[0])
	with open('./dataset/medical_splited_wordlists.pkl', 'wb') as f:
		pickle.dump([train_x, train_y, test_x, test_y], f)
		f.close()


def process_data():
	with open('./dataset/medical_splited_wordlists.pkl', 'rb') as f:
		train_x, train_y, test_x, test_y = pickle.load(f)
		f.close()

	# y = [labels.index(label) for label in y]
	# y = np.eye(39)[y]
	# x = np.array(x)
	# return 1
	# print(x.shape)
	# print(y.shape)
	# pickle.dump([x, y], open('./dataset/medical_unsplited', 'wb'))

	return 1


def load_data():
	train_x, train_y, test_x, test_y = pickle.load(open('./dataset/medical_splited_wordlists.pkl', 'rb'))
	train_x, test_x = pickle.load(open('./dataset/medical_word_vecs.pkl', 'rb'))

	ul = ['Notfall+Rettungsmedizin', 'MedizinischeKlinik', 'Strahlentherapie+Onkologie', 'Trauma+Berufskrankheit',
		  'PerinatalMedizin', 'EthikInDerMedizin', 'OperativeOrthopaedie', 'KlinischeNeuroradiologie', 'DerInternist',
		  'Psychotherapeut', 'Gefaesschirurgie', 'Rechtsmedizin', 'Arthroskopie', 'ZfuerHerzThoraxGefaesschirurgie',
		  'Herzschrittmachertherapie', 'Bundesgesundheitsblatt', 'Reproduktionsmedizin', 'Herz', 'ManuelleMedizin',
		  'ForumDerPsychoanalyse']
	train_y = train_y.tolist()
	test_y = test_y.tolist()
	train_x = train_x.tolist()
	test_y = test_y.tolist()

	train_x = pad_sequences(train_x, maxlen=maxlen, dtype='float16')
	test_x = pad_sequences(test_x, maxlen=maxlen, dtype='float16')
	train_y = [labels.index(label) for label in train_y]
	test_y = [labels.index(label) for label in test_y]
	train_y = np.eye(len(labels))[train_y]
	test_y = np.eye(len(labels))[test_y]
	return train_x, train_y, test_x, test_y


def create_model():
	x = Input(shape=(maxlen, embed_dim))
	embed = Masking(mask_value=0.)(x)
	lstm = LSTM(256, dropout=0.25)(embed)
	dense_1 = Dense(64, activation='relu')(lstm)
	dense_1 = Dropout(0.2)(dense_1)
	dense_2 = Dense(32, activation='relu')(dense_1)
	dense_2 = Dropout(0.25)(dense_2)
	y = Dense(len(labels), activation='softmax')(dense_2)
	model = Model(x, y)
	return model


def get_model(is_train=True):
	train_x, train_y, test_x, test_y = load_data()
	print(train_x.shape, train_y.shape)
	print(test_x.shape, test_y.shape)

	model = create_model()
	model.summary()

	import tensorflow as tf
	import keras.backend.tensorflow_backend as KTF

	config = tf.ConfigProto()
	config.gpu_options.allow_growth = True  # 不全部占满显存, 按需分配
	sess = tf.Session(config=config)
	KTF.set_session(sess)  # 设置session

	if is_train:
		if not os.path.exists(save_dir):
			os.makedirs(save_dir)
		model.compile('adam', loss='categorical_crossentropy', metrics=['accuracy'])
		checkpoit = ModelCheckpoint(filepath=save_dir + '_trained_best_model.h5', monitor='val_acc',
									save_best_only='True', mode='max', period=1)
		tensorboard = TensorBoard(log_dir=save_dir + '/log')
		callback_lists = [tensorboard, checkpoit]  # 因为callback是list型,必须转化为list

		history = model.fit(train_x, train_y, validation_data=[test_x, test_y], batch_size=batch_size,
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
	# split_data()
	#
	model, data = get_model(is_train=True)
	# model, data = get_model(is_train=False)
	train_x, train_y, test_x, test_y = data
	test_model(model, test_x, test_y)

	# train_x, train_y, test_x, test_y = load_data()
	# train_y = [l.index(1) for l in train_y.tolist()]
	# test_y = [l.index(1) for l in test_y.tolist()]
	# cnt = [0 for i in labels]
	# for l in train_y:
	# 	cnt[l] += 1
	# cnt2 = [0 for i in labels]
	# for l in test_y:
	# 	cnt2[l] += 1
	# s = [a / (a + b) for a, b in zip(cnt2, cnt)]
	# print(cnt)
	# print(cnt2)
	# print(s)

