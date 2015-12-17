#!/bin/bash

PATTERN=$1

function uninstall() {
    DEVICE=$1
    PACKAGE=$2

    echo $DEVICE $PACKAGE
    adb -s $DEVICE uninstall $PACKAGE
}

function devices() {
    adb devices | tail -n +2 | cut -sf 1
}

function packages() {
    DEVICE=$1
    adb -s $DEVICE shell 'pm list packages' | perl -p -i -e 's/\r\n$/\n/g' | sed -e 's/package://' | grep $PATTERN
}

for DEVICE in $(devices) ; do
    for PACKAGE in $(packages $DEVICE); do
        uninstall $DEVICE $PACKAGE
    done
done

