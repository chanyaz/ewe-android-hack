package com.expedia.bookings.notification

import android.app.Application
import com.activeandroid.ActiveAndroid
import com.activeandroid.Configuration

class ActiveAndroidNotifcationTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val configuration = Configuration.Builder(this).setDatabaseName(null)
        configuration.addModelClasses(Notification::class.java)
        ActiveAndroid.initialize(configuration.create())
    }

    override fun onTerminate() {
        ActiveAndroid.dispose()
        super.onTerminate()
    }
}
