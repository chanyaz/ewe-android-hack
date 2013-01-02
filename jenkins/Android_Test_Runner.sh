#
# run test, save output to output/
# see slave's /Users/jenkins/settings_jenkins.sh for settings

appName=$1
testName=$2

source ~/settings_jenkins.sh
source ~/android_devices.sh

#start the emulator
$start_emulator &

#install new APKs - emulator

################### SET UP ###################

# Get fresh APKs on to the emulator
$adbe wait-for-device uninstall $appName
$adbe uninstall $appName.test
$adbe install "$apk_dir"/ExpediaBookings-debug.apk
$adbe install "$apk_dir"/ExpediaBookingsTest-debug.apk

#start logcat - emulator
$adbe logcat > logcat_emulator.txt &
$adbe logcat -s ExpediaBookings FlightsTest TestUtils > logcat_filtered_emulator.txt &

# prep all devices

for devs in "${device[@]}"; do
#uninstall both binaries
   adb -s "${devs}" uninstall $appName 
   adb -s "${devs}" uninstall $appName.test 
#install new ones
   adb -s "${devs}" install "$apk_dir"/ExpediaBookings-debug.apk
   adb -s "${devs}" install "$apk_dir"/ExpediaBookingsTest-debug.apk
   done

# unlock emulator
$adbe wait-for-device shell input keyevent 82

# Create array for failed devices' names
declare -a failed_devices

################### DEVICE TEST ###################

emailSwitch=0
# run the test on all devices concurrently
iter=0
for devs in ${device[@]}; do
   logName="deviceLog_${device_names[$iter]}.txt"
    #logcat time   
    adb -s ${devs} logcat > logcat_${device_names[$iter]}.txt &
    adb -s ${devs} logcat -s ExpediaBookings FlightsTest TestUtils > logcat_filtered_${device_names[$iter]}.txt &

    # Execute Test
    adb -s "${devs}" shell am instrument -w -r -e class $testName -e debug false $appName.test/com.zutubi.android.junitreport.JUnitReportTestRunner > $logName 

   #if the test was a failure, mark job as failure 
   #and add device name to list of failed devices

   if ! /Users/jenkins/bin/parse_junit_output.sh $logName
   then emailSwitch=1; fi
   failed_devices+=( "${device_names[$iter]}" )

   iter=$(( $iter + 1 ))   
   done

############

# run on the emulator
# this will actually determine pass/fail for now 
$adbe shell am instrument -w -r -e package $testName -e debug false $appName/com.zutubi.android.junitreport.JUnitReportTestRunner > emulatorLog.txt

# look for failures
if ! /Users/jenkins/bin/parse_junit_output.sh emulatorLog.txt 
then emailSwitch=1; fi 

# run the hprof dump test to dump hprof to sdcard for archive
#uncomment the next lines eventually.
#$adbe shell am instrument -w -r -e class com.expedia.bookings.test.utils.HprofDump -e debug false $appName/com.zutubi.android.junitreport.JUnitReportTestRunner > hprofOutput.txt

# pull the report XML file from the custom test runner
# uncomment next line
$adbe pull /data/data/com.expedia.bookings/files/junit-report.xml junit-report.xml

# pull and convert the hprof dump for archive
# hprof is used for memory analysis
# uncomment the two below, eventually
#$adbe pull /sdcard/dump.hprof
#$ANDROID_HOME/tools/hprof-conv dump.hprof heap-dump.hprof

if [ "$emailSwitch" -eq "1" ]; then echo "PYTHON SCRIPT TIME! We failed on: ${failed_devices[@]}" && exit -1; fi
 
