import read_data
from keras import layers, models
from keras import backend as K
from capsulelayers import CapsuleLayer, PrimaryCap, Length, Mask
from keras.layers.core import Lambda
from keras import callbacks
import numpy as np
import argparse
import os
from gensim.models import Word2Vec
from keras.preprocessing import sequence

import keras
from keras.callbacks import TensorBoard, ModelCheckpoint

max_features = 3541
maxlen = 128
embed_dim = 100
recon_dim = 400
resultDir = './mine/result_test03/'
# wv_mn = 'word2vec_model_mine_50d'
wv_mn = 'word2vec_model_mine'


def create_embedding():
    model = Word2Vec.load(wv_mn)
    num_words = len(model.wv.index2word)
    embedding_matrix = np.zeros((num_words, embed_dim))
    for i in range(num_words):
        embedding_vector = model.wv.__getitem__(model.wv.index2word[i])
        embedding_matrix[i] = embedding_vector
    return embedding_matrix, num_words


def CapsNet(input_shape, n_class, num_routing):
    x = layers.Input(shape=(maxlen,))
    # embedding_layer = Word2Vec.load(wv_mn).wv.get_keras_embedding()
    # embedding_layer.trainable = False
    # embedding_layer.mask_zero = True
    embed = layers.Embedding(max_features, embed_dim, input_length=maxlen)(x)
    # embed = embedding_layer(x)

    # lstm = layers.LSTM(128, name='lstm', return_sequences=True)(embed)
    lstm = layers.LSTM(128, name='lstm', return_sequences=True)(embed)
    # lstm_pad = layers.convolutional.ZeroPadding2D(padding=(128, 128))(lstm)
    # lstm_reshape = layers.Reshape((128, 4), name='lstm_reshape')(lstm)

    embed_out = layers.Reshape((-1, 1))(embed)
    embed_conv = layers.Conv1D(filters=1, kernel_size=32, strides=32, padding='valid',
                                   activation='relu')(embed_out)

    # mask_embed = layers.Masking(mask_value=0)(x)
    embed_conv_out = layers.Flatten()(embed_conv)
    # embed_conv_out = Lambda(lambda x: (x + 1) / 2.6)(embed_conv_out)

    # conv1 = layers.Conv1D(filters=256, kernel_size=9, strides=1, padding='valid',
    #                       activation='relu', name='conv1')(embed)

    # Layer 2: Conv2D layer with `squash` activation, then reshape to [None, num_capsule, dim_vector]
    # 第2层：使用“squash”激活的Conv2D层，然后重塑为[None，num_capsule，dim_vector]
    primarycaps = PrimaryCap(lstm, dim_vector=8, n_channels=32, kernel_size=9,
                             strides=2, padding='valid')

    # Layer 3: Capsule layer. Routing algorithm works here.
    # 第3层：胶囊层。 路由算法在这里起作用。
    digitcaps = CapsuleLayer(num_capsule=n_class, dim_vector=8, num_routing=num_routing,
                             name='digitcaps')(primarycaps)

    # Layer 4: This is an auxiliary layer to replace each capsule with its length. Just to match the true label's shape.
    # If using tensorflow, this will not be necessary. :)
    # 第4层：这是一个辅助层，用于替换每个胶囊的长度。 只是为了匹配真实标签的形状。
    # 如果使用tensorflow，则没有必要。  :)
    out_caps = Length(name='out_caps')(digitcaps)

    # Decoder network.  解码器网络。
    y = layers.Input(shape=(n_class,))
    masked = Mask()([digitcaps, y])  # The true label is used to mask the output of capsule layer.
    # true标签用于掩盖胶囊层的输出。
    x_recon = layers.Dense(64, activation='relu')(masked)
    x_recon = layers.Dense(512, activation='relu')(x_recon)
    x_recon = layers.Dense(recon_dim, activation='sigmoid')(x_recon)
    # x_recon = Lambda(lambda x: x*2-1)(x_recon)
    sub = layers.Subtract()([x_recon, embed_conv_out])

    # two-input-two-output keras Model
    return models.Model([x, y], [out_caps, sub])


def margin_loss(y_true, y_pred):
    """
    Margin loss for Eq.(4). When y_true[i, :] contains not just one `1`, this loss should work too. Not test it.
    :param y_true: [None, n_classes]
    :param y_pred: [None, num_capsule]
    :return: a scalar loss value.
    """
    L = y_true * K.square(K.maximum(0., 0.9 - y_pred)) + \
        0.5 * (1 - y_true) * K.square(K.maximum(0., y_pred - 0.1))

    return K.mean(K.sum(L, 1))


