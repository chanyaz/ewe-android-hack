#!/bin/bash

set -e

TERM=dumb

./gradlew --no-daemon \
  "clean" \
  ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
  ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport"

./gradlew --no-daemon -PrunProguard=false "assembleExpediaDebug" "assembleExpediaDebugAndroidTest"

./gradlew --no-daemon \
  "checkstyle" ":robolectric:checkstyleTest" "lintExpediaDebug"

./gradlew --no-daemon ":robolectric:test"