#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-androidTest-unaligned.apk"
TYPE="hotels"

if [ -n "$BUILDER_NAME" -a -n "$BUILD_NUMBER" ] ; then
    export OUTPUT_DIR="spoon/${BUILDER_NAME}/${TYPE}/${BUILD_NUMBER}"
else
    export OUTPUT_DIR="spoon/${TYPE}"
fi

# hotels
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelDetailsTest"
#add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelCheckoutUserInfoTest"
#add_test "com.expedia.bookings.test.ui.phone.tests.ui.CreditCardsInfoEditTest"
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelConfirmationTest"
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelRoomsAndRatesTest"
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelFieldValidationTest"
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelReviewsTest"
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelDateAcrossMonthsTest"
add_test "com.expedia.bookings.test.ui.phone.tests.hotels.HotelCouponErrorTest"

#Localization tests. Adding to this script, for now.
#add_test "com.expedia.bookings.test.ui.phone.tests.ui.InfoScreenPhoneNumberTest"
#add_test "com.expedia.bookings.test.ui.phone.tests.ui.InfoScreenSupportNumberLoyaltyTierTest"

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
