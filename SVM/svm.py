import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.svm import SVC, LinearSVC
from sklearn import metrics
from sklearn.preprocessing import StandardScaler
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics import recall_score
from sklearn.metrics import precision_score
from sklearn.metrics import f1_score
import jieba

def show(prediction, s):
    print(s)
    print("准确率：", metrics.accuracy_score(prediction, test_y))
    print('')
    print("F值", f1_score(test_y, prediction, average='macro'))
    print("F值", f1_score(test_y, prediction, average='micro'))
    print("F值", f1_score(test_y, prediction, average='weighted'))
    print("F值", f1_score(test_y, prediction, average=None))
    print('')
    print("精确率", precision_score(test_y, prediction, average='macro'))
    print("精确率", precision_score(test_y, prediction, average='micro'))
    print("精确率", precision_score(test_y, prediction, average='weighted'))
    print("精确率", precision_score(test_y, prediction, average=None))
    print('')
    print("召回率: ", recall_score(test_y, prediction, average='macro'))
    print("召回率: ", recall_score(test_y, prediction, average='micro'))
    print("召回率: ", recall_score(test_y, prediction, average='weighted'))
    print("召回率: ", recall_score(test_y, prediction, average=None))
    print('')
    print('')

data = pd.read_csv("bbc-text.csv", encoding='utf-8')

def stopwordslist():
    stopwords = [line.strip() for line in open('cn_stopwords.txt', encoding='utf-8').readlines()]
    return stopwords


def seg_depart(sentence):
    sentence_depart = jieba.cut(sentence.strip())
    stopwords = stopwordslist()
    outstr = ''
    for word in sentence_depart:
        if word not in stopwords:
            outstr += word
    return outstr

train, test = train_test_split(data, test_size=0.3)
train_X = train['text']
train_y = train['category']
test_X = test['text']
test_y = test['category']
tfidf_transformer = TfidfVectorizer()
tf_train_data = tfidf_transformer.fit_transform(train_X)
tf_train_data1 = tfidf_transformer.transform(test_X)

clf = SVC(kernel='linear').fit(tf_train_data, train_y)
clf1 = SVC(kernel='rbf').fit(tf_train_data, train_y)
clf2 = SVC(kernel='poly').fit(tf_train_data, train_y)
clf3 = LinearSVC(C=1.0).fit(tf_train_data, train_y)


prediction = clf.predict(tf_train_data1)
prediction1 = clf1.predict(tf_train_data1)
prediction2 = clf2.predict(tf_train_data1)
prediction3 = clf3.predict(tf_train_data1)

show(prediction, 'Linear')
show(prediction, 'rbf')
show(prediction, 'poly')
show(prediction, 'LinearSVC')