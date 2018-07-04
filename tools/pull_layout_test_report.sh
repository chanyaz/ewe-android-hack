#!/bin/bash

if [ "$1" = "" ] ; then
    echo "Package name not available, so it will fetch layout test reports for all package names"
fi

packageName=$1
testName=$2
folderName=project/build/report/layoutTests
mkdir -p ${folderName}
adb pull /sdcard/layoutTests/${packageName} ${folderName}

if [ "$2" = "" ] ; then
    open ${folderName}/$1
else
    open ${folderName}/$1/$2/index.html
fi

