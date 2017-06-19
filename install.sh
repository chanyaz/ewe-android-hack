unzip /Users/pkothari/Expedia/Trash/ewe-android-eb/instantapp/build/outputs/apk/expedia/debug/instantapp-expedia-debug.zip
adb push project-expedia-debug.apk /sdcard/
adb shell pm install -t -r /sdcard/project-expedia-debug.apk
adb shell am start -a android.intent.action.VIEW -c android.intent.category.BROWSABLE -d http://www.expedia.com/ -n "com.google.android.instantapps.supervisor/.UrlHandler"
