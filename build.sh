#!/bin/bash

set -eux -o pipefail

docker build \
  -t builder \
  --load \
  ${DOCKER_BUILD_ARGS:-} \
  .

docker run \
  --name builder \
  -e ANDROID_STORE_PASSWORD="${ANDROID_STORE_PASSWORD:-}" \
  -e ANDROID_KEY_PASSWORD="${ANDROID_KEY_PASSWORD:-}" \
  --user $UID:$(id -g) \
  -v ${PWD}:/build \
  builder
# docker cp builder:/build/output .
docker rm -f builder
