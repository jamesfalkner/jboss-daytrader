#!/bin/bash

BASEDIR=$(dirname "$0")/..
oc login -u developer -p developer
oc project daytrader
cd $BASEDIR/rest-microservice
#
# The first time, you must:
# mvn clean package fabric8:resource fabric8:build fabric8:deploy
#
mvn fabric8:deploy





