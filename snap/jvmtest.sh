#!/bin/bash

set -e

source snap/common.sh

gradleww ":lib:ExpediaBookings:clean" ":lib:ExpediaBookings:test" ":lib:ExpediaBookings:jacocoTestReport"
