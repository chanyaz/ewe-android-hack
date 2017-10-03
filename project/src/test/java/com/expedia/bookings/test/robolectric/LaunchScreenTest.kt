package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.launch.widget.PhoneLaunchWidget
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class LaunchScreenTest {
    var activity: Activity by Delegates.notNull()
    var phoneLaunchWidget: PhoneLaunchWidget by Delegates.notNull()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
        phoneLaunchWidget = LayoutInflater.from(activity).inflate(R.layout.widget_phone_launch, null) as PhoneLaunchWidget
    }

    @Test
    fun testInternetConnection() {
        val launchError = phoneLaunchWidget.findViewById<View>(R.id.launch_error)
        val lobCard = phoneLaunchWidget.findViewById<View>(R.id.lob_grid_recycler)
        phoneLaunchWidget.hasInternetConnection.onNext(false)
        Assert.assertEquals(launchError.visibility, View.VISIBLE)
        Assert.assertEquals(lobCard.visibility, View.VISIBLE)

        phoneLaunchWidget.hasInternetConnection.onNext(true)
        Assert.assertEquals(launchError.visibility, View.GONE)
        Assert.assertEquals(lobCard.visibility, View.VISIBLE)
    }
}