Welcome
=======

This is the code base that builds:

 * [Expedia Hotels & Flights Android](https://play.google.com/store/apps/details?id=com.expedia.bookings)
 * [Hôtel Voyages-sncf](https://play.google.com/store/apps/details?id=com.expedia.bookings.vsc)
 * [Travelocity Hotels & Flights](https://play.google.com/store/apps/details?id=com.travelocity.android)
 * [AirAsiaGo](https://play.google.com/store/apps/details?id=com.airasiago.android)

Building
========

1. Download [Android Studio](http://developer.android.com/sdk/installing/studio.html).

2. Ensure you have the latest SDK components to build our application. Specifically, you should look at the
`compileSdkVersion` and `buildToolsVersion` from project/build.gradle and update your SDK to have those versions.
Update from the Android SDK manager, `<SDK-DIR>/tools/android`. Additionally, we require modules from the Extras
section: *Android Support Repository* and *Google Repository*.

3. Open Android Studio and update to the latest version via `Android Studio -> Check for updates...`.

4. Import the project via Import, selecting `ExpediaBookings/build.gradle` file to import.

5. Select the build variant you'd like to build, e.g. `expediaDebug`, `travelocityDebug`, `airAsiaGoDebug`. You can find
the Build Variants section via Help in the system bar.

6. You should now be able to build the APK and then install on a device or emulator.

Contributing
============

Do not commit directly to master unless you want to get yelled at. Submit a
pull request from a branch created directly under this repository or from your
fork from the following naming conventions:

- Feature branch - `f/`
  - Choose a good name so we can tell what the feature does. Try to be concise.
  - eg. `f/air-attach-pricing`
- Defect(s) branch - `d/`
  - Choose a good name so we can tell what the defect fixed. Try to be concise.
  - It is also fine to fix multiple defects as long as the commits are atomic and
    have good information in them
  - eg. `d/fix-air-attach-pricing`
  - eg. `d/fix-various-flight-list-defects`
- Release branch - `r/`
  - Choose a good name so we can tell what the release is. Try to be concise.
  - eg. `r/expedia-4.1.0`
  - eg. `r/airasiago-1.0.0`
- Work in progress/experiment branch - `w/`
  - Choose a good name so we can tell what the experiment entails. Try to be concise.
  - eg. `w/nfc-itin-sharing`

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

    $ ./buildbot/build_tests.sh
    $ ./buildbot/unittest.sh
    $ open spoon/unit/index.html

Happy Path Tests
================

Happy path tests are run against the `expediaAutomation` product flavor. Make sure the device that will be running
the tests has turned off animations: Window Animation, Transition Animation, Animator Duration. Then install and run:

    $ ./buildbot/build_tests.sh
    $ ./buildbot/happypath.sh
    $ open spoon/happy/index.html

Creating Feature Builds
=======================

Suppose you want to create a build so people can see what you've done. You can
actually give it a unique package name and upload the `apk` to Crashlytics.
`UNIQUE_FEATURE_NAME` must be a valid Java package name part because the
resulting `applicationId` becomes `com.expedia.bookings.feature.UNIQUE_FEATURE_NAME`.

    $ ./gradlew -PfeatureName="UNIQUE_FEATURE_NAME" assembleExpediaFeature
    $ ./gradlew -PfeatureName="UNIQUE_FEATURE_NAME" crashlyticsUploadDistributionExpediaFeature

