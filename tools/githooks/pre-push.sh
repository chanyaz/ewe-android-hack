#!/bin/sh

echo "Running static analysis..."

./gradlew checkstyle ktlint

if [ "$?" != "0" ]; then
  echo "Static analysis found problems. Please see output above and resolve."
  exit 1
fi

exit 0
