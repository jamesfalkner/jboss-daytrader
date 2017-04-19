#!/bin/bash
cat order.json
echo curl -H 'Content-Type: application/json' -X POST -d @order.json http://ejb-rest-daytrader.apps.shadowman.com/api/daytrader

curl -H 'Content-Type: application/json' -X POST -d @order.json http://ejb-rest-daytrader.apps.shadowman.com/api/daytrader
