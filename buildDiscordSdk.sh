#!/bin/bash

jsFile="composeApp/src/jsMain/kotlin/fr/helios/dcdl/discord/DiscordSdk.js"
outputFile="composeApp/src/jsMain/kotlin/fr/helios/dcdl/discord/DiscordSdk.kt"

echo 'package fr.helios.dcdl.discord

fun setupDiscordSdk() {
    js("""(function() {
' > $outputFile

cat $jsFile >> $outputFile

echo '
        })();""")
}' >> $outputFile
