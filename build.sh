#!/usr/bin/env bash

set -eux -o pipefail

# Cleanup function to remove builder container
cleanup() {
  echo "Cleaning up builder container..."
  docker rm -f builder 2>/dev/null || true
}

# Set trap to cleanup on script exit
trap cleanup EXIT

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
