#!/usr/local/bin/fish


set SERVER cloudbi-db


mvn -T 4 install 


mkdir target/dist
rm -rf target/dist/*
rm -rf target/dependency/*


mkdir target/dist/lib
mvn dependency:copy-dependencies
cp -r target/*.jar target/dist/lib/
cp -r target/dependency/*.jar target/dist/lib/
cp -RH src/main/webapp target/dist


rsync \
 	--exclude=config \
 	--exclude=tmp \
 	--exclude=logs \
 	--exclude=.logs \
 	--exclude=db \
 	--delete \
 	--compress \
 	--copy-links \
 	--recursive \
 	--partial \
 	--progress \
 	target/dist/ \
 	$SERVER:/opt/bennu/

