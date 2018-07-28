#!/usr/bin/env bash

COUNT=${1:-3}

docker-compose up --scale raft=${COUNT}
docker-compose stop