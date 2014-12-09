#!/bin/bash

set -e

function get() {
    local cachedir="$1"
    local package="$2"

    if [ -d "${ANDROID_HOME}/${cachedir}" ] ; then
        echo "${package} already there"
        return 0
    fi

    local START=$(date +%s)
    echo y | android update sdk --no-ui --filter "${package}" --all > /dev/null
    local END=$(date +%s)

    local DIFF=$(echo "$END - $START" | bc)
    echo "${package} took ${DIFF} seconds"
}

get "platform-tools" "platform-tools"
get "platforms/android-21" "android-21"
get "build-tools/21.1.1" "build-tools-21.1.1"
get "extras/android/m2repository" "extra-android-m2repository"
get "extras/google/m2repository" "extra-google-m2repository"

function build() {
    # Disable gradle's fancy log outputting
    TERM=dumb

    ./gradlew --no-daemon $*
}

build assembleExpediaLatest assembleExpediaLatestTest
build assembleTravelocityLatest
build assembleAirAsiaGoLatest
build assembleVoyagesLatest

