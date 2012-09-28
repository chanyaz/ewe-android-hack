# Quick script for running adb pull to get screenshots off of Android device/emulator

LOCAL_DIRECTORY='screenshots'
ANDROID_DIRECTORY='sdcard/Robotium-Screenshots'

echo "Moving files from $ANDROID_DIRECTORY TO $LOCAL_DIRECTORY"

if [ ! -d "$DIRECTORY" ]; then
    # Control will enter here if $DIRECTORY doesn't exist.
    mkdir $DIRECTORY
fi

adb shell ls | adb pull $ANDROID_DIRECTORY $DIRECTORY/ | rm $ANDROID_DIRECTORY/* | exit

