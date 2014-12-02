#!/bin/bash

TERM=dumb
./gradlew --no-daemon "clean" ":lib:ExpediaBookings:test"
