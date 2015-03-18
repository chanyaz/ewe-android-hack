#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-test-unaligned.apk"
OUTPUT_DIR="spoon/lx"

add_test "com.expedia.bookings.test.component.lx.LXDetailsPresenterTests"
add_test "com.expedia.bookings.test.component.lx.LXResultsPresenterTests"
add_test "com.expedia.bookings.test.component.lx.LXSearchParamsTest"
add_test "com.expedia.bookings.test.component.lx.LXCheckoutPresenterTests"
add_test "com.expedia.bookings.test.ui.happy.LxPhoneHappyPath"

rm -rf "spoon"
java \
    -jar "jars/spoon-runner-1.1.3-EXP-jar-with-dependencies.jar" \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name "${TESTS}" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"