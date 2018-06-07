#!/bin/bash -l

#############
# Sample Launch Parameters. It is ok to mix and match params.
#
# jenkins/CucumberLauncher.sh
# jenkins/CucumberLauncher.sh -maxreruns 0
# jenkins/CucumberLauncher.sh -noclean true
# jenkins/CucumberLauncher.sh -debug true
# jenkins/CucumberLauncher.sh -flavor Expedia
# jenkins/CucumberLauncher.sh -package com.expedia.bookings
# jenkins/CucumberLauncher.sh -featuresDir Features/Hotels
# jenkins/CucumberLauncher.sh -tags ~@skip       #### Please note that tags parameter currently only
# jenkins/CucumberLauncher.sh -tags @wip,@mytag  #### supports a single tag for exclusion and multiple
#                                                #### tags for inclusion. Complex combos TBD


#get command line arguments, which we re passed during launch
while echo $1 | grep -q ^-; do
    eval $( echo $1 | sed 's/^-//' )=$2
    shift
    shift
done

# Defaults
declare -r defaultDebug="false" # Making debug by default as false
declare -r defaultReRuns=2 # integer, 0 is the first run, 1 is the rerun, etc...
declare -r defaultTags="\"~@skip\""
declare -r defaultFlavor="Expedia"
declare -r defaultPackageName="com.expedia.bookings"
declare -r defaultNoClean="false"
declare -r baseFeaturePath="project/src/androidTest/assets/"
declare -r defaultfeaturesDir="Features"
declare featureList=[]
declare featureListJSON
declare tagListJSON

flavor=${flavor:-$defaultFlavor}
flavorJSON="\"flavor\":\"$flavor\""
packageName=${package:-$defaultPackageName}
packageJSON="\"package\":\"$packageName\""
tags=$tags
debug=${debug:-$defaultDebug}
debugJSON="\"debug\":\"$debug\""
noclean=${noclean:-$defaultNoClean}
maxReRuns=${maxreruns:-$defaultReRuns}
featuresDir=${featuresDir:-$defaultfeaturesDir}

parentDir="project/build/outputs"
reportDir="$parentDir/report"
failureArchive="$parentDir/report/FailureArchive"

if [ -z "$tags" ]; then
    tagListJSON="\"tags\":[$defaultTags]"
else
    tagListJSON="\"tags\":[$defaultTags,\"$tags\"]"
fi

function getDevices() {
    echo $(adb devices | tail -n +2 | sed '/^\s*$/d' | cut -sf 1)
}

