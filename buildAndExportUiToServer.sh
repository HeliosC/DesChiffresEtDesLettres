#!/bin/bash

set -e

echo "Building Discord SDK..."
./buildDiscordSDK.sh
echo "SDK built"

echo "Building UI..."
./gradlew :composeApp:jsBrowserDevelopmentExecutableDistribution
echo "UI built"

echo "Exporting UI to server..."
./exportUiBuildToServer.sh
echo "Server ready!"

echo "Starting server..."
./gradlew :server:run
