#!/bin/sh

export IAM_MODE=DEV
#export IAM_MODE=TEST

A=UserAdminWebApp
V=LATEST
JARFILE=$A-$V.jar

pkill -f $A

wget  $JARFILE "http://mvnrepo.cantara.no/service/local/artifact/maven/content?r=snapshots&g=net.whydah.identity&a=$A&v=$V&p=jar"
nohup java -jar -DIAM_CONFIG=useradminwebapp.TEST.properties $JARFILE &

tail -f nohup.out

