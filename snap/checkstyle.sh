#!/bin/bash

set -e

source snap/common.sh

gradleww \
    "checkstyle" \
    ":lib:ExpediaBookings:checkstyleMain" ":lib:ExpediaBookings:checkstyleTest" \
    ":robolectric:checkstyleTest"
