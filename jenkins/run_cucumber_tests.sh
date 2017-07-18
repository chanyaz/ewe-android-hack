#!/usr/bin/env bash
#!/bin/ksh

flavor=$1
packageName=$2
tags=$3

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


tagsForEachDeviceArr=()
deviceIdentifierArr=()
deviceStatusArr=()
tagStatusArr=()
devicesCount=0
firstIdleDeviceIndex=-1
firstAvailableTagIndex=-1

echo "Running Cucumber UI tests on $flavor"

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
    removeDummyFiles=$?
}

function build() {
    echo "Building assemble${flavor}Debug....."
    ./gradlew --no-daemon clean
    ./gradlew --no-daemon -PrunProguard=false -PcucumberInstrumentation=true assemble${flavor}Debug assemble${flavor}DebugAndroidTest
    buildDebug=$?
}

function installBuild() {
    device=$1
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
    adb -s $device shell pm grant "${packageName}.debug" android.permission.WRITE_EXTERNAL_STORAGE

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
    if [ -n "$tags" ]; then
        tagsPassed="-e tags \"${tags}\""
    fi
    echo adb -s $device shell am instrument -w -r -e debug false ${tagsPassed} com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner
    adb -s $device shell am instrument -w -r -e debug false ${tagsPassed} com.expedia.bookings.test/com.expedia.bookings.test.CucumberInstrumentationRunner
    runCucumberTests=$?
}

function publishHTMLReport() {
    device=$1
    mkdir project/build/outputs/$device
    cd project/build/outputs/$device
    adb -s $device pull /data/local/tmp/cucumber-htmlreport
    adb -s $device shell "rm -R /sdcard/cucumber-images"
    adb -s $device shell "run-as ${packageName}.debug cp -R /data/data/${packageName}.debug/files/cucumber-images /sdcard"
    adb -s $device pull /sdcard/cucumber-images
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

function isRunning() {
    device=$1
    adb -s $device shell ps com.expedia.bookings.debug | tail -n +2
}

function runTestsOnDevice() {
    device=$1
    tags=$2
    runningDeviceIndex=$3
    echo "Running device " ${runningDeviceIndex}
    sleep 2
    echo "Completed Run on device " ${runningDeviceIndex}

    #uninstall existing build
    #uninstallBuild $device
    #remove dummy files if already present
    #removeDummyFilesOnDevice $device
    #createDummyFilesOnDevice $device
    #installBuild $device
    #runCucumberTests $device $tags
    #publishHTMLReport $device
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

function setFirstIdleDeviceIndex() {
    firstIdleDeviceIndex=-1
    echo "Number of devices " ${#deviceStatusArr[@]}
    for (( i=0; i<${#deviceStatusArr[@]}; i++ )) ; do
        var=${deviceStatusArr[i]}
        echo "device status at ${i}"  ${deviceStatusArr[i]}
        if [ "IDLE" = "$var" ] ; then
            echo "found device index" ${i}
            firstIdleDeviceIndex=$i
            break
        fi
    done
}

function setFirstAvailableTagIndex() {
    firstIdleTagIndex=-1
    for (( i=0; i<${#tagsArr[@]}; i++ )) ; do
        var=${tagStatusArr[i]}
        if [ "PENDING" = "$var" ] ; then
            echo "found tag index" ${i}
            firstAvailableTagIndex=$i
            break
        fi
    done
}

function refreshDeviceStatus() {
local index=0
for DEVICE in $(devices) ; do
    processArr=$(isRunning $DEVICE)
    echo "process arr " ${#processArr[@]}
    if [ ${#processArr[@]} > 0 ] ; then
        echo "setting device status"
        deviceStatusArr[index]="IDLE"
    fi
    index=$((${index}+1))
done
}


for DEVICE in $(devices) ; do
    deviceIdentifierArr[devicesCount]=$DEVICE
    deviceStatusArr[devicesCount]="IDLE"
    devicesCount=$((${devicesCount}+1))
done

if (("$devicesCount" == 0)) ; then
  echo "No devices are connected"
  exit 1
fi

#Cumputing pending tags count
tags=$(echo ${tags} | sed 's/,/ /g')
read -a tagsArr <<<$tags
pendingTagsCount=${#tagsArr[@]}

for (( i=0; i<${#tagsArr[@]}; i++ )) ; do
    tagStatusArr[i]="PENDING"
done

#Building Debug and Android Test Debug
#build

for (( i=0; i<${#deviceIdentifierArr[@]}; i++ )) ; do
    #installBuild ${deviceIdentifierArr[i]}
    echo "install builld on device " ${deviceIdentifierArr[i]}
done

while [ $pendingTagsCount -gt 0 ]
    do
        echo "pending tags count " ${pendingTagsCount}
        setFirstIdleDeviceIndex
        setFirstAvailableTagIndex
        echo "idle device index " ${firstIdleDeviceIndex}
        echo "idle tag index " ${firstAvailableTagIndex}
        if [ $firstIdleDeviceIndex != -1 -a $firstAvailableTagIndex != -1 ] ; then
            echo "inside"
            echo "Allocated device index " $firstIdleDeviceIndex
            deviceStatusArr[$firstIdleDeviceIndex]="BUSY"
            tagStatusArr[firstAvailableTagIndex]="DONE"
            runTestsOnDevice ${deviceIdentifierArr[firstIdleDeviceIndex]} ${tagsArr[firstAvailableTagIndex]} ${firstIdleDeviceIndex} &
            pendingTagsCount=`expr $pendingTagsCount - 1`
        fi
        refreshDeviceStatus
        sleep 1
    done






#Distribute tags over devices to run in parallel
#distributingTagsOverDevices ${tags} ${devicesCount}




echo "print" $firstIdleDeviceIndex
#for (( i=0; i<${#tagsForEachDeviceArr[@]}; i++ )) ; do
    #Trimming first character(+) and replacing space with comma(,)
#    tagsToRun=$(echo ${tagsForEachDeviceArr[i]} | sed 's/ /,/g')
#    echo "Trigerring on device" ${deviceIdentifierArr[i]} "with tags" $tagsToRun
#    runOnDevicesStr+=" "${deviceIdentifierArr[i]}
#    runTestsOnDevice ${deviceIdentifierArr[i]} $tagsToRun &
#    echo "Trigerred"
#done

wait
echo "Done"

#Get list of devices on which automation was run, runOnDevicesStr is comma separated list of device identifier
#runOnDevicesStr=$(echo ${runOnDevicesStr} | sed 's/ /,/g')
#python jenkins/generate_cucumber_report.py ${runOnDevicesStr}
