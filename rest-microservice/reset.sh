#!/bin/bash

THIS=$(readlink -f "$0")
BASEDIR=$(dirname $THIS)/..

cd $BASEDIR
git reset --hard

oc login -u developer -p developer
oc project daytrader
cd $BASEDIR/rest-microservice
mvn fabric8:undeploy





