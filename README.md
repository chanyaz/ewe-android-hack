Welcome
=======

This is the code base that builds:

 * [Expedia Hotels & Flights Android](https://play.google.com/store/apps/details?id=com.expedia.bookings)
 * [Hôtel Voyages-sncf](https://play.google.com/store/apps/details?id=com.expedia.bookings.vsc)
 * [Travelocity Hotels & Flights](https://play.google.com/store/apps/details?id=com.travelocity.android)
 * [AirAsiaGo](https://play.google.com/store/apps/details?id=com.airasiago.android)

Building
========

1. Install `JDK7` or `JDK8`.

2. Download [Android Studio](http://developer.android.com/sdk/installing/studio.html).

3. Clone the `ExpediaInc/ewe-android-eb` repository and run `git submodule init`
   and `git submodule update`.

4. Run ./snap/build.sh to download all the required SDK components and ensure
   the command line build is working.

5. Open Android Studio and open the project via Import, selecting the root `build.gradle` file from the repository.

6. Select the build variant you'd like to build, e.g. `expediaDebug`, `travelocityDebug`, `airAsiaGoDebug`. You can find
the Build Variants section via Help in the system bar.

7. You should now be able to build the APK from Android Studio and install on a device or emulator.

Contributing
============

Do not commit directly to master unless you want to get yelled at. Submit a
pull request from a branch created directly under this repository or from your
fork from the following naming conventions:

- Stories - `s/`
  - Choose a good name so we can tell what the feature does. Try to be concise.
  - eg. `s/air-attach-pricing`
- Defect(s) - `d/`
  - Choose a good name so we can tell what the defect fixed. Try to be concise.
  - It is also fine to fix multiple defects as long as the commits are atomic and
    have good information in them
  - eg. `d/fix-air-attach-pricing`
  - eg. `d/fix-various-flight-list-defects`
- Improvements - `i/`
  - eg. `i/resource-cleanup`
  - eg. `i/port-unittests-to-java`
- Release branch - `r/`
  - Choose a good name so we can tell what the release is. Try to be concise.
  - We use these right before a release to insulate it from other changes we
    want to merge in and test.
  - After a release we tag it, rebase master and finally merge back into master
    with a pull request
  - eg. `r/expedia-4.1.0`
  - eg. `r/airasiago-1.0.0`
- Work in progress/experiments - `w/`
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

Jvm Unit Tests
==============

These live in `lib/ExpediaBookings/` and have no Android dependencies so we can
run them very quickly just on the `JVM`.

````shell
./buildbot/jvm_unittests.sh
````

Android Tests
==================

Make sure the device(s) that will be running the tests has turned off animations:
Window Animation, Transition Animation, Animator Duration.

````shell
BUILDER_NAME=expedia # or another product flavor
./buildbot/build.sh

# Android Unit tests
./buildbot/unittest.sh
open spoon/unit/index.html

# Happy path tests
./buildbot/happypath.sh
open spoon/happy/index.html

# Subset of the regression tests we want to run on every checkin
./buildbot/regression.sh
open spoon/regression/index.html
````

Creating Feature Builds
=======================

Suppose you want to create a build so people can see what you've done. You can
actually give it a unique package name and upload the `apk` to Crashlytics.
`UNIQUE_FEATURE_NAME` must be a valid Java package name part because the
resulting `applicationId` becomes `com.expedia.bookings.feature.UNIQUE_FEATURE_NAME`.

````shell
    $ ./gradlew -Pid="feature.UNIQUE_FEATURE_NAME" assembleExpediaDebug
    $ ./gradlew -Pid="feature.UNIQUE_FEATURE_NAME" crashlyticsUploadDistributionExpediaDebug
````

