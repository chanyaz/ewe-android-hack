#!/bin/bash

set -e

TERM=dumb

# So we can let the sdkmanager plugin run and download if the libraries fail to resolve
./gradlew --no-daemon "clean" "--continue"

./gradlew --no-daemon \
  "clean" \
  ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
  ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport"

./gradlew --no-daemon -PrunProguard=false "assembleExpediaDebug" "assembleExpediaDebugAndroidTest"

./gradlew --no-daemon \
  "checkstyle" ":robolectric:checkstyleTest" "lintExpediaDebug"

./gradlew --no-daemon ":robolectric:test"
