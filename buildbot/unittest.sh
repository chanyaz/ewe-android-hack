#!/bin/bash

# Run tests
APK="project/build/apk/project-Expedia-debug-unaligned.apk"
TEST_APK="project/build/apk/project-Expedia-debug-test-unaligned.apk"
OUTPUT_DIR="spoon/unit/${BUILDER_NAME}/${BUILD_NUMBER}"
OUTPUT_TAR="spoon-unit-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

java \
    -jar lib/TestUtils/jars/spoon-runner-1.1.1-jar-with-dependencies.jar \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"

SPOON_RESULT=$?

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    # Archive to the master
    tar cvzf "$OUTPUT_TAR" 'spoon/unit'
    scp "$OUTPUT_TAR" "buildbot@buildbot.mobiata.com:/home/buildbot/artifacts/."
    ssh "buildbot@buildbot.mobiata.com" 'cd /home/buildbot/artifacts ; umask 022 ; for i in *.tar.gz ; do echo Extrating "$i" ; tar xzf "$i" ; rm -f "$i" ; done'

    ## Cleanup locally
    rm -rf "spoon"
    rm -f spoon-unit-*.tar.gz
fi

exit "$SPOON_RESULT"
