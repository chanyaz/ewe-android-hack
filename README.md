Welcome
=======

This is the codebase that builds:

 * [Expedia Hotels & Flights Android](https://play.google.com/store/apps/details?id=com.expedia.bookings)
 * [Hôtel Voyages-sncf](https://play.google.com/store/apps/details?id=com.expedia.bookings.vsc)
 * [Travelocity Hotels & Flights](https://play.google.com/store/apps/details?id=com.travelocity.android)

Building
========

1. Download [Android Studio](http://developer.android.com/sdk/installing/studio.html).

2. Ensure you have the latest SDK components to build our application. Specifically, you should look at the
compileSdkVersion and buildToolsVersion from project/build.gradle and update your SDK to have those versions.
Update from the Android SDK manager, `<SDK-DIR>/tools/android`. Additionally, we require modules from the Extras
section: *Android Support Repository* and *Google Repository*.

3. Open Android Studio and update to the latest version via `Android Studio -> Check for updates...`.

4. Import the project via Import, selecting `ExpediaBookings/build.gradle` file to import.

5. Select the build variant you'd like to build, e.g. `expediaDebug`, `travelocityDebug`, `airAsiaGoDebug`. You can find
the Build Variants section via Help in the system bar.

6. You should now be able to build the APK and then install on a device or emulator.

Code Style
==========

If you're committing code, use the Mobiata Java codestyle which is found in `common/MobiataIntellij.xml`. You
must copy this file in to your AndroidStudio codestyle preferences folder, which on a Mac will most likely be
found in your home directory's `Library/Preferences` folder:

    $ cp common/MobiataIntellij.xml ~/Library/Preferences/AndroidStudioPreview/codestyles/

Restart Android Studio and you should be able to select this codestyle from `Preferences -> Codestyle -> Java -> Scheme`.

XML style can be set via `Preferences -> Code Style -> XML - > Set from ... -> Predefined Style -> Android`. This
does not seem to persist across Android Studio restarts so if you notice your XML formatting is all off, be sure to
do this step again.

Unit Tests
==========

    $ ./gradlew assembleExpediaDebug assembleExpediaDebugTest
    $ ./buildbot/unittest.sh
    $ open spoon/unit/index.html

Happy Path Tests
================

Happy path tests are run against the expediaAutomation product flavor. Make sure the device that will be running
the tests has turned off animations: Window Animation, Transition Animation, Animator Duration. Then install and run:

    $ ./gradlew assembleExpediaAutomationDebug assembleExpediaAutomationDebugTest
    $ ./buildbot/happypath.sh
    $ open spoon/happy/index.html


