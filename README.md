Welcome
=======

This is the code base that builds:

 * [Expedia Hotels & Flights Android](https://play.google.com/store/apps/details?id=com.expedia.bookings)
 * [Hôtel Voyages-sncf](https://play.google.com/store/apps/details?id=com.expedia.bookings.vsc)
 * [Travelocity Hotels & Flights](https://play.google.com/store/apps/details?id=com.travelocity.android)
 * [AirAsiaGo](https://play.google.com/store/apps/details?id=com.airasiago.android)
 * [Wotif](https://play.google.com/store/apps/details?id=com.wotif.android)
 * [Lastminute AU & NZ](https://play.google.com/store/apps/details?id=com.lastminute.android)
 * [Expedia for Samsung](#)

Building
========

1. Install `JDK7` or `JDK8`.

2. Clone the `ExpediaInc/ewe-android-eb` repository

3. Navigate to the root of the `ewe-android-eb` repository.

4. Run `git submodule init` and `git submodule update`.

5. Run `./gradlew assembleExpediaDebug`

Once you have that working, try importing the project into **AndroidStudio** and
building from there.

Contributing
============

Please see our expectations on [Contributing](https://github.com/ExpediaInc/ewe-android-eb/wiki/Contributing)

Code Style
==========

Our Java and XML files follow a specific code style. To import the Mobiata code style first close Android Studio, then:

````shell
./tools/sync-code-style.sh
````

Restart Android Studio and select `MobiataIntellij` via `Preferences -> Code Style -> Java -> Scheme`.

Set XML style via `Preferences -> Code Style -> XML - > Set from ... -> Predefined Style -> Android`.

Unit Tests
==============

These live in `lib/ExpediaBookings/` and have no Android dependencies so we can
run them very quickly just on the `JVM`.

There are also android unit tests and robolectric tests living in the android project


````shell
./gradlew :lib:ExpediaBookings:test :lib:mocked:mocke3:test :project:testExpediaDebug
````

Android Tests
==================

Make sure the device(s) that will be running the tests has turned off animations:
Window Animation, Transition Animation, Animator Duration.

````shell
BUILDER_NAME=expedia # or another product flavor
./buildbot/build.sh

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

Creating Usability Builds
=======================

Passing in `-Pid="usability` while assembling a release build triggers special functionality
to always suppress final bookings for all lines of business. This is behavior deemed important
for third-party usability testing through www.usertesting.com. We will likely share these builds
via Crashlytics share link.

````shell
    $ ./gradlew -Pid="usability" assembleExpediaRelease
````
