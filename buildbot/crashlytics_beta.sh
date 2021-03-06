#!/bin/bash

PROPERTIES_FILE="properties.json"
CHANGE_LOG_FILE="changelog.txt"

if [ -e "$PROPERTIES_FILE" ] ; then
    echo "=== Properties ==="
    cat $PROPERTIES_FILE | python -m json.tool
    echo "=== End Properties ==="

    echo "Build: $BUILD_NUMBER" > "$CHANGE_LOG_FILE"
    ./buildbot/CreateChangelog.py "$PROPERTIES_FILE" | head -c 14000 >> "$CHANGE_LOG_FILE"
else
    echo "Build: $BUILD_NUMBER" > "$CHANGE_LOG_FILE"
    git log "HEAD~1..HEAD" | head -c 14000  >> "$CHANGE_LOG_FILE"
fi

if [ -e "$CHANGE_LOG_FILE" ] ; then
    echo "=== Changelog ==="
    cat $CHANGE_LOG_FILE
    echo "=== End Changelog ==="
fi

if [ -z "${TARGET}" ] ; then
    echo "Must supply TARGET so we can figure out which flavor to upload"
    exit 1
fi
echo "TARGET=$TARGET"

if [ -z "${APPLICATION_ID_SUFFIX}" ] ; then
    echo "Must supply APPLICATION_ID_SUFFIX so we can figure out which crashlytics build to upload"
    exit 1
fi
echo "APPLICATION_ID_SUFFIX=${APPLICATION_ID_SUFFIX}"

TERM=dumb
./gradlew "-Pid=${APPLICATION_ID_SUFFIX}" "crashlyticsUploadDistribution${TARGET}Debug"

RESULT=$?

# Cleanup files
rm -f "$CHANGE_LOG_FILE" "$PROPERTIES_FILE"

exit "$RESULT"
