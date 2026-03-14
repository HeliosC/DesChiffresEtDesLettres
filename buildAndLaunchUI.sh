#!/bin/bash

set -e

echo "Building Discord SDK..."
./buildDiscordSDK.sh
echo "SDK built"

echo "Building UI..."
./gradlew :composeApp:jsBrowserDevelopmentRun
echo "UI built"
