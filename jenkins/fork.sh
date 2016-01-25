#!/bin/bash

export TERM=dumb

internal_artifact() {
	 pushd project/build/fork
	 tar -czvf ~/artifacts/uitests-$BUILD_NUMBER-$1.tar.gz expedia
	 popd
}

./gradlew --no-daemon clean --continue
./gradlew --no-daemon clean

# unistall old apks
./tools/uninstall.sh com.expedia.bookings

build() {
./gradlew --no-daemon aED aEDAT
}

run() {
	 # run tests
	 # We can save up time from reinstallation in case we have faliures.
	 ./gradlew forkExpediaDebug -D "fork.tablet=true" -D android.test.classes=$1
}

build || build
if [ $? -ne 0 ]; then
	echo "Build failed"
	exit 1
	fi

failed_test_classes=""
for runCount in `seq 3`
	do
		echo "run count - $runCount"

		# run test
		run $failed_test_classes

		# Check tests.
		# Creating a comma seprated list for the classes which house the failed tests.
		cat project/build/fork/expedia/debug/summary/fork-*.json |
		tr '}' '\n' |
		grep failureTrace |
		sed 's/.*"testClass":"\([^"]*\)","testMethod":"\([^"]*\)","failureTrace".*/\1/' > project/build/fork/expedia/debug/summary/failed_test_classes.txt

		failed_test_classes=$(cat project/build/fork/expedia/debug/summary/failed_test_classes.txt | tr '\n' ',')

		# exit if all test passed
		if [ "$failed_test_classes" == "" ]; then
			echo "All tests passed quit build."
			internal_artifact "$runCount-success"
			exit 0
		else
			internal_artifact "$runCount-failure"
		fi
	done

exit 1
