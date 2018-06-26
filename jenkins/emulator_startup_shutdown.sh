#!/usr/bin/env bash
source ./emulator_startup.sh
source ./emulator_shutdown.sh

getRunningDevices
shutdownRunningEmulators
sleep 5

currDir=`pwd`
cd `dirname $(which emulator)`
getAvailableEmulators
startAllAvailableBuilderEmulators
cd $currDir
sleep 10

waitForAllDevicesBootCompleted
getRunningDevices

