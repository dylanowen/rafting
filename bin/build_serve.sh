#!/usr/bin/env bash

FULL_PATH=$(dirname "$0")

COUNT=${1:-3}

sbt docker:publishLocal
${FULL_PATH}/serve.sh ${COUNT}