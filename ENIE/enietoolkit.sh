
#load environment variables from component.env
. component.env

#cd to the home directory
cd $ENIE_HOMEDIR

#build the toolkit from source and generate a jar file
echo "========================================================"
echo "building the toolkit from source and generate a jar file"
echo "========================================================"
ant 

#run a sample test
echo "========================================================"
echo "running a sample test"
echo "========================================================"
CLASSPATH=bin:lib/nametagging.jar:lib/stanford-postagger.jar:lib/colt-nohep.jar:lib/dbparser.jar:lib/dom4j-1.6.1.jar:lib/javelin.jar:lib/jaxen-1.1.1.jar:lib/joda-time-1.6.jar:lib/jyaml-1.3.jar:lib/log4j.jar:lib/mallet.jar:lib/mallet_old.jar:lib/opennlp-maxent-3.0.1-incubating.jar:lib/opennlp-tools-1.5.1-incubating.jar:lib/pnuts.jar:lib/RadixTree-0.3.jar:lib/stanford-parser.jar:lib/lucene-core-3.0.2.jar:lib/weka.jar:lib/trove.jar:lib/indri.lib

java -cp $CLASSPATH -Xmx3g -Xms3g -server -DjetHome=./ cuny.blender.englishie.ace.IETagger /m3/KBP/software/EnglishIE/ENIE/props/enie.property /m3/KBP/software/EnglishIE/ENIE/example/filelist /m3/KBP/software/EnglishIE/ENIE/example/sgm /m3/KBP/software/EnglishIE/ENIE/example/out


#validate the output files
echo "========================================================"
echo "validating the output files"
echo "========================================================"

flagfile="./success"
outdir="./example/out/ace05/*"
validate=1

if [ ! -e $flagfile ];then
rm -rf $flagfile
fi

for f in $outdir
do
  result=$(diff -u $f ${f/ace05/ace05_validate})
  if [ $? -eq 0 ]; then 
	 echo "Validating $f: passed!"
  else 
    echo "Validating $f: failed!"
    validate=0
  fi
done

if [ $validate -eq 1 ]
then
  echo "Sample test: passed!"
  touch $flagfile
else 
  echo "Sample test: failed!"
fi
