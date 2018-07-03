#!/bin/bash
# This script will install an Android Nougat emulator image 'from scratch' on a clean MacOS device.
# It can be used for automated testing or for user support to have access to Android emulators.
# A shortcut to launch the emulator will be placed on the Desktop.
# The emulator image has the Google Play store, so any production app can easily be installed.
# Test/debug apks can be dragged onto the emulator to install. 

# When double-clicked as a '.command' file, this runs as if it's in the ~/ (User's home) directory.
mkdir AndroidEmulator
cd AndroidEmulator

# Download the Android SDK (don't need the whole Android Studio)
wget https://dl.google.com/android/repository/sdk-tools-darwin-4333796.zip
unzip sdk-tools-darwin-4333796.zip
rm sdk-tools-darwin-4333796.zip

# Accept all licenses so we aren't manually prompted later
yes | ./tools/bin/sdkmanager --licenses

# Download all required components from sdkmanager
./tools/bin/sdkmanager "platform-tools" "emulator" "system-images;android-25;google_apis_playstore;x86"

# Create a AVD image simulating a Nexus 5X with Nougat
./tools/bin/avdmanager create avd -n nougat -k "system-images;android-25;google_apis_playstore;x86" -d "Nexus 5X"

# This directory needs to exist but isn't automatically created
mkdir platforms

# The default emulator config is missing a lot of important settings like partition size, hardware keyboard support, and haxm support.
# Overwrite the config ini with these good values.
echo "AvdId=Nexus_5X_API_25_Nougat
PlayStore.enabled=true
abi.type=x86
avd.ini.displayname=Nexus 5X API 25 Nougat
avd.ini.encoding=UTF-8
disk.systemPartition.size = 2g
disk.dataPartition.size = 2g
hw.accelerometer=yes
hw.audioInput=yes
hw.battery=yes
hw.camera.back=emulated
hw.camera.front=emulated
hw.cpu.arch=x86
hw.cpu.ncore=4
hw.dPad=no
hw.device.manufacturer=Google
hw.device.name=Nexus 5X
hw.gps=yes
hw.gpu.enabled=yes
hw.gpu.mode=auto
hw.initialOrientation=Portrait
hw.keyboard=yes
hw.lcd.density=420
hw.lcd.height=1920
hw.lcd.width=1080
hw.mainKeys=no
hw.ramSize=1536
hw.sdCard=yes
hw.sensors.orientation=yes
hw.sensors.proximity=yes
hw.trackBall=no
image.sysdir.1=system-images/android-25/google_apis_playstore/x86/
runtime.network.latency=none
runtime.network.speed=full
sdcard.size=100M
showDeviceFrame=no
skin.dynamic=yes
skin.name=1080x1920
skin.path=_no_skin
skin.path.backup=_no_skin
tag.display=Google Play
tag.id=google_apis_playstore
vm.heapSize=256
" > ~/.android/avd/nougat.avd/config.ini

# Create a shortcut on the Desktop to launch the emulator
echo "#!/bin/bash
$PWD/emulator/emulator -avd nougat
" > ~/Desktop/Android_Nougat_Emulator.command
chmod +x ~/Desktop/Android_Nougat_Emulator.command

