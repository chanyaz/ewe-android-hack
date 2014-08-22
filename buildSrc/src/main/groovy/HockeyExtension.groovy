class HockeyExtension {

    // This is an "extension" class that gets attached to a buildType. This allows for easy configuration
    // of HockeyApp builds. After adding this extension, one can set the enabled value and later query the
    // buildType for whether or not hockeyApp is enabled and perform tasks if necessary.
    //
    // http://stackoverflow.com/questions/17697154/gradle-android-plugin-add-custom-flavor-attribute

    boolean enabled

    HockeyExtension() {
        enabled = false;
    }

    //////////////////////////////////////////////
    // Static Data
    //
    // This map stores HockeyApp project IDs for a given packageName

    static def hockeyIds = [
        'com.expedia.bookings.latest' : '4d9aae3faac40c74443772c8bebd5aaf',
        'com.expedia.bookings.next' : 'dfd124d23663e4513d4fe5745fb1edf8',
        'com.expedia.bookings.tablet' : 'f2a4bda78f7b59aea9bb0f1a129c1b94',

        'com.expedia.bookings.vsc.latest' : '48567c64e3daad9bee274ab36e5d8498',

        'com.travelocity.android.latest' : '1a4b859f1e9e0281742a761d5c92ff71',

        'com.airasiago.android.latest' : 'ff6f33e5467b6a194354853e0dbce39c',
    ]

    static def getHockeyId(buildVariant) {
        return hockeyIds[GradleUtil.getPackageName(buildVariant)]
    }

}