function checkForConnectedDevices() {
    arrDevices=($(getDevices))
    if ((${#arrDevices[@]} == 0)); then
      echo "No devices are connected"
      exit 1
    fi
}

function checkDependencies() {
    which rvm
    if (($? != 0)); then echo "Ruby Version Manager (RVM) Not Found"; exit 1; fi
    which ruby
    if (($? != 0)); then echo "Ruby Not Found"; exit 1; fi
    which python3
    if (($? != 0)); then echo "Python3 Not Found"; exit 1; fi
    rvm default
    if (($? != 0)); then echo "Unable to set default ruby"; exit 1; fi

    gem install report_builder
}

function clean() {
    if [ "$noclean" != "true" ]; then
        ./gradlew --no-daemon clean
    else
        rm -rf $reportDir
    fi
    return=$?
}

function build() {
    echo "Building assemble${flavor}Debug....."
    ./gradlew --no-daemon -PrunProguard=false -PcucumberInstrumentation=true assemble${flavor}Debug assemble${flavor}DebugAndroidTest
    return=$?
}

function uninstallBuild() {
    arrDevices=($(getDevices))
    echo "Uninstallling Old APKs..."

    for device in ${arrDevices[@]}; do
        # unistall old apks
        echo "$device uninstall ${packageName}.debug"
        echo "$device uninstall ${packageName}.test"
        adb -s $device uninstall ${packageName}.debug &&
        adb -s $device uninstall ${packageName}.test &
    done

    wait
}

function installBuild() {
    arrDevices=($(getDevices))
    echo "Installing New APKs..."
    flavorLowerCase=$(tr "[A-Z]" "[a-z]" <<< "$flavor")

    for device in ${arrDevices[@]}; do
        echo "$device install ${packageName}.debug"
        echo "$device install ${packageName}.test"
        adb -s $device install project/build/outputs/apk/${flavorLowerCase}/debug/project-${flavorLowerCase}-debug.apk &&
        adb -s $device install project/build/outputs/apk/androidTest/${flavorLowerCase}/debug/project-${flavorLowerCase}-debug-androidTest.apk &
    done

    wait
}

function grantPermissions() {
    arrDevices=($(getDevices))

    for device in ${arrDevices[@]}; do
        adb -s $device shell pm grant ${packageName}.debug android.permission.ACCESS_FINE_LOCATION

        #re-enable services, in case they are still disabled.
        adb -s $device shell svc data enable
        adb -s $device shell svc wifi enable
    done
}

function populateFeatureFileList() {
    featurePath="$baseFeaturePath$featuresDir"
    featureList=($(find $featurePath -type f))

    for feature in ${featureList[@]}; do
        #Strip Away basePath from Feature Path
        addFeature="${feature/#$baseFeaturePath}"

        #Add a feature to JSON
        if [[ -z $featureListJSON ]]; then
            featureListJSON="\"$addFeature\""
        else
            featureListJSON="$featureListJSON,\"$addFeature\""
        fi
    done

    featureListJSON="\"features\":[$featureListJSON]"
}

function prepareRerun() {
    runInstance=$1
    featureListJSON="" #Set to blank, so that it could be reused

    # Loop Through Feature List
    # Move Failed Features to archive and add them to rerun
    for feature in ${featureList[@]} ; do
        executedFeature="${feature/#$baseFeaturePath}"
        jsonFile="$reportDir/$executedFeature/cucumber-htmlreport/cucumber.json"

        #if File has "failed" or
        #   File does not have "passed" and with either of the above being true
        #   File contains "elements": key <- this is crucial for tagging to work
        # then add feature to be re-run
        if ( grep -q "\"status\": \"failed\"" $jsonFile ||
        ! grep -q "\"status\": \"passed\"" $jsonFile ) &&
        grep -q "\"elements\":" $jsonFile ; then

            mkdir -p "$failureArchive/${executedFeature}_Run$((runInstance-1))"
            mv "$reportDir/$executedFeature" "$failureArchive/${executedFeature}_Run$((runInstance-1))"

            if [[ -z $featureListJSON ]]; then
                featureListJSON="\"$executedFeature\""
            else
                featureListJSON="$featureListJSON,\"$executedFeature\""
            fi
        fi
    done
    featureListJSON="\"features\":[$featureListJSON]"
}

function ensureCucumberJsonExists() {
    # Loop Through Feature List
    # Move Failed Features to archive and add them to rerun
    for feature in ${featureList[@]} ; do
        #Strip Away basePath from Feature Path
        executedFeature="${feature/#$baseFeaturePath}"
        jsonFile="$reportDir/$executedFeature/cucumber-htmlreport/cucumber.json"
        #If file is missing, or empty, add a dummy json file to the html report directory
        if [ ! -f "$jsonFile" ] ||
        [ ! -s "$jsonFile" ]; then
            mkdir -p "$reportDir/$executedFeature/cucumber-htmlreport/"
            fileName="$jsonFile"
            featureName=(${executedFeature##*/}) #Get Only Feature Name
            python jenkins/CucumberCreateEmptyJson.py $fileName $featureName
        fi
    done
}

function runBalancedWithRerun() {
    for runInstance in $(seq 0 $maxReRuns); do
        #ReRun Logic
        if [ $runInstance -ne 0 ]; then
            prepareRerun $runInstance
        fi

        if [ $featureListJSON == "\"features\":[]" ]; then
            #Exit, as there are no more failed tests to rerun
            return 0
        fi

        #Execute Tests
        echo -e "\n###### Test Run Instance $runInstance"
        pythonParam="{$flavorJSON,$packageJSON,$debugJSON,$featureListJSON,$tagListJSON}"
        python jenkins/CucumberRunner.py $pythonParam
        ensureCucumberJsonExists
    done
}

function generateReport() {
    #Copy All JSON files into the report folder
    for feature in ${featureList[@]}; do
        executedFeature="${feature/#$baseFeaturePath}"
        sourceFile="$reportDir/$executedFeature/cucumber-htmlreport/cucumber.json"
        destinationFileName="${executedFeature//\//_}"
        destinationFileName="${destinationFileName//.feature/.json}"
        destinationFile="$reportDir/$destinationFileName"

        #if json file does not contain "elements" section, then it was skipped due to tagging.
        #we shouldn't use it to generate results
        if grep -q "\"elements\":" $sourceFile ; then
            cp $sourceFile $destinationFile
        fi
    done

    #Generate Report
    report_builder -s "`pwd`/$reportDir" -o "`pwd`/$reportDir/UITestReport"
}

function finalRunStatus() {
    jsonFiles=($(find "`pwd`/$reportDir" -type f -name "*.json"))

    for file in ${jsonFiles[@]}; do
        if grep -q "\"status\": \"failed\"" $file; then
            #if status:failed has been found, mark run as failed
            return 1
        fi
    done
    return 0
}

echo ''
echo "######################################################################"
echo "###### Running Cucumber UI tests with the following parameters:"
echo "###### Flavor:      $flavor"
echo "###### Package:     $packageName"
echo "###### Tags:        $tags"
echo "###### Feature Dir: $featuresDir"
echo "###### Max Reruns:  $maxReRuns"
echo "###### No Clean:    $noclean"
echo "###### Debug:       $debug"
echo "######################################################################"
echo ''

#Building Debug and Android Test Debug
checkForConnectedDevices
checkDependencies
clean
if (($? != 0)); then exit 1; fi
build
if (($? != 0)); then exit 1; fi
uninstallBuild
installBuild
grantPermissions
populateFeatureFileList
runBalancedWithRerun
generateReport
finalRunStatus
exit $?
