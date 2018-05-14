#!/usr/bin/env bash

set -x

source tools/setup_python_env.sh enum "github3.py==1.0.0.a4" slackclient objectpath python-dateutil

# Prepare device and install test butler apk
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X install -r tools/test-butler-app-1.2.0.apk
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc data disable
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc wifi disable

prepare_frequencies_of_flaky_ui_tests() {
	# Cook up the frequencies of flaky UI tests so they are readily available for consumption by the Topmost-Flaky-UI-Tests-Daily-Report Job
	python ./jenkins/prepare_frequencies_of_flaky_ui_tests.py project/build/fork ~/artifacts/$jobName-$BUILD_NUMBER-$1.flaky.tests.frequency.txt
}

./gradlew --no-daemon clean

# unistall old apks
./tools/uninstall.sh com.expedia.bookings

# run test
./gradlew --no-daemon -PrunProguard=false forkExpediaDebug
exitVal=$?

prepare_frequencies_of_flaky_ui_tests

adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc data enable
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc wifi enable

echo "exitVal is $exitVal"
exit $exitVal
