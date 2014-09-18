#!/bin/bash

PROPERTIES_FILE="properties.json"
CHANGE_LOG_FILE="changelog.txt"

if [ -e "$PROPERTIES_FILE" ] ; then
    echo "=== Properties ==="
    cat $PROPERTIES_FILE | python -m json.tool
    echo "=== End Properties ==="

    ./buildbot/CreateChangelog.py "$PROPERTIES_FILE" | sed -e 's/^/    /' > "$CHANGE_LOG_FILE"
fi

./gradlew --info --stacktrace --no-daemon -DdisablePreDex clean assembleExpediaLatest

./gradlew crashlyticsUploadDistributionExpediaLatest

# Cleanup files
rm -f "$CHANGE_LOG_FILE" "$PROPERTIES_FILE"
