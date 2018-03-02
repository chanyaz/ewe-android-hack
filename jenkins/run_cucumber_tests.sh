#!/usr/bin/env bash
#!/bin/ksh

# Defaults
debug="false" # Making debug by default as false
declare -i maxReRuns=2 # integer, 0 is the first run, 1 is the rerun, etc...

while echo $1 | grep -q ^-; do
    eval $( echo $1 | sed 's/^-//' )=$2
    shift
    shift
done

flavor=$flavor
packageName=$package
tags=$tags
debug=$debug
noclean=$noclean

parentDir=`pwd`"/project/build/outputs"
failedTagsFile="$parentDir/failedTagsFile.txt"
failureArchive="$parentDir/FailureArchive"


if [ -z "$flavor" ]; then
   echo "Missing Flavor"
   exit 1
fi

if [ -z "$packageName" ]; then
   echo "Missing Package name"
   exit 1
fi

if [ -z "$tags" ]; then
   echo "Missing Tags"
   exit 1
fi

if [ ! -z "$maxreruns" ]; then
    maxReRuns=$maxreruns
fi

echo "Running Cucumber UI tests on $flavor with a maximum of $maxReRuns re-runs"

function createDummyFilesOnDevice() {
    device=$1
    echo "Creating Dummy Files....."
    adb -s $device shell mkdir /data/local/tmp/cucumber-htmlreport
    echo "Created..... cucumber-htmlreport"
    adb -s $device shell touch /data/local/tmp/cucumber-htmlreport/cucumber.json
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
    device=$1
    echo "Removing Dummy Files....."
    adb -s $device shell rm -r /data/local/tmp/cucumber-htmlreport
    adb -s $device shell rm -r /sdcard/cucumber-images
    removeDummyFiles=$?
}

function build() {
    echo "Building assemble${flavor}Debug....."
    if [ "$noclean" != "true" ]
        then
            ./gradlew --no-daemon clean
    fi
    ./gradlew --no-daemon -PrunProguard=false -PcucumberInstrumentation=true assemble${flavor}Debug assemble${flavor}DebugAndroidTest
    buildDebug=$?
}

function installBuild() {
    device=$1
    echo "Installing Builds....."
    flavorLowerCase=$(tr "[A-Z]" "[a-z]" <<< "$flavor")

    echo "installing..... project-${flavorLowerCase}-debug.apk"
    adb -s $device install project/build/outputs/apk/${flavorLowerCase}/debug/project-${flavorLowerCase}-debug.apk
    installDebug=$?

    if [[ ($installDebug -ne 0) ]]; then
        echo "File, project-${flavorLowerCase}-debug.apk, not found"
        exit 1
    fi

    echo "installing..... project-${flavorLowerCase}-debug-androidTest.apk"
    adb -s $device install project/build/outputs/apk/androidTest/${flavorLowerCase}/debug/project-${flavorLowerCase}-debug-androidTest.apk
    installAndroidTest=$?

    if [[ ($installAndroidTest -ne 0) ]]; then
        echo "File, project-${flavorLowerCase}-debug-androidTest.apk, not found"
        exit 1
    fi

    adb -s $device shell pm grant "${packageName}.debug" android.permission.ACCESS_FINE_LOCATION
}

function uninstallBuild() {
    device=$1
    echo "Uninstall Builds.."
    # unistall old apks
    adb -s $device uninstall ${packageName}.debug
    echo "Uninstalled ${packageName}.debug"
    adb -s $device uninstall "com.expedia.bookings.test"
    echo "Uninstalled com.expedia.bookings.test"
}

function runCucumberTests() {
    device=$1
    tagsPassed=$2
    echo "Running Cucumber Tests"
    if [ -n "$tagsPassed" ]; then
        tagsPassed="-e tags \"${tagsPassed}\""
    fi
    adb -s $device shell input keyevent KEYCODE_WAKEUP
    echo adb -s $device shell am instrument -w -r -e debug ${debug} ${tagsPassed} com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner
    adb -s $device shell am instrument -w -r -e debug ${debug} ${tagsPassed} com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner
    runCucumberTests=$?
}

