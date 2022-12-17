#!/bin/bash

# flush-log-* settings make biggest DDL test perf difference
docker run --rm --publish 3306:3306 --name mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:8.0.31 \
  --innodb-flush-log-at-trx-commit=0 \
  --innodb-flush-log-at-timeout=30 \
  --innodb-stats-persistent=OFF \
  --default-time-zone=+8:00;
