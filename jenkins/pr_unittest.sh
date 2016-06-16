#!/usr/bin/env bash

if [ -n "${BUILD_NUMBER}" ]; then
	isJenkins=true
fi

# do python scripts related setup only if we are running in CI context and a special feature requiring python setup is asked for
if [[ $isJenkins && ("$isPRPoliceEnabled" == "true" || "$isUnitTestsFeedbackBotEnabled" == "true")]]; then
    GITHUB_TOKEN=7d400f5e78f24dbd24ee60814358aa0ab0cd8a76
    HIPCHAT_TOKEN=3htGpj4sE9XxUToWvWCWWmISA3op2U1roRufVjpQ

    if [ ! -d 'virtualenv' ]; then
        virtualenv -p python2.7 virtualenv
    fi

    source ./virtualenv/bin/activate

    pip install --upgrade "pip"
    pip install enum
    pip install "github3.py==1.0.0.a4"
    pip install "hypchat==0.21"
    pip install "lxml==3.5.0"

    # exit if finds 'needs-human' label
    python ./jenkins/prLabeledAsNeedsHuman.py $GITHUB_TOKEN $ghprbPullId
    prLabeledAsNeedsHumanStatus=$?
    if [ $prLabeledAsNeedsHumanStatus -ne 0 ]; then
       echo "PR is labeled needs-human, so exiting..."
       exit 1
    fi
fi

if [ "$isPRPoliceEnabled" == "true" ]; then
    # Invoke PR Police to check for issues
    python ./jenkins/pr_police/PRPolice.py ${GITHUB_TOKEN} ${ghprbPullId}
    prPoliceStatus=$?
else
    echo "PR Police is disabled!"
    prPoliceStatus=0
fi

# So the sdkmanager plugin can run and download if the libraries fail to resolve
./gradlew --no-daemon --continue "-Dorg.gradle.configureondemand=false" "clean"

run() {
    ./lib/mocked/validate.sh || return 1
    ./tools/validate-strings.sh ./project/src/main/res || return 1
    ./gradlew --no-daemon \
        "clean" \
        ":lib:mocked:mocke3:test" \
        ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport" \
        ":project:jacocoExpediaDebug" \
        ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
        "checkstyle" "lintExpediaDebug"
}

# Retry once because of current kotlin compilation issue. The 2nd time should work
run || run
unitTestStatus=$?

# only if we are running in CI context and unittests Feedback Bot is enabled
if [[ $isJenkins && "$isUnitTestsFeedbackBotEnabled" == "true" ]]; then
    python ./jenkins/pr_unit_feedback.py $GITHUB_TOKEN $ghprbGhRepository $ghprbPullId $HIPCHAT_TOKEN
fi

if [[ $isJenkins && $unitTestStatus -eq 0 ]]; then
    BUILD_URL="https://ewemobile.jenkins.test.expedia.com/job/$JOB_NAME/$BUILD_NUMBER"
    python ./jenkins/report_missing_code_coverage.py $GITHUB_TOKEN $ghprbPullId $BUILD_URL project/build/reports/jacoco/jacocoExpediaDebug/jacocoExpediaDebug.xml lib/ExpediaBookings/build/reports/jacoco/test/jacocoTestReport.xml
    coverageBotStatus=$?
else
    echo "Either script was not run on Jenkins or the unit tests failed. Not invoking Coverage Bot."
    coverageBotStatus=1
fi

if [[ ($unitTestStatus -ne 0) || ($prPoliceStatus -ne 0) ]]; then
    exit 1
else
    exit 0
fi
