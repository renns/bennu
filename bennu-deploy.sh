#!/bin/bash

# Prerequisites:
# - Get latest code of model3
# - Do a clean install of model3
# - Get latest code of agentui
# - Get latest code of bennu

SERVER=$1

echo "deploying to $SERVER"

mvn -T 4 package -DskipTests

mkdir target/dist
rm -rf target/dist/*
rm -rf target/dependency/*

mkdir target/dist/lib
mvn dependency:copy-dependencies
cp -r target/*.jar target/dist/lib/
cp -r target/dependency/*.jar target/dist/lib/
cp -RH src/main/webapp target/dist

mkdir target/dist/bin
cp schema/* target/dist/bin
cp src/main/scripts/* target/dist/bin

ssh fabio@dev.qoid.com "sudo stop $SERVER"

rsync \
 	--exclude=config.json \
 	--exclude=.logs \
 	--exclude=db \
 	--delete \
 	--compress \
 	--copy-links \
 	--recursive \
 	--partial \
 	--progress \
 	target/dist/ \
 	fabio@dev.qoid.com:/opt/$SERVER/

ssh fabio@dev.qoid.com "sudo chmod g+rw -R /opt/$SERVER/ ; sudo chown -R fabio:bennu /opt/$SERVER/ ; sudo chmod u+rw -R /opt/$SERVER/ "

ssh fabio@dev.qoid.com "sudo start $SERVER"
