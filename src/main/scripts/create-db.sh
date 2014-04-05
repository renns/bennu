#!/bin/bash

SCRIPT_PATH=`realpath $0`
cd `dirname $SCRIPT_PATH`/..

CLASSPATH=`find ./lib/ -name "*.jar" -exec echo -n {}: \;`
sudo -u fabio java \
    -cp $CLASSPATH \
    com.qoid.bennu.schema.CreateDatabase
