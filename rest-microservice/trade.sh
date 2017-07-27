#!/bin/bash
cat order.json
HOST=$(oc get route ejb-rest -o jsonpath='{.spec.host}')
echo curl -H 'Content-Type: application/json' -X POST -d @order.json http://${HOST}/api/daytrader

curl -H 'Content-Type: application/json' -X POST -d @order.json http://${HOST}/api/daytrader
