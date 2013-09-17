class AppNameLabels {

    // Use this data class to store homescreen app name overrides. If you'd like the name to be
    // overriden for your build variant, define a packageName/label pair in this class and it
    // will be picked up and overridden in the build.gradle script.

    static def appNameLabels = ['com.expedia.bookings.latest' : 'Expedia Latest',
            'com.expedia.bookings.debug' : 'Expedia Debug',
            'com.expedia.bookings.tablet' : 'Expedia Tablet',
            'com.expedia.bookings.next' : 'Expedia Next',
            'com.expedia.bookings.vsc.debug' : 'VSC Debug',
            'com.expedia.bookings.vsc.latest' : 'VSC Latest']

    static def getAppName(buildVariant) {
        def packageName = GradleUtil.getPackageName(buildVariant)
        return appNameLabels[packageName]
    }

}