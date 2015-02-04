#!/bin/bash

set -e

# Skip pull requests
test -z "${SNAP_PULL_REQUEST_NUMBER}" || exit 0

source snap/common.sh

getAndroid "platform-tools" "platform-tools"
getAndroid "platforms/android-21" "android-21"
getAndroid "build-tools/21.1.1" "build-tools-21.1.1"
getAndroid "extras/android/m2repository" "extra-android-m2repository"
getAndroid "extras/google/m2repository" "extra-google-m2repository"

echo """
SNAP_PIPELINE_COUNTER: ${SNAP_PIPELINE_COUNTER}
SNAP_BRANCH: ${SNAP_BRANCH}

$(git log ${SNAP_COMMIT}^..${SNAP_COMMIT})
""" | head -c 14000 > "changelog.txt"

gradleww "-Pid=latest" crashlyticsUploadDistributionTravelocityDebug
gradleww "-Pid=latest" crashlyticsUploadDistributionAirAsiaGoDebug
gradleww "-Pid=latest" crashlyticsUploadDistributionVoyagesDebug
