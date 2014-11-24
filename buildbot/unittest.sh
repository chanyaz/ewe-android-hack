#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-test-unaligned.apk"
OUTPUT_TAR="spoon-unit-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    export OUTPUT_DIR="spoon/unit/${BUILDER_NAME}/${BUILD_NUMBER}"
else
    export OUTPUT_DIR="spoon/unit"
fi

add_test "com.expedia.bookings.test.unit.tests.DateTestCase"
add_test "com.expedia.bookings.test.unit.tests.FlightFilterTest"
add_test "com.expedia.bookings.test.unit.tests.FlightSearchLegTest"
add_test "com.expedia.bookings.test.unit.tests.FlightSearchParamsTest"
add_test "com.expedia.bookings.test.unit.tests.HotelFilterTest"
add_test "com.expedia.bookings.test.unit.tests.HttpCookieStoreTest"
add_test "com.expedia.bookings.test.unit.tests.ItinContentGeneratorTest"
add_test "com.expedia.bookings.test.unit.tests.JSONUtilsTestCase"
add_test "com.expedia.bookings.test.unit.tests.JodaUtilsTests"
add_test "com.expedia.bookings.test.unit.tests.LocalExpertDataTestCase"
add_test "com.expedia.bookings.test.unit.tests.ShareUtilsTests"
add_test "com.expedia.bookings.test.unit.tests.StrUtilsTests"

java \
    -jar "jars/spoon-runner-1.1.1-jar-with-dependencies.jar" \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name "${TESTS}" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"

SPOON_RESULT=$?

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    # Archive to the master
    tar cvzf "$OUTPUT_TAR" 'spoon/unit'
    scp "$OUTPUT_TAR" "buildbot@buildbot.mobiata.com:/home/buildbot/artifacts/."
    ssh "buildbot@buildbot.mobiata.com" 'cd /home/buildbot/artifacts ; for i in *.tar.gz ; do echo Extracting "$i" ; tar xzf "$i" ; rm -f "$i" ; done ; /home/buildbot/fixperms.sh'

    ## Cleanup locally
    rm -rf "spoon"
    rm -f spoon-unit-*.tar.gz
fi

exit "$SPOON_RESULT"
