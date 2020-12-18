import pickle
import jieba
import re
from random import shuffle
import fasttext
import numpy as np

dir = 'C:/PycharmProjects/dataset/mine/'
originFile = 'accidents.txt'
labels = ['瓦斯事故', '顶板事故', '运输事故', '工伤事故', '机电事故', '透水事故', '火灾事故', '爆炸事故']
trainPath = dir + 'mine_train.txt'
testPath = dir + 'mine_test.txt'


def read_data():
	x = []
	y = []
	with open(dir + originFile, 'r', encoding='utf-8') as f:
		for line in f.readlines():
			label, text = line.split('\t')
			text = re.sub(r'[^\u4e00-\u9fa50-9a-zA-Z]', ' ', text)
			text = ' '.join(jieba.cut(text))
			text = re.sub(' +', ' ', text)
			text = text.encode('utf-8').decode('utf-8')
			x.append(text)
			# x.append(text.strip('\n'))
			y.append(label)

	for i in range(len(y) - 1, 0, -1):
		if y[i] not in labels:
			x.pop(i)
			y.pop(i)

	z = list(zip(x, y))
	shuffle(z)
	x = [xy[0] for xy in z]
	y = [xy[1] for xy in z]
	del z

	s = len(y) //5

	train_y = y[:-s]
	test_y = y[-s:]
	train_x = x[:-s]
	test_x = x[-s:]
	txtName = 'mine'

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

	with open(dir + 'mine1.pkl', 'wb') as f:
		pickle.dump([train_x, train_y, test_x, test_y], f)
		f.close()


def load_data():
	with open(dir + 'mine1.pkl', 'rb') as f:
		train_x, train_y, test_x, test_y =pickle.load(f)
		f.close()
	return train_x, train_y, test_x, test_y


def train(modelName):
	model = fasttext.train_supervised(trainPath, dim=100, ws=5, epoch=180, wordNgrams=2, thread=4)
	print(len(model.words))
	print(model.labels)
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
	print('marco', np.mean(p), np.mean(r), np.mean(f1))
	p_micro = tp_sum / (tp_sum + fp_sum)
	r_micro = tp_sum / (tp_sum + fn_sum)
	f1_micro = 2 * p_micro * r_micro / (p_micro + r_micro)
	print('micro', p_micro, r_micro, f1_micro)
	return p, r, f1


def get_similarity(a, b):
	return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))


if __name__ == '__main__':
	# read_data()
	modelName = './models/fasttext_mine_03.bin'
	# model = train(modelName)
	model = fasttext.load_model(modelName)

	# result = model.test(trainPath)
	# print(result)
	# result = model.test(testPath)
	# print(result)
	# train_x, train_y, test_x, test_y = load_data()
	# test_model(model, test_x, test_y)

	a = model['道轨']
	b = model['轨道']
	print(get_similarity(a, b))

	# ct = [0 for i in range(8)]
	# ctest = [0 for i in range(8)]
	# for y in train_y:
	# 	ct[labels.index(y)] += 1
	# for y in test_y:
	# 	ctest[labels.index(y)] += 1
	# print(ct)
	# print(ctest)
	# len_t = [len(s.strip(' ')) for s in train_x] + [len(s.strip(' ')) for s in test_x]
	# len_t.sort(reverse=True)
	# print(len_t[:100])
	# print(np.average(len_t))
	# train_x, train_y, test_x, test_y = load_data()
	# x = train_x + test_x
	# words = []
	# for s in x:
	# 	words += s.split(' ')
	# # wlen = [len(w) for w in words]
	# for w in words:
	# 	# if len(w) == 3:
	# 	if '爆' in w:
	# 		print(w)
