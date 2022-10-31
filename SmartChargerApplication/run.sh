#!/bin/bash

CLASSPATH=.:classes:/opt/pi4j/lib/'*';

for i in `ls $PWD/lib/*.jar`
do
CLASSPATH+=:$i;
done

#echo $CLASSPATH

cd bin

java -Djava.library.path=/usr/lib/jni -classpath $CLASSPATH Main