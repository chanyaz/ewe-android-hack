#!/bin/bash

set -e

source snap/common.sh

gradleww "-Pid=latest" assembleExpediaDebug assembleExpediaDebugAndroidTest
gradleww "-Pid=latest" assembleTravelocityDebug
gradleww "-Pid=latest" assembleAirAsiaGoDebug
gradleww "-Pid=latest" assembleVoyagesDebug
