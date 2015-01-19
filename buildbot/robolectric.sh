#!/bin/bash

TERM=dumb
./gradlew --no-daemon "clean" ":robolectric:test"
