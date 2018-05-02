package com.expedia.bookings.notification

import com.activeandroid.ActiveAndroid
import com.activeandroid.Configuration
import com.expedia.bookings.test.robolectric.TestExpediaBookingApp

class ActiveAndroidNotifcationTestApp : TestExpediaBookingApp() {

    override fun onCreate() {
        super.onCreate()
        val configuration = Configuration.Builder(this).setDatabaseName(null)
        configuration.addModelClasses(Notification::class.java)
        ActiveAndroid.initialize(configuration.create())
    }
}
