#!/bin/bash

{ [ $(id -u) -eq 0 ]; } || { echo "execute as root"; exit; }

docker run --rm --publish 5432:5432 --name postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres:13

SECONDS=0

until psql postgresql://postgres:mysecretpassword@127.0.0.1:5432;
do
  ((SECONDS < 120)) || exit;
  sleep 1;
done
