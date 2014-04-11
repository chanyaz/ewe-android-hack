#!/bin/bash

# Run tests
APK="project/build/apk/project-ExpediaAutomation-debug-unaligned.apk"
TEST_APK="project/build/apk/project-ExpediaAutomation-debug-test-unaligned.apk"
OUTPUT_DIR="spoon/happy/${BUILDER_NAME}/${BUILD_NUMBER}"
OUTPUT_TAR="spoon-happy-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

java \
    -jar lib/TestUtils/jars/spoon-runner-1.1.1-jar-with-dependencies.jar \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name com.expedia.bookings.test.tests.full.FullHappyPathTest \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"

SPOON_RESULT=$?

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    # Archive to the master
    tar cvzf "$OUTPUT_TAR" 'spoon/happy'
    scp "$OUTPUT_TAR" "buildbot@buildbot.mobiata.com:/home/buildbot/artifacts/."
    ssh "buildbot@buildbot.mobiata.com" 'cd /home/buildbot/artifacts ; umask 022 ; for i in *.tar.gz ; do echo Extrating "$i" ; tar xzf "$i" ; rm -f "$i" ; done'

    ## Cleanup locally
    rm -rf "spoon"
    rm -f spoon-happy-*.tar.gz
fi

exit "$SPOON_RESULT"
