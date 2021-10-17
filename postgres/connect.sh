#!/bin/bash

{ [ $(id -u) -eq 0 ]; } || { echo "execute as root"; exit; }

$(dirname "$0")/setup.sh

SECONDS=0

until psql postgresql://postgres:mysecretpassword@127.0.0.1:5432;
do
  ((SECONDS < 120)) || exit;
  sleep 1;
done
