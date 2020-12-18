import fasttext
from random import shuffle
import pickle
import re
import numpy as np

dir = 'C:/PycharmProjects/dataset/medical/'
labels = ['Arthroskopie', 'Bundesgesundheitsblatt', 'DerAnaesthesist', 'DerChirurg', 'DerGynaekologe', 'DerHautarzt',
          'DerInternist', 'DerNervenarzt', 'DerOpthalmologe', 'DerOrthopaede', 'DerPathologe', 'DerRadiologe',
          'DerSchmerz', 'DerUnfallchirurg', 'DerUrologeA', 'EthikInDerMedizin', 'ForumDerPsychoanalyse',
          'Gefaesschirurgie', 'Herz', 'Herzschrittmachertherapie', 'HNO', 'IntensiveMedizin',
          'KlinischeNeuroradiologie', 'ManuelleMedizin', 'MedizinischeKlinik', 'MonatsschriftKinderheilkunde',
          'MundKieferGesichtschirurgie', 'Notfall+Rettungsmedizin', 'OperativeOrthopaedie', 'PerinatalMedizin',
          'Psychotherapeut', 'Rechtsmedizin', 'Reproduktionsmedizin', 'Strahlentherapie+Onkologie',
          'Trauma+Berufskrankheit', 'ZfuerGerontologie+Geriatrie', 'ZfuerHerzThoraxGefaesschirurgie',
          'ZfuerKardiologie', 'ZfuerRheumatologie']
# trainPath = dir + 'medical_train.txt'
# testPath = dir + 'medical_test.txt'
trainPath = dir + 'medical_processed_train.txt'
testPath = dir + 'medical_processed_test.txt'
# trainPath = dir + 'medical_processed_30_train.txt'
# testPath = dir + 'medical_processed_30_test.txt'


def load_data_temp():
	y = []
	x = []
	with open(dir + 'temp.txt', 'r') as f:
		for line in f.readlines():
			label, text = line.split('\t')
			text = text[2:-4].strip(' ')
			y.append(label)
			x.append(text)
		f.close()
	return x, y


def get_shuffled_pickle(x, y):
	z = list(zip(x, y))
	shuffle(z)
	x = [xy[0] for xy in z]
	y = [xy[1] for xy in z]
	del z
	print(len(x))
	s = len(x) // 5
	train_x = x[:-s]
	train_y = y[:-s]
	test_x = x[-s:]
	test_y = y[-s:]
	print(len(train_x), len(test_x))
	print(train_y[:10])
	with open(dir + 'shuffled_pickle.pkl', 'wb') as f:
		pickle.dump([train_x, train_y, test_x, test_y], f)
		f.close()


def load_data():
	with open(dir + 'splited_pickle.pkl', 'rb') as f:
		train_x, train_y, test_x, test_y = pickle.load(f)
		f.close()
	return train_x, train_y, test_x, test_y


def display_dataset_stat():
	train_x, train_y, test_x, test_y = load_data()
	cn = [0 for l in labels]
	for l in train_y:
		cn[labels.index(l)] += 1
	for l, c in zip(labels, cn):
		print(l, end='\t')
		print(c)


def split_data():
	x, y = load_data_temp()
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
	with open(dir + 'splited_pickle.pkl', 'wb') as f:
		pickle.dump([train_x, train_y, test_x, test_y], f)
		f.close()


def write_txt(txtName):
	train_x, train_y, test_x, test_y = load_data()
	# txtName = 'medical'
	with open(dir + txtName + '_train.txt', 'w', encoding='utf-8') as f:
		for news, group in zip(train_x, train_y):
			f.write(news)
			f.write('\t__label__' + group + '\n')
		f.close()
	with open(dir + txtName + '_test.txt', 'w', encoding='utf-8') as f:
		for news, group in zip(test_x, test_y):
			f.write(news)
			f.write('\t__label__' + group + '\n')
		f.close()


def process_sentence(text):
	text = re.sub(r'[^a-zA-Z0-9 \-\']', ' ', text).lower()
	text = re.sub(' +', ' ', text).strip(' ')
	return text