def train(model, data, args):
    # unpacking the data  解压缩数据
    (x_train, y_train), (x_test, y_test) = data
    sub_label = np.array([[0 for i in range(recon_dim)] for t in x_train])
    sub_label_t = np.array([[0 for i in range(recon_dim)] for t in y_test])
    # callbacks  回调
    # log = callbacks.CSVLogger(args.save_dir + '/log.csv')
    # tb = callbacks.TensorBoard(log_dir=args.save_dir + '/tensorboard-logs',
    #                            batch_size=args.batch_size, histogram_freq=args.debug)
    # checkpoint = callbacks.ModelCheckpoint(args.save_dir + '/weights-{epoch:02d}.h5',
    #                                        save_best_only=True, save_weights_only=True, verbose=1)
    # lr_decay = callbacks.LearningRateScheduler(schedule=lambda epoch: 0.001 * np.exp(-epoch / 10.))

    adam = keras.optimizers.Adam(lr=0.001)
    model_checkpoint = ModelCheckpoint(
        filepath=args.save_dir + '/trained_model_cp.h5',  # 保存模型的路径
        monitor='val_loss',  # 被监测的数据
        verbose=1,  # 详细信息模式，0 或者 1
        save_best_only=True)  # 保存最佳模型
    tensorboard = TensorBoard(log_dir=args.save_dir + '/logs', histogram_freq=0, write_graph=True,
                              write_images=True)
    # 保存日志，用于绘制准确率、损失图像

    learning_rate_reduction = keras.callbacks.ReduceLROnPlateau(monitor='val_loss',
                                                                factor=0.5, patience=2, verbose=1)
    # 2个epochs里validation loss不变，learning rate*0.5

    # compile the model
    model.compile(optimizer=adam,
                  loss=[margin_loss, 'mse'],
                  loss_weights=[1., args.lam_recon],
                  metrics={'out_caps': 'accuracy'})

    # model.fit([x_train, y_train], [y_train, x_train], batch_size=args.batch_size,
    #           epochs=args.epochs, validation_data=[[x_test, y_test], [y_test, x_test]],
    #           callbacks=[log, tb, checkpoint], verbose=1)
    model.fit([x_train, y_train], [y_train, sub_label],
              validation_data=[[x_test, y_test], [y_test, sub_label_t]],
              batch_size=args.batch_size, epochs=args.epochs, verbose=1,
              # callbacks=[log, tb, checkpoint]
              callbacks=[model_checkpoint, tensorboard, learning_rate_reduction])
              # callbacks = [model_checkpoint, tensorboard])

    model.save_weights(args.save_dir + '/trained_model.h5')
    # model.save(args.save_dir + '/trained_model.h5')
    print('Trained model saved to \'%s/trained_model.h5\'' % args.save_dir)

    return model


def get_model(isTrain=True):
    parser = argparse.ArgumentParser()
    parser.add_argument('--batch_size', default=36, type=int)
    parser.add_argument('--epochs', default=20, type=int)
    parser.add_argument('--lam_recon', default=0.5, type=float)
    parser.add_argument('--num_routing', default=3, type=int)  # num_routing should > 0
    parser.add_argument('--shift_fraction', default=0.1, type=float)
    parser.add_argument('--debug', default=0, type=int)  # debug>0 will save weights by TensorBoard
    parser.add_argument('--save_dir', default=resultDir)
    # parser.add_argument('--is_training', default=1, type=int)
    parser.add_argument('--weights', default=None)
    args = parser.parse_args()
    print(args)

    if not os.path.exists(args.save_dir):
        os.makedirs(args.save_dir)

    x, y = read_data.load_pickle_data(read_data.mine_pkl_path)
    y = np.eye(8)[y]

    # x_train = x[:324]
    # y_train = y[:324]
    # x_v = x[324:360]
    # y_v = y[324:360]
    x_train = x[:360]
    y_train = y[:360]
    x_v = x[360:]
    y_v = y[360:]
    print(x_train.shape)
    print(y_train.shape)

    model = CapsNet(input_shape=x_train.shape,
                    n_class=8,
                    num_routing=args.num_routing)
    model.summary()

    if isTrain:
        train(model=model, data=((x_train, y_train), (x_v, y_v)), args=args)
    else:
        print('loading model weights...')
        model.load_weights(args.save_dir + 'trained_model.h5')

    return model


def test_model(model, test_x, test_y):
    print('predicting...')
    y_pre, x_recon = model.predict([test_x, np.eye(8)[test_y]])
    y_pre = y_pre.tolist()
    y_pre = [l.index(max(l)) for l in y_pre]

    cats = [i for i in range(len(read_data.mine_labels))]
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
        if tp + fp == 0:
            precision = 0
        else:
            precision = tp / (tp + fp)
        if tp + fn == 0:
            recall = 0
        else:
           recall = tp / (tp + fn)
        if recall + precision == 0:
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
        print(cats[i], p[i], r[i], f1[i])
    print('marco', np.mean(p), np.mean(r), np.mean(f1))
    p_micro = tp_sum / (tp_sum + fp_sum)
    r_micro = tp_sum / (tp_sum + fn_sum)
    f1_micro = 2 * p_micro * r_micro / (p_micro + r_micro)
    print('micro', p_micro, r_micro, f1_micro)


if __name__ == '__main__':
    model = get_model()
    # model = get_model(isTrain=False)
    x, y = read_data.load_pickle_data(read_data.mine_pkl_path)

    test_x = x[360:]
    test_y = y[360:]
    test_model(model, x[:360], y[:360])
    test_model(model, test_x, test_y)

    # embedding_1_layer_model = models.Model(inputs=model.input, outputs=model.get_layer('embedding_1').output)
    # embed_layer_model = models.Model(inputs=model.input, outputs=model.get_layer('flatten_1').output)
    # dense_layer_model = models.Model(inputs=model.input, outputs=model.get_layer('dense_3').output)
    # subtract_layer_model = models.Model(inputs=model.input, outputs=model.get_layer('subtract_1').output)
    # test_x = x[0]
    # test_y = np.eye(8)[y[0]]
    # p = embedding_1_layer_model.predict([[test_x], [test_y]])
    # print(p[0][127])
    # p = embed_layer_model.predict([[test_x], [test_y]])
    # print(p[0][-100:])
    # p = dense_layer_model.predict([[test_x], [test_y]])
    # print(p[0][-100:])
    # p = subtract_layer_model.predict([[test_x], [test_y]])
    # print(p[0][-100:])
    # #
    # # test_x = x[360]
    # # test_y = np.eye(8)[y[360]]
    # # p = embed_layer_model.predict([[test_x], [test_y]])
    # # print(p[0][-100:])
    # # p = dense_layer_model.predict([[test_x], [test_y]])
    # # print(p[0][-100:])
