#!/bin/bash

# Run tests
APK="project/build/apk/project-ExpediaAutomation-debug-unaligned.apk"
TEST_APK="project/build/apk/project-ExpediaAutomation-debug-test-unaligned.apk"
TYPE="regression"
OUTPUT_TAR="spoon-${TYPE}-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    export OUTPUT_DIR="spoon/${TYPE}/${BUILDER_NAME}/${BUILD_NUMBER}"
else
    export OUTPUT_DIR="spoon/${TYPE}"
fi

TESTS=""
TESTS+="com.expedia.bookings.test.tests.tablet.Hotels.ui.regression.HotelSearchFilterTests,"
TESTS+="com.expedia.bookings.test.tests.tablet.Hotels.ui.regression.HotelSearchSortTests,"

TESTS+="com.expedia.bookings.test.tests.tablet.Flights.ui.regression.FlightCheckoutUserInfoTests,"
TESTS+="com.expedia.bookings.test.tests.tablet.Flights.ui.regression.FlightDetailsTests,"
TESTS+="com.expedia.bookings.test.tests.tablet.Flights.ui.regression.FlightSearchResultsSortTests,"

TESTS+="com.expedia.bookings.test.tests.hotelsEspresso.ui.regression.HotelRoomsAndRatesTests,"

java \
    -jar lib/TestUtils/jars/spoon-runner-1.1.1-jar-with-dependencies.jar \
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
