#!/bin/bash

{ [ $(id -u) -eq 0 ]; } || { echo "execute as root"; exit; }

docker run --rm --publish 5432:5432 --name postgres -e POSTGRES_PASSWORD=mysecretpassword -d postgres:13
