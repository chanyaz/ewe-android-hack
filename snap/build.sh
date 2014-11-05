#!/bin/bash

function get() {
    START=$(date +%s)
    echo y | android update sdk --no-ui --filter "${1}" --all > /dev/null
    END=$(date +%s)
    DIFF=$(echo "$END - $START" | bc)
    echo $1 "took" $DIFF "seconds"
}

get "platform-tools"
get "android-19"
get "extra-android-m2repository"
get "extra-google-m2repository"
get "build-tools-19.1.0"

./gradlew --no-daemon \
    assembleExpediaDebug \
    assembleExpediaDebugTest \
    assembleExpediaAutoDebug \
    assembleExpediaAutoDebugTest \
    assembleTravelocityDebug \
    assembleAirAsiaGoDebug

