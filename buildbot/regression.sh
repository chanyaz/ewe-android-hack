#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-test-unaligned.apk"
TYPE="regression"
OUTPUT_TAR="spoon-${TYPE}-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    export OUTPUT_DIR="spoon/${TYPE}/${BUILDER_NAME}/${BUILD_NUMBER}"
else
    export OUTPUT_DIR="spoon/${TYPE}"
fi

# eb_tp project: #281 Flight Checkout Validation
add_test "com.expedia.bookings.test.ui.phone.tests.flights.FlightCheckoutUserInfoTests"
add_test "com.expedia.bookings.test.ui.tablet.tests.flights.FlightCheckoutUserInfoTests"

# eb_tp project: #243 Booking - Credit Card Types & Security Code
add_test "com.expedia.bookings.test.ui.phone.tests.ui.CreditCardsInfoEditTest"
add_test "com.expedia.bookings.test.ui.tablet.tests.ui.CreditCardsInfoEditTest"

java \
    -jar "jars/spoon-runner-1.1.3-EXP-jar-with-dependencies.jar" \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name "${TESTS}" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"

SPOON_RESULT=$?

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    # Archive to the master
    tar czf "$OUTPUT_TAR" "spoon/${TYPE}"
    scp "$OUTPUT_TAR" "buildbot@buildbot.mobiata.com:/home/buildbot/artifacts/."
    ssh "buildbot@buildbot.mobiata.com" 'cd /home/buildbot/artifacts ; for i in *.tar.gz ; do echo Extracting "$i" ; tar xzf "$i" ; rm -f "$i" ; done ; /home/buildbot/fixperms.sh'

    ## Cleanup locally
    rm -rf "spoon"
    rm -f spoon-${TYPE}-*.tar.gz
fi

exit "$SPOON_RESULT"
