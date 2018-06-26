#!/usr/bin/env bash

getRunningDevices() {
    devices=`adb devices | grep device$ | awk '{ print $1 }'`
    echo -e "All Connected Devices: \n ${devices}"
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

getRunningDevices
shutdownRunningEmulators
sleep 5
