#!/bin/bash

set -eux -o pipefail

# This runs inside the container
mkdir -p $GRADLE_USER_HOME || true
mkdir -p $ANDROID_HOME || true

# Accept Android SDK licenses
yes | sdkmanager --licenses --sdk_root="${ANDROID_HOME}" || true

# Copy prebuilt UI files into the build directory
cp -r /ui/* /build

# Build the app
./gradlew build
mkdir output || true
mv app/build/outputs/apk/release/*.apk output
