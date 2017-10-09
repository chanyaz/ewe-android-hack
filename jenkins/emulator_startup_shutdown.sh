#!/usr/bin/env bash

getRunningDevices() {
    devices=`adb devices | grep device$ | awk '{ print $1 }'`
    echo -e "All Connected Devices: \n ${devices}"
}

getAvailableEmulators() {
    emulators=`emulator -list-avds`
    echo -e "All Available Emulators: \n ${emulators}"
}

shutdownRunningEmulators() {
    for device in ${devices}; do
        if [[ $device == *"emulator-"* ]]; then
            echo "Shutting Down: $device"
            adb -s $device -e emu kill
        fi
    done
}

startAllAvailableBuilderEmulators() {
    shopt -s nocasematch
    for emu in ${emulators}; do
        if [[ $emu == *"builder"* ]]; then
            emulator -avd $emu & disown
            sleep 1
        fi
    done
}

waitForAllDevicesBootCompleted() {
    for device in ${devices}; do
        adb -s $device wait-for-device
        adb -s $device shell getprop sys.boot_completed
    done
}

getRunningDevices
shutdownRunningEmulators
sleep 5

currDir=`pwd`
cd `dirname $(which emulator)`
getAvailableEmulators
startAllAvailableBuilderEmulators
cd $currDir
sleep 10

getRunningDevices
waitForAllDevicesBootCompleted

