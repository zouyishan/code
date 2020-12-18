import re
import pandas
from nltk.corpus import stopwords
import pickle
from keras.preprocessing import sequence
import numpy as np

stopwords_eng = stopwords.words('english')
vocab_bbc_path = 'vocab_bbc.pkl'
vocab_bbc_8000_path = 'vocab_bbc_8000.pkl'
bbc_pkl_path = 'bbc_8000_pkl.pkl'
bbc_labels = ['tech', 'business', 'sport', 'entertainment', 'politics']

vocab_mine_path = 'vocab_mine_8000.pkl'
mine_pkl_path = 'mine_8000.pkl'
mine_labels = ['瓦斯事故', '顶板事故', '运输事故', '工伤事故',
          '机电事故', '透水事故', '火灾事故', '爆炸事故']

medical_pkl_path = 'medical_minc5.pkl'
m_labels = ['Arthroskopie', 'Bundesgesundheitsblatt', 'DerAnaesthesist', 'DerChirurg', 'DerGynaekologe', 'DerHautarzt',
          'DerInternist', 'DerNervenarzt', 'DerOpthalmologe', 'DerOrthopaede', 'DerPathologe', 'DerRadiologe',
          'DerSchmerz', 'DerUnfallchirurg', 'DerUrologeA', 'EthikInDerMedizin', 'ForumDerPsychoanalyse',
          'Gefaesschirurgie', 'Herz', 'Herzschrittmachertherapie', 'HNO', 'IntensiveMedizin',
          'KlinischeNeuroradiologie', 'ManuelleMedizin', 'MedizinischeKlinik', 'MonatsschriftKinderheilkunde',
          'MundKieferGesichtschirurgie', 'Notfall+Rettungsmedizin', 'OperativeOrthopaedie', 'PerinatalMedizin',
          'Psychotherapeut', 'Rechtsmedizin', 'Reproduktionsmedizin', 'Strahlentherapie+Onkologie',
          'Trauma+Berufskrankheit', 'ZfuerGerontologie+Geriatrie', 'ZfuerHerzThoraxGefaesschirurgie',
          'ZfuerKardiologie', 'ZfuerRheumatologie']

# maxLen = 2229
maxLen = 2230
mine_maxLen = 128



def sentences2words(s):
    s = re.sub(r'[^a-z0-9 \'\-]', ' ', s.lower())
    s = re.sub(' +', ' ', s)
    s.strip(' ')
    words = s.split(' ')
    words = [w for w in words if w not in stopwords_eng]
    words = [w for w in words if w != '']
    return words


def read_bbs_news2wl_label():
    path = 'bbc-text.csv'
    df = pandas.read_csv(path)
    x = df['text'].tolist()
    y = df['category'].tolist()
    x = [sentences2words(s) for s in x]
    return x, y


def wordlists2vocab(wordlists, fn):
    vocab = []
    cn = []
    for wl in wordlists:
        for w in wl:
            if w not in vocab:
                vocab.append(w)
                cn.append(0)
            cn[vocab.index(w)] += 1

    z = list(zip(vocab, cn))
    z.sort(key=lambda t: t[1], reverse=True)
    # z = [t for t in z if t[1] >= 5]
    z = [t for t in z if t[1] > 3]
    # if len(z) > 7999:
    #   z = z[:7999]
    vocab = [t[0] for t in z]
    vocab.insert(0, '__')

    with open(fn + '.pkl', 'wb') as f:
        pickle.dump(vocab, f)
        f.close()
    with open(fn + '.txt', 'w') as f:
        for t in z:
            f.write(t[0] + '\t' + str(t[1]) + '\n')
        f.close()


def load_vocab(path):
    with open(path, 'rb') as f:
        vocab = pickle.load(f)
        f.close()
    return vocab


def process_bbc(x, y, vocab):
    x = [[vocab.index(w) for w in wl if w in vocab] for wl in x]
    y = [bbc_labels.index(l) for l in y]
    x = np.array(x)
    y = np.array(y)
    x = sequence.pad_sequences(x, maxlen=maxLen)
    return x, y


def load_pickle_data(path):
    with open(path, 'rb') as f:
        x, y = pickle.load(f)
        f.close()

    return x, y


def process_mine():
    with open('mine1.pkl', 'rb') as f:
        train_x, train_y, test_x, test_y = pickle.load(f)
        f.close()

    x = train_x + test_x
    y = train_y + test_y
    x = [s.split(' ') for s in x]
    x = [[w for w in s if w != ''] for s in x]
    y = np.array([mine_labels.index(l) for l in y])
    vocab = load_vocab('vocab_mine_8000.pkl')
    x = np.array([[vocab.index(w)for w in s if w in vocab]for s in x])
    x = sequence.pad_sequences(x, maxlen=128)
    with open('mine_8000.pkl', 'wb') as f:
        pickle.dump([x, y], f)
        f.close()
    return x, y


