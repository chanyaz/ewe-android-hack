#!/bin/bash

function getAndroid() {
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

function gradleww() {
    # Disable gradle's fancy log outputting
    TERM=dumb

    ./gradlew --no-daemon $*
}
