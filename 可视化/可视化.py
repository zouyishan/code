import jieba
from wordcloud import WordCloud
import pandas as pd

# 弄词云的各种配置
w = WordCloud(width=1920,
              height=1312,
              background_color='black',
              font_path="simsun.ttf",
              random_state=30)

# 加载停用词
def stopwordslist():
    stopwords = [line.strip() for line in open('cn_stopwords.txt', encoding='utf-8').readlines()]
    return stopwords

# 对句子进行去停用词
def seg_depart(sentence):
    sentence_depart = jieba.cut(sentence.strip())
    stopwords = stopwordslist()
    outstr = ''
    for word in sentence_depart:
        if word not in stopwords:
            outstr += word
    return outstr

txt = ''
data = pd.read_csv("煤矿数据集.csv", encoding='utf-8')
text = data['text']  # 读取text这一列(主要原因)的数据
for line in text:
    seg_list = seg_depart(line)
    txt += seg_list



txtlist = jieba.lcut(txt)
string = " ".join(txtlist)
w.generate(string)

w.to_file('可视化.png')