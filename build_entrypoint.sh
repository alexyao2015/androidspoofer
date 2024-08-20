#!/bin/bash

# This runs inside the container

# Copy prebuilt UI files into the build directory
cp -r /ui/* /build

# Build the app
./gradlew build
mkdir output
mv app/build/outputs/apk/release/*.apk output
