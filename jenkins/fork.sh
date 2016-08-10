#!/usr/bin/env bash

set -x

GITHUB_TOKEN=0a1f692d47819eec1349e990240525233a12b4fd
HIPCHAT_TOKEN=MdHG4PNWYSGD41jwF4TvVfhNADhw0NnOyGdjw3uI

if [ ! -d 'virtualenv' ] ; then
    virtualenv -p python2.7 virtualenv
fi

source ./virtualenv/bin/activate

pip install --upgrade "pip"
pip install enum
pip install "github3.py==1.0.0.a4"
pip install "hypchat==0.21"
pip install objectpath

internal_artifact() {
	# Cook up the frequencies of flaky UI tests so they are readily available for consumption by the Topmost-Flaky-UI-Tests-Daily-Report Job
	python ./jenkins/prepare_frequencies_of_flaky_ui_tests.py project/build/fork ~/artifacts/uitests-$BUILD_NUMBER-$1.flaky.tests.frequency.txt
	pushd project/build/fork
	tar -czvf ~/artifacts/uitests-$BUILD_NUMBER-$1.tar.gz expedia
	popd
}

# exit if finds 'needs-human' label
python ./jenkins/prLabeledAsNeedsHuman.py $GITHUB_TOKEN $ghprbPullId
prLabeledAsNeedsHumanStatus=$?
if [ $prLabeledAsNeedsHumanStatus -ne 0 ]; then
   echo "PR is labeled needs-human, so exiting..."
   exit 1
fi

./gradlew --no-daemon clean --continue
./gradlew --no-daemon clean

# unistall old apks
./tools/uninstall.sh com.expedia.bookings

build() {
./gradlew --no-daemon aExpediaDebug aExpediaDebugAT
}

run() {
	 # run tests
	 ./gradlew --no-daemon forkExpediaDebug -D "fork.tablet=true" -D android.test.classes=$1
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

		cat project/build/fork/expedia/debug/summary/fork-*.json |
		tr '}' '\n' |
		grep failureTrace |
		sed 's/.*"testClass":"\([^"]*\)","testMethod":"\([^"]*\)","failureTrace".*/\1/' > project/build/fork/expedia/debug/summary/failed_test_classes.txt

		failed_test_classes=$(cat project/build/fork/expedia/debug/summary/failed_test_classes.txt | tr '\n' ',')

		# exit if all test passed
		if [ "$failed_test_classes" == "" ]; then
			echo "All tests passed quit build."
			internal_artifact "$runCount-success"
			break
		else
			internal_artifact "$runCount-failure"
		fi
	done

python ./jenkins/pr_ui_feedback.py $GITHUB_TOKEN $ghprbGhRepository $ghprbPullId $HIPCHAT_TOKEN
exit $?
