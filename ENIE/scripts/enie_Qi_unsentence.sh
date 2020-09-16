clear

# cd to the home directory of enietoolkit
cd /Users/qli/PhD_Data/software/ENIE/
CLASSPATH=bin:lib/nametagging.jar:lib/stanford-postagger.jar:lib/colt-nohep.jar:lib/dbparser.jar:lib/dom4j-1.6.1.jar:lib/javelin.jar:lib/jaxen-1.1.1.jar:lib/joda-time-1.6.jar:lib/jyaml-1.3.jar:lib/log4j.jar:lib/mallet.jar:lib/mallet_old.jar:lib/opennlp-maxent-3.0.1-incubating.jar:lib/opennlp-tools-1.5.1-incubating.jar:lib/pnuts.jar:lib/RadixTree-0.3.jar:lib/stanford-parser.jar:lib/lucene-core-3.0.2.jar:lib/weka.jar:lib/trove.jar


# argument 1: file list
# arguemnt 2: input dir
# argument 3: output dir
java -cp $CLASSPATH -Xmx3g -Xms3g -server -DjetHome=./ cuny.blender.englishie.ace.IETagger ./props/enie_unsentence.property $1 $2 $3
