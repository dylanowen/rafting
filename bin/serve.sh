#!/usr/bin/env bash

COUNT=${1:-5}

docker-compose up --scale raft=${COUNT}
docker-compose stop