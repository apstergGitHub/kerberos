#!/bin/bash

docker network create hope_network
docker run --name kerberos-mongo --network=hope_network -d mongo