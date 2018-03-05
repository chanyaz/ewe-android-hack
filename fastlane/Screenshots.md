
### Running screenshots
```
fastlane screenshots
```
Examples of options, which could be added
```
classes:com.expedia.bookings.screengrab.PlayStoreScreenshotSweep
packages:com.expedia.bookings.screengrab
locales:da_DK,en_US
```
Example start command with options
```
fastlane screenshots classes:com.expedia.bookings.screengrab.PlayStoreScreenshotSweep locales:da_DK,en_US
```

### Debugging
Prerequisites:
```
./gradlew assembleExpediaDebug assembleExpediaDebugAndroidTest -PSCREENSHOT_BUILD
adb install -r project/build/outputs/apk/expedia/debug/project-expedia-debug.apk
adb shell settings put secure location_providers_allowed -network
adb shell settings put secure location_providers_allowed -gps
adb shell pm grant com.expedia.bookings.debug android.permission.ACCESS_COARSE_LOCATION
adb shell pm grant com.expedia.bookings.debug android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.expedia.bookings.debug android.permission.DUMP
```

Debugging
```
fastlane screengrab --verbose
```

Additional screengrab parameters
```
--use_tests_in_packages:"com.expedia.bookings.screengrab",
--use_tests_in_classes:"com.expedia.bookings.screengrab.PlayStoreScreenshotSweep",
--locales:"da_DK,en_US"
```
