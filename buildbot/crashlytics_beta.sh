#!/bin/bash

PROPERTIES_FILE="properties.json"
CHANGE_LOG_FILE="changelog.txt"

if [ -e "$PROPERTIES_FILE" ] ; then
    echo "=== Properties ==="
    cat $PROPERTIES_FILE | python -m json.tool
    echo "=== End Properties ==="

    echo "Build: $BUILD_NUMBER" > "$CHANGE_LOG_FILE"
    ./buildbot/CreateChangelog.py "$PROPERTIES_FILE" >> "$CHANGE_LOG_FILE"
fi

if [ -e "$CHANGE_LOG_FILE" ] ; then
    echo "=== Changelog ==="
    cat $CHANGE_LOG_FILE
    echo "=== End Changelog ==="
fi

TARGET=$(echo ${BUILDER_NAME} | perl -ne 'print ucfirst($_)')

if [ -z "${TARGET}" ] ; then
    echo "Must supply a proper BUILDER_NAME so we can figure out which flavor to build"
    exit 1
fi

./gradlew --info --stacktrace --no-daemon -PdisablePreDex "clean" "assemble${TARGET}Latest"

./gradlew "crashlyticsUploadDistribution${TARGET}Latest"

# Cleanup files
rm -f "$CHANGE_LOG_FILE" "$PROPERTIES_FILE"
