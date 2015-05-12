#!/bin/bash

set -e

TERM=dumb

# So we can let the sdkmanager plugin run and download if the libraries fail to resolve
./gradlew --no-daemon "clean" "--continue"

# Retry once because of current kotlin compilation issue. The 2nd time should work
./gradlew --no-daemon \
  "clean" \
  ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
  ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport" || \
  ":lib:mocked:mocke3:test" \
./gradlew --no-daemon \
  "clean" \
  ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
  ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport" \
  ":lib:mocked:mocke3:test"

./gradlew --no-daemon \
  "checkstyle" ":robolectric:checkstyleTest" "lintExpediaDebug"

./gradlew --no-daemon ":robolectric:test"
