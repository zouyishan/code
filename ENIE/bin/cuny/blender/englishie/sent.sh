#!/bin/sh
cd /m3/KBP/software/EnglishIE/ENIE/

export CLASSPATH=bin:lib/nametagging.jar:lib/stanford-postagger.jar:lib/colt-nohep.jar:lib/dbparser.jar:lib/dom4j-1.6.1.jar:lib/javelin.jar:lib/jaxen-1.1.1.jar:lib/joda-time-1.6.jar:lib/jyaml-1.3.jar:lib/log4j.jar:lib/mallet.jar:lib/mallet_old.jar:lib/opennlp-maxent-3.0.1-incubating.jar:lib/opennlp-tools-1.5.1-incubating.jar:lib/pnuts.jar:lib/RadixTree-0.3.jar:lib/stanford-parser.jar:lib/lucene-core-3.0.2.jar:lib/weka.jar:lib/trove.jar:lib/collections-generic-4.01.jar:lib/indri.jar:lib/jung2-2.0.jar:lib/lingpipe-4.1.0.jar:lib/sspace-1.0.jar

java -Xms2g -Xmx2g -cp $CLASSPATH cuny.blender.englishie.nlp.zoner.SentenceWriter /m1/Jet/example/sgmlist /m1/Jet/example/sgm/egypt/ /m1/Jet/example/sentout/ XML