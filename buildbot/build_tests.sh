#!/bin/bash

./gradlew --info --stacktrace --no-daemon -PdisablePreDex clean assembleExpediaDebug assembleExpediaDebugTest assembleExpediaAutoDebug assembleExpediaAutoDebugTest