def process_mdedical_20():
    ul = ['Gefaesschirurgie', 'Rechtsmedizin', 'Arthroskopie', 'ZfuerHerzThoraxGefaesschirurgie',
          'Herzschrittmachertherapie', 'Bundesgesundheitsblatt', 'Reproduktionsmedizin', 'Herz', 'ManuelleMedizin',
          'ForumDerPsychoanalyse', 'Notfall+Rettungsmedizin', 'MedizinischeKlinik', 'Strahlentherapie+Onkologie',
          'Trauma+Berufskrankheit', 'PerinatalMedizin', 'EthikInDerMedizin', 'OperativeOrthopaedie',
          'KlinischeNeuroradiologie', 'DerInternist']
    nl = ['DerHautarzt', 'DerChirurg', 'DerAnaesthesist', 'DerNervenarzt', 'DerRadiologe', 'ZfuerKardiologie',
          'DerUnfallchirurg', 'DerOpthalmologe', 'DerOrthopaede', 'MonatsschriftKinderheilkunde', 'HNO', 'DerPathologe',
          'IntensiveMedizin', 'MundKieferGesichtschirurgie', 'DerUrologeA', 'ZfuerRheumatologie', 'DerSchmerz',
          'ZfuerGerontologie+Geriatrie', 'DerGynaekologe', 'Psychotherapeut']
    x, y = load_pickle_data(medical_pkl_path)
    x = x.tolist()
    y = y.tolist()
    ul = [m_labels.index(l) for l in ul]
    nl = [m_labels.index(l) for l in nl]
    for i in range(len(x) - 1, -1, -1):
        if y[i] in ul:
            y.pop(i)
            x.pop(i)
    x = np.array(x)
    y = [nl.index(l) for l in y]
    y = np.array(y)
    with open('medical_20_minc5.pkl', 'wb') as f:
        pickle.dump([x, y], f)
        f.close()
    return x, y


def process_medical():
    vocab = load_vocab('vocab_medical.pkl')
    with open('medical_wordlist_label.pkl', 'rb') as f:
        train_x, train_y, test_x, test_y = pickle.load(f)
        f.close()
    x = train_x + test_x
    y = train_y + test_y
    y = np.array([m_labels.index(l) for l in y])
    x = [[vocab.index(w) for w in s if w in vocab] for s in x]
    x = sequence.pad_sequences(x, maxlen=720)
    with open(medical_pkl_path, 'wb') as f:
        pickle.dump([x, y], f)
        f.close()
    return x, y


if __name__ == '__main__':
    # x, y = read_bbs_news2wl_label()
    # wordlists2vocab(x)

    # with open('mine1.pkl', 'rb') as f:
    #     train_x, train_y, test_x, test_y = pickle.load(f)
    #     f.close()
    # train_x = [s.split(' ') for s in train_x]
    # test_x = [s.split(' ') for s in test_x]
    # train_x = [[w for w in s if w != ''] for s in train_x]
    # test_x = [[w for w in s if w != ''] for s in test_x]
    # print(train_x[0])
    # l = [len(s) for s in train_x] + [len(s) for s in test_x]
    # l.sort(reverse=True)
    # print(l[:20])
    # print(np.average(l))
    # process_mine()
    # x, y = load_pickle_data(mine_pkl_path)
    # print(len(x))
    # print(y[360:])

    # wordlists2vocab(train_x + test_x, 'vocab_mine_8000')

    # vocab = load_vocab('vocab_mine_8000.pkl')
    # print(vocab[:10])


    # vocab = load_vocab(vocab_bbc_8000_path)
    # x, y = read_bbs_news2wl_label()
    # x, y = process_bbc(x, y, vocab)
    # with open(bbc_pkl_path, 'wb') as f:
    #     pickle.dump([x, y], f)
    #     f.close()
    # x, y = load_pickle_data(bbc_pkl_path)
    # print(len(x))
    # -445
    # 1602 1780

    # with open('medical_wordlist_label.pkl', 'rb') as f:
    #     train_x, train_y, test_x, test_y = pickle.load(f)
    #     f.close()
    # x = train_x + train_y
    # l = [len(s) for s in x]
    # print(max(l))
    # x, y = process_medical()
    # print(x[0])
    # print(y[:10])
    # x, y = load_pickle_data(medical_pkl_path)
    # print(y[6274])
    # print(y[76])
    # x, y = process_mdedical_20()
    x, y = load_pickle_data('medical_20_minc5.pkl')
    print(len(x))
    print(y[0])
    for i in range(1000, len(x)):
        if y[i] == 2:
            print(i)
            break
    # 6821 5467