function publishHTMLReport() {
    device=$1
    localtag=$2
    echo "creating report " ${localtag}
    #mkdir project/build/outputs/$device
    mkdir project/build/outputs/${localtag}
    cd project/build/outputs/${localtag}
    adb -s $device pull /data/local/tmp/cucumber-htmlreport
    adb -s $device shell "rm -R /sdcard/cucumber-images"
    adb -s $device shell "run-as ${packageName}.debug cp -R /data/data/${packageName}.debug/files/cucumber-images /sdcard"
    adb -s $device pull /sdcard/cucumber-images
    cd ../../../..
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

function devices() {
    adb devices | tail -n +2 | cut -sf 1
}

function runTestsOnDevice() {
    device=$1
    tags=$2
    tags=$(echo ${tags} | sed 's/,/ /g')
    read -a tagsArrLocal <<<$tags
    for (( i=0; i<${#tagsArrLocal[@]}; i++ )) ; do
        tagSingle=${tagsArrLocal[i]}
        removeDummyFilesOnDevice $device
        createDummyFilesOnDevice $device
        echo "tag trigerred " ${tagSingle}
        runCucumberTests $device $tagSingle
        publishHTMLReport $device $tagSingle
        sleep 10
    done

}

function distributingTagsOverDevices() {
    tags=$1
    numberOfDevices=$2
    #Replacing comma with spaces
    tags=$(echo ${tags} | sed 's/,/ /g')
    read -a tagsArr <<<$tags
    tagsCount=${#tagsArr[@]}

    if (("$numberOfDevices" > "$tagsCount")) ; then
        #Distribute each tag per device
        for (( i=0; i<${tagsCount}; i++ )) ; do
            tagsForEachDeviceArr[i]=${tagsArr[$i]}
        done
    else
        #Distribute tags over device
        #for example: 2 devices, 5 tags. On 1st Device - Tag1,Tag3,Tag5 will run and on 2nd Device - Tag2,Tag4 will run
        for (( i=0; i<${tagsCount}; i++ )) ; do
            index=$((i%${numberOfDevices}))
            newTagStr="${tagsForEachDeviceArr[index]} ${tagsArr[$i]}"
            tagsForEachDeviceArr[index]=${newTagStr}
        done
    fi

    for (( i=0; i<${#tagsForEachDeviceArr[@]}; i++ )) ; do
        echo ${tagsForEachDeviceArr[i]}
    done
}

function archiveTagFailuresAndRemoveReport() {
    currentRunTags=$1
    currentRun=$2
    echo "Removing Report and ${currentRunTags} folders"

    rm $parentDir"/errorRecordFile.txt"
    rm $parentDir"/UITestReport.html"
    cp $failedTagsFile "$failureArchive/failedTagsFile_run${currentRun}.txt"
    echo "" > $failedTagsFile

    currentRunTags=$(echo ${currentRunTags} | sed 's/,/ /g')
    read -a currentRunTagsArr <<<$currentRunTags
    for (( i=0; i<${#currentRunTagsArr[@]}; i++ )) ; do
        echo "Move $parentDir/${currentRunTagsArr[i]} to $parentDir/Archive/${currentRunTagsArr[i]}_run$currentRun"
        mv "$parentDir/${currentRunTagsArr[i]}" "$failureArchive/${currentRunTagsArr[i]}_run$currentRun"
    done
    sleep 1
}

deviceIdentifierArr=()
devicesCount=0

for DEVICE in $(devices) ; do
    deviceIdentifierArr[devicesCount]=$DEVICE
    devicesCount=$((${devicesCount}+1))
done

if (("$devicesCount" == 0)) ; then
  echo "No devices are connected"
  exit 1
fi

#Building Debug and Android Test Debug
build

#install
for DEVICE in "${deviceIdentifierArr[@]}" ; do
    # Need to re-enable data/wifi on devices, which used to run espresso tests in addition to cucumber.
    adb -s ${DEVICE} shell svc data enable
    adb -s ${DEVICE} shell svc wifi enable

    uninstallBuild ${DEVICE}
    installBuild ${DEVICE} &
done

wait

runTags=$tags
initialTags=$tags
touch $failedTagsFile
mkdir $failureArchive

for (( run=0; ${run}<=${maxReRuns}; run++ )) ; do
    tagsForEachDeviceArr=()
    if [ ! -z $(cat $failedTagsFile) ]; then
        runTags=$(cat $failedTagsFile)
        archiveTagFailuresAndRemoveReport $runTags $(($run-1))
    fi

    wait

    #Distribute tags over devices to run in parallel
    distributingTagsOverDevices ${runTags} ${devicesCount}
    for (( i=0; i<${#tagsForEachDeviceArr[@]}; i++ )) ; do
        #Trimming first character(+) and replacing space with comma(,)
        tagsToRun=$(echo ${tagsForEachDeviceArr[i]} | sed 's/ /,/g')
        echo "Trigerring on device" ${deviceIdentifierArr[i]} "with tags" $tagsToRun
        runOnDevicesStr+=" "${deviceIdentifierArr[i]}

        runTestsOnDevice ${deviceIdentifierArr[i]} $tagsToRun &
        echo "Trigerred"
    done

    wait

    python jenkins/generate_cucumber_report.py $initialTags

    wait

    if [ -z $(cat $failedTagsFile) ]; then
        break
    fi
done

# If errorRecordFile is present, output the error and mark the build fail
if [ -f project/build/outputs/errorRecordFile.txt ]
    then
        echo "\nList Of Failed Tests"
        awk '{gsub(/\<br>/,"\n")}1'  project/build/outputs/errorRecordFile.txt
        rm -fr project/build/outputs/mapping
        rm -fr project/build/outputs/apk
        exit 1
    else
        echo "All test cases passed." > project/build/outputs/errorRecordFile.txt
fi
