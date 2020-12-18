import pandas as pd
import fasttext
import numpy as np


dir = 'C:\PycharmProjects\dataset\\bbc\\'
csv_path = 'C:\PycharmProjects\dataset\\bbc\\bbc-text.csv'
trainPath = dir + 'bbc_news_train.txt'
testPath = dir + 'bbc_news_test.txt'
cats = ['tech', 'business', 'sport', 'entertainment', 'politics']


def read_data():
	df = pd.read_csv(csv_path)
	y = df['category'].tolist()
	x = df['text'].tolist()

	s = len(y) //5

	train_y = y[:-s]
	test_y = y[-s:]
	train_x = x[:-s]
	test_x = x[-s:]
	txtName = 'bbc_news'

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


def load_data():
	df = pd.read_csv(csv_path)
	y = df['category'].tolist()
	x = df['text'].tolist()

	s = len(y) // 5

	train_y = y[:-s]
	test_y = y[-s:]
	train_x = x[:-s]
	test_x = x[-s:]
	return train_x, train_y, test_x, test_y


def train(modelName):
	# model = fasttext.supervised(trainPath, "news_fasttext.model", label_prefix="__label__")
	# model = fasttext.train_supervised(trainPath)
	model = fasttext.train_supervised(trainPath, dim=100, ws=5, epoch=150, wordNgrams=2, thread=4)
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
	for cat in cats:
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
		precision = tp / (tp + fp)
		recall = tp / (tp + fn)
		feature1 = 2 * precision * recall / (precision + recall)
		p.append(precision)
		r.append(recall)
		f1.append(feature1)

		tp_sum += tp
		fp_sum += fp
		fn_sum += fn

	for i in range(len(p)):
		print(cats[i], p[i], r[i], f1[i])
	print('marco', np.mean(p), np.mean(r), np.mean(f1))
	p_micro = tp_sum / (tp_sum + fp_sum)
	r_micro = tp_sum / (tp_sum + fn_sum)
	f1_micro = 2 * p_micro * r_micro / (p_micro + r_micro)
	print('micro', p_micro, r_micro, f1_micro)
	# return p, r, f1


if __name__ == '__main__':
	modelName = './models/fasttext_bbc_news_01.bin'
	# model = train(modelName)
	model = fasttext.load_model(modelName)

	# result = model.test(trainPath)
	# print(result)
	result = model.test(testPath)
	print(result)
	train_x, train_y, test_x, test_y = load_data()
	test_model(model, test_x, test_y)

	# ct = [0 for i in range(len(cats))]
	# ctest = [0 for i in range(len(cats))]
	# for y in train_y:
	# 	ct[cats.index(y)] += 1
	# for y in test_y:
	# 	ctest[cats.index(y)] += 1
	# print(ct)
	# print(ctest)

	# len_t = [len(s.strip(' ')) for s in train_x] + [len(s.strip(' ')) for s in test_x]
	# len_t.sort(reverse=True)
	# print(len_t[:100])
	# print(np.average(len_t))