def process_data():
	train_x, train_y, test_x, test_y = load_data()
	ul = ['Notfall+Rettungsmedizin', 'MedizinischeKlinik', 'Strahlentherapie+Onkologie', 'Trauma+Berufskrankheit',
	      'PerinatalMedizin', 'EthikInDerMedizin', 'OperativeOrthopaedie', 'KlinischeNeuroradiologie', 'DerInternist',
	      'Psychotherapeut', 'Gefaesschirurgie', 'Rechtsmedizin', 'Arthroskopie', 'ZfuerHerzThoraxGefaesschirurgie',
	      'Herzschrittmachertherapie', 'Bundesgesundheitsblatt', 'Reproduktionsmedizin', 'Herz', 'ManuelleMedizin',
	      'ForumDerPsychoanalyse']
	for i in range(len(train_x) - 1, 0, -1):
		if train_y[i] in ul:
			train_x.pop(i)
			train_y.pop(i)
	for i in range(len(test_x)-1, 0, -1):
		if test_y[i] in ul:
			test_x.pop(i)
			test_y.pop(i)

	train_x = [process_sentence(text) for text in train_x]
	test_x = [process_sentence(text) for text in test_x]
	txtName = 'medical_processed_20'
	with open(dir + txtName + '_train.txt', 'w', encoding='utf-8') as f:
		for news, group in zip(train_x, train_y):
			f.write(news)
			f.write('\t__label__' + group + '\n')
		f.close()
	with open(dir + txtName + '_test.txt', 'w', encoding='utf-8') as f:
		for news, group in zip(test_x, test_y):
			f.write(news)
			f.write('\t__label__' + group + '\n')
		f.close()


def train(modelName):
	model = fasttext.train_supervised(trainPath, dim=100, ws=5, epoch=180, wordNgrams=2, thread=4, minCount=3)
	model.save_model(modelName)
	return model


def test_model(model, test_x, test_y):
	y_pre = model.predict(test_x)
	y_pre = [g[0][9:] for g in y_pre[0]]
	p = []
	r = []
	f1 = []
	tp_sum = 0
	fp_sum = 0
	fn_sum = 0
	for cat in labels:
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
	modelName = './models/fasttext_medical_30_05.bin'
	# model = train(modelName)
	model = fasttext.load_model(modelName)

	# result = model.test(trainPath)
	# print(result)
	result = model.test(testPath)
	print(result)

	train_x, train_y, test_x, test_y = load_data()
	# # ul = ['Notfall+Rettungsmedizin', 'MedizinischeKlinik', 'Strahlentherapie+Onkologie', 'Trauma+Berufskrankheit',
	# #       'PerinatalMedizin', 'EthikInDerMedizin', 'OperativeOrthopaedie', 'KlinischeNeuroradiologie', 'DerInternist',
	# #       'Psychotherapeut', 'Gefaesschirurgie', 'Rechtsmedizin', 'Arthroskopie', 'ZfuerHerzThoraxGefaesschirurgie',
	# #       'Herzschrittmachertherapie', 'Bundesgesundheitsblatt', 'Reproduktionsmedizin', 'Herz', 'ManuelleMedizin',
	# #       'ForumDerPsychoanalyse']
	ul = ['Notfall+Rettungsmedizin', 'MedizinischeKlinik', 'Strahlentherapie+Onkologie', 'Trauma+Berufskrankheit',
	      'PerinatalMedizin', 'EthikInDerMedizin', 'OperativeOrthopaedie', 'KlinischeNeuroradiologie', 'DerInternist']
	labels = [l for l in labels if l not in ul]
	# # for i in range(len(test_x) - 1, 0, -1):
	# # 	if train_y[i] in ul:
	# # 		train_x.pop(i)
	# # 		train_y.pop(i)
	for i in range(len(test_x) - 1, 0, -1):
		if test_y[i] in ul:
			test_x.pop(i)
			test_y.pop(i)
	test_model(model, test_x, test_y)
	# test_model(model, train_x, train_y)

	# y_pre = model.predict(test_x)
	# y_pre = [g[0][9:] for g in y_pre[0]]
	# special_l = ['Reproduktionsmedizin', 'ZfuerHerzThoraxGefaesschirurgie', 'ManuelleMedizin', 'Herz']
	# for l, t in zip(y_pre, test_y):
	# 	if t in special_l:
	# 		print(l, t)
		# if l =='DerChirurg':
		# 	print(l, t)

	# train_x, train_y, test_x, test_y = load_data()
	# train_x = [process_sentence(s).split(' ') for s in train_x]
	# test_x = [process_sentence(s).split(' ') for s in test_x]
	#
	# len_trainx = [len(s) for s in train_x]
	# len_total = len_trainx + [len(s) for s in test_x]
	# len_total.sort(reverse=True)
	# print(len_total[:100])
	# print(np.average(len_total))

	# ct = [0 for i in range(len(labels))]
	# ctest = [0 for i in range(len(labels))]
	# for y in train_y:
	# 	ct[labels.index(y)] += 1
	# for y in test_y:
	# 	ctest[labels.index(y)] += 1
	# print(ct)
	# print(ctest)
