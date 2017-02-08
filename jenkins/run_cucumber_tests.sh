#!/usr/bin/env bash

flavor=$1
packageName=$2
device=$3
tags=$4

if [ -z "$flavor" ]; then
   echo "Missing Flavor"
   exit 1
fi

if [ -z "$packageName" ]; then
   echo "Missing Package name"
   exit 1
fi

if [ -z "$device" ]; then
   echo "Please specify the device"
   exit 1
fi

echo "Running Cucumber UI tests on $flavor"

function createDummyFilesOnDevice() {
    echo "Creating Dummy Files....."
    adb -s $device shell mkdir /data/local/tmp/cucumber-htmlreport
    echo "Created..... cucumber-htmlreport"
    adb -s $device shell touch /data/local/tmp/cucumber.json
    echo "Created..... cucumber.json"
    adb -s $device shell touch /data/local/tmp/cucumber-htmlreport/formatter.js
    echo "Created..... formatter.js"
    adb -s $device shell touch /data/local/tmp/cucumber-htmlreport/index.html
    echo "Created..... index.html"
    adb -s $device shell touch /data/local/tmp/cucumber-htmlreport/jquery-1.8.2.min.js
    echo "Created..... jquery-1.8.2.min.js"
    adb -s $device shell touch /data/local/tmp/cucumber-htmlreport/report.js
    echo "Created..... report.js"
    adb -s $device shell touch /data/local/tmp/cucumber-htmlreport/style.css
    echo "Created..... style.css"
    createDummyFiles=$?
}

function removeDummyFilesOnDevice() {
    echo "Removing Dummy Files....."
    adb -s $device shell rm -r /data/local/tmp/cucumber-htmlreport
    adb -s $device shell rm /data/local/tmp/cucumber.json
    removeDummyFiles=$?
}

function build() {
    echo "Building assemble${flavor}Debug....."
    ./gradlew --no-daemon clean
    ./gradlew --no-daemon -PcucumberInstrumentation=true assemble${flavor}Debug assemble${flavor}DebugAndroidTest
    buildDebug=$?
}

function installBuild() {
    echo "Installing Builds....."
    flavorLowerCase=$(tr "[A-Z]" "[a-z]" <<< "$flavor")

    echo "installing..... project-${flavorLowerCase}-debug.apk"
    adb -s $device install project/build/outputs/apk/project-${flavorLowerCase}-debug.apk
    installDebug=$?

    if [[ ($installDebug -ne 0) ]]; then
        echo "File, project-${flavorLowerCase}-debug.apk, not found"
        exit 1
    fi

    echo "installing..... project-${flavorLowerCase}-debug-androidTest.apk"
    adb -s $device install project/build/outputs/apk/project-${flavorLowerCase}-debug-androidTest.apk
    installAndroidTest=$?

    if [[ ($installAndroidTest -ne 0) ]]; then
        echo "File, project-${flavorLowerCase}-debug-androidTest.apk, not found"
        exit 1
    fi
    
    adb -s $device shell pm grant "${packageName}.debug" android.permission.ACCESS_FINE_LOCATION

}

function uninstallBuild() {
    echo "Uninstall Builds.."
    # unistall old apks
    adb -s $device uninstall ${packageName}.debug
    echo "Uninstalled ${packageName}.debug"
    adb -s $device uninstall ${packageName}.test
    echo "Uninstalled ${packageName}.test"
}

function runCucumberTests() {
    echo "Running Cucumber Tests"
    tagsPassed=""
    if [ -n "$tags" ]; then
        tagsPassed="-e tags \"${tags}\""
    fi
    echo adb -s $device shell am instrument -w -r -e debug false ${tagsPassed} com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner
    adb -s $device shell am instrument -w -r -e debug false ${tagsPassed} com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner
    runCucumberTests=$?
}

function publishHTMLReport() {
    cd project/build/outputs
    adb -s $device pull /data/local/tmp/cucumber-htmlreport
    publishHTMLReport=$?
}

function printTestStatus() {
    if [ $1 -ne 0 ]; then
        echo "$2: FAILED"
    else
        echo "$2: PASSED"
    fi
}

function printResultsAndExit() {
  if [[ ($buildDebug -ne 0) || ($installBuild -ne 0) || ($runCucumberTests -ne 0) || ($createDummyFiles -ne 0) || ($removeDummyFiles -ne 0) || ($publishHTMLReport -ne 0) ]]; then
    printTestStatus $createDummyFiles "Create dummy files"
    printTestStatus $removeDummyFiles "Remove dummy files"
    printTestStatus $buildDebug "Build Debug"
    printTestStatus $installBuild "Install Build"
    printTestStatus $runCucumberTests "UI tests"
    printTestStatus $publishHTMLReport "Publish HTML report"
    echo "============ FAILURE - PLEASE SEE DETAILS ABOVE ============"
    exit 1
  else
    exit 0
  fi
}

#uninstall existing build
uninstallBuild
build
#remove dummy files if already present
removeDummyFilesOnDevice
createDummyFilesOnDevice
installBuild
runCucumberTests
publishHTMLReport
removeDummyFilesOnDevice
uninstallBuild
printResultsAndExit

