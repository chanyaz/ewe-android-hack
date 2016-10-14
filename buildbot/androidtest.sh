#!/bin/bash

TESTS=""
function add_test() {
    TESTS+="$1,"
}

# Run tests
APK="project/build/outputs/apk/project-expedia-debug.apk"
TEST_APK="project/build/outputs/apk/project-expedia-debug-androidTest.apk"
TYPE="single"
OUTPUT_DIR="spoon/${TYPE}"

for i in $* ; do
    add_test "$i"
done

java \
    -jar "jars/spoon-runner-1.1.3-EXP-jar-with-dependencies.jar" \
    --apk  "$APK" \
    --test-apk "$TEST_APK" \
    --class-name "${TESTS}" \
    --no-animations \
    --fail-on-failure \
    --output "$OUTPUT_DIR"

SPOON_RESULT=$?

exit "$SPOON_RESULT"
