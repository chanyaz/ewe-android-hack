#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-androidTest-unaligned.apk"
OUTPUT_TAR="spoon-happy-${BUILDER_NAME}-${BUILD_NUMBER}.tar.gz"

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    export OUTPUT_DIR="spoon/happy/${BUILDER_NAME}/${BUILD_NUMBER}"
else
    export OUTPUT_DIR="spoon/happy"
fi

# Happypath
add_test "com.expedia.bookings.test.ui.happy.TabletHappyPath"
add_test "com.expedia.bookings.test.ui.happy.PhoneHappyPath"
add_test "com.expedia.bookings.test.ui.happy.CarPhoneHappyPath"
#add_test "com.expedia.bookings.test.ui.happy.LxPhoneHappyPath"

# Cars
add_test "com.expedia.bookings.test.component.cars.CarSearchPresenterTests"
add_test "com.expedia.bookings.test.ui.phone.tests.cars.CarSearchErrorTests"
add_test "com.expedia.bookings.test.ui.phone.tests.cars.CarCreateTripErrorTests"
add_test "com.expedia.bookings.test.ui.phone.tests.cars.CarCheckoutErrorTests"

# LX
add_test "com.expedia.bookings.test.component.lx.LXSearchParamsTest"
add_test "com.expedia.bookings.test.component.lx.LXDetailsPresenterTests"
add_test "com.expedia.bookings.test.component.lx.LXResultsPresenterTests"
add_test "com.expedia.bookings.test.component.lx.LXCheckoutPresenterTests"
add_test "com.expedia.bookings.test.component.lx.LXCheckoutErrorTests"
#add_test "com.expedia.bookings.test.component.lx.LXCreateTripErrorTests"

java \
    -jar "jars/spoon-runner-1.1.3-EXP-jar-with-dependencies.jar" \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name "$TESTS" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"

SPOON_RESULT=$?

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    # Archive to the master
    tar czf "$OUTPUT_TAR" 'spoon/happy'
    scp "$OUTPUT_TAR" "buildbot@buildbot.mobiata.com:/home/buildbot/artifacts/."
    ssh "buildbot@buildbot.mobiata.com" 'cd /home/buildbot/artifacts ; for i in *.tar.gz ; do echo Extracting "$i" ; tar xzf "$i" ; rm -f "$i" ; done ; /home/buildbot/fixperms.sh'

    ## Cleanup locally
    rm -rf "spoon"
    rm -f spoon-happy-*.tar.gz

    # Uninstall
    if [ -z "${APPLICATION_ID_SUFFIX}" ] ; then
        APPLICATION_ID_SUFFIX="latest"
    fi
    echo "APPLICATION_ID_SUFFIX=${APPLICATION_ID_SUFFIX}"
    TERM=dumb
    ./gradlew --no-daemon "-Pid=${APPLICATION_ID_SUFFIX}" uninstallExpediaDebug
fi

exit "$SPOON_RESULT"
