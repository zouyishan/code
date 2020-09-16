clear

#cd to the home directory of enietoolkit
cd /m3/KBP/software/EnglishIE/ENIE
CLASSPATH=bin:lib/nametagging.jar:lib/stanford-postagger.jar:lib/colt-nohep.jar:lib/dbparser.jar:lib/dom4j-1.6.1.jar:lib/javelin.jar:lib/jaxen-1.1.1.jar:lib/joda-time-1.6.jar:lib/jyaml-1.3.jar:lib/log4j.jar:lib/mallet.jar:lib/mallet_old.jar:lib/opennlp-maxent-3.0.1-incubating.jar:lib/opennlp-tools-1.5.1-incubating.jar:lib/pnuts.jar:lib/RadixTree-0.3.jar:lib/stanford-parser.jar:lib/lucene-core-3.0.2.jar:lib/weka.jar:lib/trove.jar

java -cp $CLASSPATH -Xmx8g -Xms8g -server -DjetHome=./ cuny.blender.englishie.ace.IETagger /m3/KBP/software/EnglishIE/ENIE/props/enie.property /m1/Data_2011/BOLT/LDC2012E04_phase1_discussion_forums_source_data_r1/data/eng/filelist /m1/Data_2011/BOLT/LDC2012E04_phase1_discussion_forums_source_data_r1/data/eng/xml_text/ /m1/Data_2011/BOLT/LDC2012E04_phase1_discussion_forums_source_data_r1/data/eng/IE_output/
