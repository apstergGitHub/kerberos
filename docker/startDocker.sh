#!/bin/bash

docker network create kerb
docker build -f docker/Dockerfile --tag=kerberos .
docker run -it -p 4000:80 --name kerberos kerberos