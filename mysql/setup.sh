#!/bin/bash

{ [ $(id -u) -eq 0 ]; } || { echo "execute as root"; exit; }

docker run --rm --publish 3306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:8 \
  --innodb-flush-log-at-trx-commit=0 \ # flush-log* settings seem to make the biggest perf difference
  --innodb-flush-log-at-timeout=30 \
  --innodb-stats-persistent=OFF;

SECONDS=0

until mysql -u root -h 127.0.0.1 -pmy-secret-pw;
do
  ((SECONDS < 120)) || exit;
  sleep 1;
done
