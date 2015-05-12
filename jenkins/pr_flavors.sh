#!/bin/bash

set -e

TERM=dumb

./gradlew --no-daemon -PrunProguard=false \
  clean \
  assembleExpediaDebug assembleExpediaDebugAndroidTest \
  assembleTravelocityDebug \
  assembleAirAsiaGoDebug \
  assembleVoyagesDebug

