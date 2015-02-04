#!/bin/bash

set -e

source snap/common.sh

getAndroid "platform-tools" "platform-tools"
getAndroid "platforms/android-21" "android-21"
getAndroid "build-tools/21.1.1" "build-tools-21.1.1"
getAndroid "extras/android/m2repository" "extra-android-m2repository"
getAndroid "extras/google/m2repository" "extra-google-m2repository"

gradleww "-Pid=latest" assembleExpediaDebug assembleExpediaDebugTest
gradleww "-Pid=latest" assembleTravelocityDebug
gradleww "-Pid=latest" assembleAirAsiaGoDebug
gradleww "-Pid=latest" assembleVoyagesDebug
