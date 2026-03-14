#!/bin/bash

buildDir="composeApp/build/dist/js/developmentExecutable"
serverUiDir="server/build/dist/js/developmentExecutable"

rm -r $serverUiDir
cp -r $buildDir $serverUiDir
