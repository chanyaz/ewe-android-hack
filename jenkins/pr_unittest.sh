#!/usr/bin/env bash

KOTLIN_DUMMY_FILE="./project/src/main/java/com/expedia/bookings/utils/DummyFiletoHandleKotlinLintError.java"
KOTLIN_UNUSED_RESOURCES_REPORT_FILE="project/build/outputs/kotlin-unused-resources.txt"
flavor=$1

function updateJenkinsFlag() {
  if [ -n "${BUILD_NUMBER}" ]; then
	isJenkins=true
  fi
}

function prepareLocalMavenRepo() {
  # Robolectric does not play well with multiple threads: https://github.com/robolectric/robolectric/issues/2346
  # Turns out the problem is an internal maven downloader. Workaround is to make sure resources are downloaded ahead of time
  if [ -n `which mvn` ]; then
    mvn -f jenkins/robo3-api18-pom.xml dependency:go-offline
    mvn -f jenkins/robo3-api21-pom.xml dependency:go-offline
    mvn -f jenkins/robo3-api23-pom.xml dependency:go-offline
  fi
}

function setUpForPythonScripts() {
# do python scripts related setup only if we are running in CI context and a special feature requiring python setup is asked for
  if [[ $isJenkins && ("$isPRPoliceEnabled" == "true" || "$isUnitTestsFeedbackBotEnabled" == "true") ]]; then
    export PYTHONIOENCODING=utf-8

    if [ ! -d 'virtualenv' ]; then
        virtualenv -p python2.7 virtualenv
    fi

    source ./virtualenv/bin/activate

    pip install --upgrade "pip"
    pip install enum
    pip install "github3.py==1.0.0.a4"
    pip install slackclient
    pip install "lxml==3.5.0"
    pip install python-dateutil

    # exit if finds 'needs-human' label
    python ./jenkins/prLabeledAsNeedsHuman.py $GITHUB_ACCESS_TOKEN $ghprbPullId
    prLabeledAsNeedsHumanStatus=$?
    if [ $prLabeledAsNeedsHumanStatus -ne 0 ]; then
       echo "PR is labeled needs-human, so exiting..."
       exit 1
    fi
  fi
}

function runPRPolice() {
  if [ "$isPRPoliceEnabled" == "true" ]; then
    # Invoke PR Police to check for issues
    python ./jenkins/pr_police/PRPolice.py ${GITHUB_ACCESS_TOKEN} ${ghprbPullId}
    prPoliceStatus=$?
  else
    echo "PR Police is disabled!"
    prPoliceStatus=0
  fi
}

function sdkManagerWorkAround() {
  # So the sdkmanager plugin can run and download if the libraries fail to resolve
  ./gradlew --no-daemon --continue "-Dorg.gradle.configureondemand=false" "clean"
}

function runUnitTests() {
    if [ "$flavor" != "Expedia" ] && [ "$flavor" != "" ]; then
        ./gradlew --no-daemon \
            "test${flavor}DebugUnitTest"
        unitTestStatus=$?
    else
        echo "Running Expedia"
        ./lib/mocked/validate.sh || return 1
        ./tools/validate-strings.sh ./project/src/main/res main || return 1
        ./gradlew --no-daemon \
            ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
            "checkstyle" "lintExpediaDebug" \
            ":lib:mocked:mocke3:test" \
            ":lib:AccountLib:test" \
            ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport" \
            ":project:jacocoExpediaDebug"
        unitTestStatus=$?
    fi
}

function runKotlinUnusedResourceCheck() {
  rm -f ${KOTLIN_UNUSED_RESOURCES_REPORT_FILE}
  cat ${KOTLIN_DUMMY_FILE} | perl ./jenkins/check_for_resources_not_used_by_kotlin.pl > ${KOTLIN_UNUSED_RESOURCES_REPORT_FILE}
  kotlinUnusedResourcesStatus=$?
  if [ $kotlinUnusedResourcesStatus -ne 0 ]; then
    cat ./project/build/outputs/kotlin-unused-resources.txt
  fi
}

function runFeedbackAndCoverageReports() {
  if [[ $isJenkins && "$isUnitTestsFeedbackBotEnabled" == "true" ]]; then
    python ./jenkins/pr_unit_feedback.py $GITHUB_ACCESS_TOKEN $ghprbGhRepository $ghprbPullId $SLACK_ACCESS_TOKEN
  fi

  if [[ "$flavor" == "Expedia" ]]; then
      if [[ $isJenkins && $unitTestStatus -eq 0 ]]; then
        BUILD_URL="https://jenkins-ewe-mobile-android-master.tools.expedia.com/job/$JOB_NAME/$BUILD_NUMBER"
        python ./jenkins/report_missing_code_coverage.py $GITHUB_ACCESS_TOKEN $ghprbPullId $BUILD_URL project/build/reports/jacoco/jacocoExpediaDebug/jacocoExpediaDebug.xml lib/ExpediaBookings/build/reports/jacoco/test/jacocoTestReport.xml
        coverageBotStatus=$?
      else
        echo "Either script was not run on Jenkins or the unit tests failed. Not invoking Coverage Bot."
        coverageBotStatus=1
      fi
  fi
}

function printTestStatus() {
    if [ $1 -ne 0 ]; then
        echo "$2: FAILED"
    else
        echo "$2: PASSED"
    fi
}

function printResultsAndExit() {
  if [[ "$flavor" == "Expedia" && ($prPoliceStatus -ne 0) ]]; then
    echo "WARNING: PR Police has flagged potential problems."
  fi
  if [[ "$flavor" == "Expedia" && (($unitTestStatus -ne 0) || ($kotlinUnusedResourcesStatus -ne 0)) ]]; then
    printTestStatus $unitTestStatus "Unit tests"
    printTestStatus $kotlinUnusedResourcesStatus "Kotlin unused resources"
    echo "============ FAILURE - PLEASE SEE DETAILS ABOVE ============"
    exit 1
  elif [[ ($unitTestStatus -ne 0) ]]; then
      printTestStatus $unitTestStatus "Unit tests"
      echo "============ FAILURE - PLEASE SEE DETAILS ABOVE ============"
      exit 1
  else
    exit 0
  fi
}

if [ "$flavor" == "" ]; then
    echo "Please pass flavor to run the script"
    exit 1
fi

updateJenkinsFlag
prepareLocalMavenRepo
setUpForPythonScripts
if [ "$flavor" == "Expedia" ]; then
    runPRPolice
fi
sdkManagerWorkAround
runUnitTests
if [ "$flavor" == "Expedia" ]; then
    runKotlinUnusedResourceCheck
fi
runFeedbackAndCoverageReports
printResultsAndExit