#!/bin/bash

set -e

TERM=dumb

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

# exit if finds 'needs-human' label
python ./jenkins/prLabeledAsNeedsHuman.py $GITHUB_TOKEN $ghprbPullId
prLabeledAsNeedsHumanStatus=$?
if [ $prLabeledAsNeedsHumanStatus -ne 0 ]; then
   echo "PR is labeled needs-human, so exiting..."
   exit 1
fi

# So the sdkmanager plugin can run and download if the libraries fail to resolve
./gradlew --no-daemon --continue "-Dorg.gradle.configureondemand=false" "clean"

build() {
    ./gradlew --no-daemon -PrunProguard=false \
        "clean" \
        "assembleTravelocityDebug" \
        "assembleAirAsiaGoDebug" \
        "assembleVoyagesDebug" \
        "assembleWotifDebug" \
        "assembleLastMinuteDebug" \
        "assembleSamsungDebug" \
        "assembleOrbitzDebug" \
        "assembleCheapTicketsDebug" \
        "assembleEbookersDebug" \
        "assembleMrJetDebug" 2> >(tee /tmp/flavorsFeedbackBotErrors.txt >&2)
}

# Retry once because of current kotlin compilation issue. The 2nd time should work
build || build
buildStatus=$?

python ./jenkins/pr_flavors_feedback.py $GITHUB_TOKEN $ghprbGhRepository $ghprbPullId $HIPCHAT_TOKEN $buildStatus

