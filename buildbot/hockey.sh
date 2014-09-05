#!/bin/bash

HOCKEY_URL="https://rink.hockeyapp.net/api/2/apps/${HOCKEY_ID}/app_versions"
HOCKEY_TOKEN="bf8e54b34dcb40c4bee1364a75be509f"

PROPERTIES_FILE="properties.json"
CHANGE_LOG_FILE="changelog.txt"
HOCKEY_LOG_FILE="hockey.txt"

APK=( project/build/outputs/apk/*.apk )
if [ ! -f "${APK[0]}" ] ; then
    echo "Could not find built apk"
    exit 1
else
    APK=${APK[0]}
    echo "Using ${APK}"
fi

if [ -e "$PROPERTIES_FILE" ] ; then
    echo "=== Properties ==="
    cat $PROPERTIES_FILE | python -m json.tool
    echo "=== End Properties ==="

    ./buildbot/CreateChangelog.py $PROPERTIES_FILE | sed -e 's/^/    /' > $CHANGE_LOG_FILE
fi

if [ -e "$CHANGE_LOG_FILE" ] ; then
    echo "=== Changes ==="
    cat $CHANGE_LOG_FILE
    echo "=== End Changes ==="
fi

curl \
-F "status=2" \
-F "notify=1" \
-F "notes=@${CHANGE_LOG_FILE}" \
-F "notes_type=1" \
-F "ipa=@${APK}" \
-H "X-HockeyAppToken: ${HOCKEY_TOKEN}" \
$HOCKEY_URL > $HOCKEY_LOG_FILE

if [ -e "$HOCKEY_LOG_FILE" ] ; then
    echo "=== Hockey Server Response ==="
    cat $HOCKEY_LOG_FILE
    echo ""
    echo "=== End Hockey Server Response ==="
fi

# Delete the files, we already logged them in buildbot
rm -f "$HOCKEY_LOG_FILE" "$CHANGE_LOG_FILE"
