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
            gtimeout 10 adb -s $device -e emu kill
        fi
    done

    #Ideally this line will not have any effect on an emulator. It will only work if there are
    # emulators, which could not shut down, and who's processes are hanging in the system.
    pkill -9 -f emulator
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
        timer=0
        while :
        do
            if [[ `adb -s $device shell getprop sys.boot_completed` -eq 1 && `adb -s $device -e shell getprop init.svc.bootanim` -eq "stopped" ]]; then
                break
            elif [[ $timer -ge 300 ]]; then
                break
            fi

            sleep 1
            timer=$((timer + 1))
            echo "Waiting for $device, $timer seconds elapsed"
        done
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

waitForAllDevicesBootCompleted
getRunningDevices

