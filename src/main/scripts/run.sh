#!/bin/sh

cd /ssd2/bennu/

CLASSPATH=`find ./lib -name "*.jar" -exec echo -n {}: \;`.

/usr/lib/jvm/jre-1.7.0-openjdk.x86_64/bin/jbennu \
	-Dports=http:8080 \
	-Xmx1024m \
	-cp $CLASSPATH \
	-Dlog.housekeptfile \
	com.qoid.bennu.RunQoidServer
	
