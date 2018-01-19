#!/usr/bin/env bash

set -x

source tools/setup_python_env.sh enum "github3.py==1.0.0.a4" slackclient objectpath python-dateutil

# Prepare device and install test butler apk
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X install -r tools/test-butler-app-1.2.0.apk
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc data disable
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc wifi disable

if [ -z "${ghprbPullId}" ] ; then
	hasPullId=false
	jobName="hourly"
else
	hasPullId=true
	jobName="uitests"
fi
internal_artifact() {
	# Cook up the frequencies of flaky UI tests so they are readily available for consumption by the Topmost-Flaky-UI-Tests-Daily-Report Job
	python ./jenkins/prepare_frequencies_of_flaky_ui_tests.py project/build/fork ~/artifacts/$jobName-$BUILD_NUMBER-$1.flaky.tests.frequency.txt
	pushd project/build/fork
	tar -czvf ~/artifacts/$jobName-$BUILD_NUMBER-$1.tar.gz expedia
	popd
}

if [ "$hasPullId" = "true" ]; then
	# exit if finds 'needs-human' label
	python ./jenkins/prLabeledAsNeedsHuman.py $GITHUB_ACCESS_TOKEN $ghprbPullId
	prLabeledAsNeedsHumanStatus=$?
	if [ $prLabeledAsNeedsHumanStatus -ne 0 ]; then
		echo "PR is labeled needs-human, so exiting..."
		exit 1
	fi

	# exit if UI tests not required
	python ./jenkins/changes_require_ui_test.py $GITHUB_ACCESS_TOKEN $ghprbPullId
	requiresUITestRun=$?
	if [ $requiresUITestRun -ne 0 ]; then
		echo "PR does not have any changes which require UI tests to run"
		exit 0
	fi
fi

./gradlew --no-daemon clean

# unistall old apks
./tools/uninstall.sh com.expedia.bookings

# run test
./gradlew --no-daemon -PrunProguard=false forkExpediaDebug

cat project/build/fork/expedia/debug/summary/fork-*.json |
tr '}' '\n' |
grep failureTrace |
sed 's/.*"testClass":"\([^"]*\)","testMethod":"\([^"]*\)","failureTrace".*/\1/' > project/build/fork/expedia/debug/summary/failed_test_classes.txt

failed_test_classes=$(cat project/build/fork/expedia/debug/summary/failed_test_classes.txt | tr '\n' ',')

# exit if all test passed
if [ "$failed_test_classes" == "" ]; then
    echo "All tests passed quit build."
    internal_artifact "success"
    break
else
    internal_artifact "failure"
fi
if [ "$hasPullId" = "true" ]; then
	python ./jenkins/pr_ui_feedback.py $GITHUB_ACCESS_TOKEN $ghprbGhRepository $ghprbPullId $SLACK_ACCESS_TOKEN
fi

adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc data enable
adb devices | tail -n +2 | cut -sf 1 | xargs -I X adb -s X shell svc wifi enable

exit $?
