#!/bin/bash

{ [ $(id -u) -eq 0 ]; } || { echo "execute as root"; exit; }

$(dirname "$0")/setup.sh

SECONDS=0

until mysql -u root -h 127.0.0.1 -pmy-secret-pw;
do
  ((SECONDS < 120)) || exit;
  sleep 1;
done
