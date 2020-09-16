#!/bin/bash
IE_HOME="$( cd "$( dirname "$0" )" && pwd )"
java -cp "${IE_HOME}/bin:${IE_HOME}/lib/nametagging.jar:${IE_HOME}/lib/stanford-postagger.jar:${IE_HOME}/lib/colt-nohep.jar:${IE_HOME}/lib/dbparser.jar:${IE_HOME}/lib/dom4j-1.6.1.jar:${IE_HOME}/lib/javelin.jar:${IE_HOME}/lib/jaxen-1.1.1.jar:${IE_HOME}/lib/joda-time-1.6.jar:${IE_HOME}/lib/jyaml-1.3.jar:${IE_HOME}/lib/log4j.jar:${IE_HOME}/lib/mallet.jar:${IE_HOME}/lib/mallet_old.jar:${IE_HOME}/lib/opennlp-maxent-3.0.1-incubating.jar:${IE_HOME}/lib/opennlp-tools-1.5.1-incubating.jar:${IE_HOME}/lib/pnuts.jar:${IE_HOME}/lib/RadixTree-0.3.jar:${IE_HOME}/lib/stanford-parser.jar:${IE_HOME}/lib/lucene-core-3.0.2.jar:${IE_HOME}/lib/weka.jar:${IE_HOME}/lib/trove.jar:${IE_HOME}/lib/indri.lib" -Xmx3g -Xms3g -server -DjetHome=${IE_HOME}/ cuny.blender.englishie.ace.IETagger $@

# ./props/enie.property ./example/filelist ./example/sgm ./example/out
