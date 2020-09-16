#!/bin/sh
# train a model from given source training data
# usage: ./train.sh source_data file_list training_data_inter model_name 

export Jet_HOME=../

export CLASSPATH=bin:$Jet_HOME/lib/nametagging.jar:$Jet_HOME/lib/stanford-postagger.jar:$Jet_HOME/lib/mallet.jar:$Jet_HOME/lib/mallet-deps.jar:$Jet_HOME/lib/opennlp-maxent-3.0.1-incubating.jar:$Jet_HOME/lib/opennlp-tools-1.5.1-incubating.jar:$Jet_HOME/lib/RadixTree-0.3.jar:$Jet_HOME/lib/stanford-parser.jar

echo "preparing training data"

java -Xmn700M -Xms3000M -Xmx3000M -DNER_HOME=$Jet_HOME/modules/ner cuny.blender.nametagging.TrainingDataPreparer $1 $2 $3 > log_prepare

echo "training"
java -Xmn700M -Xms3000M -Xmx3000M -DNER_HOME=$Jet_HOME/modules/ner cuny.blender.nametagging.CRFTrainer $4 $3 > log_training

