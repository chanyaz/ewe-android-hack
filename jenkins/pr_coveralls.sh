#!/usr/bin/env bash

export CI_NAME="jenkins"
export CI_BRANCH=$GIT_BRANCH
export CI_BUILD_NUMBER=$BUILD_NUMBER
export CI_BUILD_URL=$BUILD_URL
export CI_PULL_REQUEST=$ghprbPullId

./gradlew jacocoTestReport coveralls
