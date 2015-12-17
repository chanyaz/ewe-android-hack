#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-androidTest-unaligned.apk"
TYPE="cars"

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    export OUTPUT_DIR="spoon/${BUILDER_NAME}/${TYPE}/${BUILD_NUMBER}"
else
    export OUTPUT_DIR="spoon/${TYPE}"
fi

# Cars
add_test "com.expedia.bookings.test.phone.cars.CarSearchPresenterTest"
add_test "com.expedia.bookings.test.phone.cars.CarSearchErrorTest"
add_test "com.expedia.bookings.test.phone.cars.CarCreateTripErrorTest"
add_test "com.expedia.bookings.test.phone.cars.CarCheckoutErrorTest"
add_test "com.expedia.bookings.test.phone.cars.CarFilterTest"
add_test "com.expedia.bookings.test.phone.cars.CarCreditCardTest"
add_test "com.expedia.bookings.test.phone.cars.CarDetailsTest"
add_test "com.expedia.bookings.test.phone.cars.CarCheckoutViewTest"

java \
    -jar "jars/spoon-runner-1.1.3-EXP-jar-with-dependencies.jar" \
    --apk  "${APK}" \
    --test-apk "${TEST_APK}" \
    --class-name "${TESTS}" \
    --no-animations \
    --fail-on-failure \
    --output "${OUTPUT_DIR}"

SPOON_RESULT=$?

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    # Uninstall
    if [ -z "${APPLICATION_ID_SUFFIX}" ] ; then
        APPLICATION_ID_SUFFIX="latest"
    fi
    echo "APPLICATION_ID_SUFFIX=${APPLICATION_ID_SUFFIX}"
    TERM=dumb
    ./gradlew --no-daemon "-Pid=${APPLICATION_ID_SUFFIX}" uninstallExpediaDebug
fi

exit $SPOON_RESULT
