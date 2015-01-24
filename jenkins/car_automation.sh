#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug-unaligned.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-test-unaligned.apk"
OUTPUT_DIR="spoon/cars"

add_test "com.expedia.bookings.test.component.cars.CarSearchParamsTests"
add_test "com.expedia.bookings.test.ui.happy.CarPhoneHappyPath"

rm -rf "spoon"
java \
    -jar "jars/spoon-runner-1.1.1-jar-with-dependencies.jar" \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name "${TESTS}" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"