package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.launch.widget.NewPhoneLaunchWidget
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class NewLaunchScreenTest {
    var activity: Activity by Delegates.notNull()
    var newPhoneLaunchWidget: NewPhoneLaunchWidget by Delegates.notNull()


    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.NewLaunchTheme)
        newPhoneLaunchWidget = LayoutInflater.from(activity).inflate(R.layout.widget_new_phone_launch_test, null) as NewPhoneLaunchWidget

    }

    @Test
    fun testInternetConnection() {
        val launchError = newPhoneLaunchWidget.findViewById(R.id.launch_error)
        val lobCard = newPhoneLaunchWidget.findViewById(R.id.lob_grid_recycler)
        val otherCards = newPhoneLaunchWidget.findViewById(R.id.air_attach_banner)
        newPhoneLaunchWidget.hasInternetConnection.onNext(false)
        Assert.assertEquals(launchError.visibility, View.VISIBLE)
        Assert.assertEquals(lobCard.visibility, View.VISIBLE)

        newPhoneLaunchWidget.hasInternetConnection.onNext(true)
        Assert.assertEquals(launchError.visibility, View.GONE)
        Assert.assertEquals(lobCard.visibility, View.VISIBLE)
    }

    @Test
    fun testAirAttachBanner() {
        val airAttachBanner = newPhoneLaunchWidget.findViewById(R.id.air_attach_banner)
        newPhoneLaunchWidget.showAirAttachBanner.onNext(false)
        Assert.assertEquals(airAttachBanner.visibility, View.INVISIBLE)
        newPhoneLaunchWidget.showAirAttachBanner.onNext(true)
        Assert.assertEquals(airAttachBanner.visibility, View.VISIBLE)
    }

}