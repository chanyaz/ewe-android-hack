#!/bin/bash

set -e

TERM=dumb

# So the sdkmanager plugin can run and download if the libraries fail to resolve
./gradlew --no-daemon --continue "-Dorg.gradle.configureondemand=false" "clean"

run() {
    ./lib/mocked/validate.sh || return 1
    ./tools/validate-strings.sh ./project/src/main/res || return 1
    ./gradlew --no-daemon \
        "clean" \
        ":lib:mocked:mocke3:test" \
        ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport" \
        ":project:testExpediaDebug" \
        ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
        "checkstyle" "lintExpediaDebug"
}

# Retry once because of current kotlin compilation issue. The 2nd time should work
run || run
