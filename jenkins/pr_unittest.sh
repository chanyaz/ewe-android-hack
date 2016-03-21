#!/bin/bash

TERM=dumb

GITHUB_TOKEN=7d400f5e78f24dbd24ee60814358aa0ab0cd8a76
# Invoke PR Police to check for issues
python ./jenkins/pr_police/PRPolice.py ${GITHUB_TOKEN} ${ghprbPullId}
prPoliceStatus=$?

set -e

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
unitTestStatus=$?

if [[ ($unitTestStatus -ne 0) || ($prPoliceStatus -ne 0) ]]; then
    exit 1
fi