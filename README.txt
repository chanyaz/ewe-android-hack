Welcome to the Android Expedia codebase. Here is some information about importing the project
into Android Studio such that you can develop, compile, and install the APK. We'll try and keep
this is up-to-date as possible. -Brad


/////////////////////////
Building

1. Download Android Studio from the following link: http://developer.android.com/sdk/installing/studio.html

2. Ensure you have the latest SDK components to build our application. Specifically, you should look at the
compileSdkVersion and buildToolsVersion from project/build.gradle and update your SDK to have those versions.
Update from the Android SDK manager, <SDK-DIR>/tools/android

3. Open Android Studio and update to the latest version via Android Studio -> Check for updates...

4. Import the project via Import, selecting ExpediaBookings/build.gradle file to import.

5. Select the build variant you'd like to build, e.g. ExpediaDebug, TravelocityDebug, VSCDebug. You can find
the Build Variants section via Help in the system bar.

6. You should now be able to build the APK and then install on a device or emulator.


/////////////////////////
Code Style

1. If you're committing code, use the Mobiata Java codestyle which is found in common/MobiataIntellij.xml. You
must copy this file in to your AndroidStudio codestyle preferences folder, which on a Mac will most likely be
found in your home directory's 'Library/Preferences' folder:

$ cp common/MobiataIntellij.xml ~/Library/Preferences/AndroidStudioPreview/codestyles/

Restart Android Studio and you should be able to select this codestyle from Preferences -> Codestyle -> Java -> Scheme.

2. XML style can be set via Preferences -> Code Style -> XML - > Set from ... -> Predefined Style -> Android. This
does not seem to persist across Android Studio restarts so if you notice your XML formatting is all off, be sure to
do this step again.


/////////////////////////
Unit Tests

$ ./gradlew assembleExpediaDebug assembleExpediaDebugTest
$ ./buildbot/unittest.sh
$ open spoon/unit/index.html


/////////////////////////
Happy Path Tests

1. Happy path tests are run against the ExpediaAutomation product flavor. Make sure the device that will be running
the tests has turned off animations: Window Animation, Transition Animation, Animator Duration. Then install and run:

$ ./gradlew assembleExpediaAutomationDebug assembleExpediaAutomationDebugTest
$ ./buildbot/happypath.sh
$ open spoon/happy/index.html


