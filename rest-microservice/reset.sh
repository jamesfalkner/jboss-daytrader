#!/bin/bash

BASEDIR=$(dirname "$0")/..
cd $BASEDIR
git reset --hard

oc login -u developer -p developer
oc project daytrader
cd $BASEDIR/rest-microservice
mvn fabric8:undeploy





