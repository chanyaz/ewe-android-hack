#!/bin/bash

CHANGE_LOG_FILE="changelog.txt"

LOG_COMMITS="HEAD~1..HEAD"

if [ -n "$GIT_COMMIT" -a -n "$GIT_PREVIOUS_SUCCESSFUL_COMMIT" ] ; then
    LOG_COMMITS="$GIT_PREVIOUS_SUCCESSFUL_COMMIT..$GIT_COMMIT"
fi

echo "Build: $BUILD_NUMBER" > "$CHANGE_LOG_FILE"
git log $LOG_COMMITS | head -c 14000  >> "$CHANGE_LOG_FILE"

if [ -e "$CHANGE_LOG_FILE" ] ; then
    echo "=== Changelog ==="
    cat $CHANGE_LOG_FILE
    echo "=== End Changelog ==="
fi

if [ -z "${TARGET}" ] ; then
    TARGET=${1}
    if [ -z "$TARGET" ] ; then
        echo "Must supply TARGET so we can figure out which flavor to upload"
        exit 1
    fi
fi
echo "TARGET=$TARGET"

if [ -z "${BUILD_VARIANT}" ] ; then
    BUILD_VARIANT=Debug
fi
echo "BUILD_VARIANT=$BUILD_VARIANT"

TERM=dumb

if [ -z "${APPLICATION_ID_SUFFIX}" ] ; then
    APPLICATION_ID_SUFFIX=${2}
fi


if [ -z "${APPLICATION_ID_SUFFIX}" ] ; then
    ./gradlew "crashlyticsUploadDistribution${TARGET}${BUILD_VARIANT}"
else
    ./gradlew "-Pid=${APPLICATION_ID_SUFFIX}" "crashlyticsUploadDistribution${TARGET}${BUILD_VARIANT}"
fi

RESULT=$?

# Cleanup files
rm -f "$CHANGE_LOG_FILE" "$PROPERTIES_FILE"

exit "$RESULT"
