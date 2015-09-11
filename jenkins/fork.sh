#!/bin/bash

export TERM=dumb

internal_artifact() {
  pushd project/build/fork
  tar -czvf ~/artifacts/uitests-$BUILD_NUMBER.tar.gz expedia
  popd
}

# artifact when script exits no matter success or failure
trap internal_artifact EXIT

./gradlew --no-daemon clean --continue
./gradlew --no-daemon clean

# unistall old apks
./tools/uninstall.sh com.expedia.bookings

# run tests
./gradlew --no-daemon -D"fork.tablet=true" aED aEDAT forkExpediaDebug
