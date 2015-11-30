#!/bin/bash

set -e

TERM=dumb

# So the sdkmanager plugin can run and download if the libraries fail to resolve
./gradlew --no-daemon --continue "-Dorg.gradle.configureondemand=false" "clean"

build() {
    ./gradlew --no-daemon -PrunProguard=false \
        "clean" \
        "assembleTravelocityDebug" \
        "assembleAirAsiaGoDebug" \
        "assembleVoyagesDebug" \
        "assembleWotifDebug" \
        "assembleLastMinuteDebug"
}

# Retry once because of current kotlin compilation issue. The 2nd time should work
build || build
