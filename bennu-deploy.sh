#!/bin/bash

# Prerequisites:
# - Get latest code of model3
# - Do a clean install of model3
# - Get latest code of agentui
# - Get latest code of bennu

mvn -T 4 package

mkdir target/dist
rm -rf target/dist/*
rm -rf target/dependency/*

mkdir target/dist/lib
mvn dependency:copy-dependencies
cp -r target/*.jar target/dist/lib/
cp -r target/dependency/*.jar target/dist/lib/
cp -RH src/main/webapp target/dist

ssh fabio@dev.qoid.com 'sudo stop $1'

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
 	fabio@dev.qoid.com:/opt/$1/

ssh fabio@dev.qoid.com 'chmod g+w -R /opt/$1/ ; chown -R fabio:bennu /opt/$1/'

ssh fabio@dev.qoid.com 'sudo start $1'
