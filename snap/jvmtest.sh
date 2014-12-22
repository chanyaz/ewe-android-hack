#!/bin/bash

set -e

source snap/common.sh

gradleww ":lib:ExpediaBookings:test"
