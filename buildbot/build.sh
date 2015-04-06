#!/bin/bash

if [ -z "${TARGET}" ] ; then
    TARGET="Expedia"
fi
echo "TARGET=$TARGET"

if [ -z "${APPLICATION_ID_SUFFIX}" ] ; then
    APPLICATION_ID_SUFFIX="latest"
fi
echo "APPLICATION_ID_SUFFIX=${APPLICATION_ID_SUFFIX}"

TERM=dumb
./gradlew --no-daemon "clean" "--continue"
./gradlew --no-daemon "-Pid=${APPLICATION_ID_SUFFIX}" "clean" "assemble${TARGET}Debug" "assemble${TARGET}DebugAndroidTest"
