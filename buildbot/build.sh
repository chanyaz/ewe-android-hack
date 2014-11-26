#!/bin/bash


TARGET=$(echo ${BUILDER_NAME} | perl -ne 'print ucfirst($_)')

if [ -z "${TARGET}" ] ; then
    # Just default to expedia
    TARGET="Expedia"
fi

echo "target=$TARGET"

TERM=dumb
./gradlew --no-daemon "clean" "assemble${TARGET}Latest" "assemble${TARGET}LatestTest"
