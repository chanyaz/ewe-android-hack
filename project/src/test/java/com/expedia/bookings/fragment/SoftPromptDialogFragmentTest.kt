package com.expedia.bookings.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Button
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.launch.activity.PhoneLaunchActivity
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Constants
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class SoftPromptDialogFragmentTest {

    private lateinit var activity: PhoneLaunchActivity
    private lateinit var prompt: SoftPromptDialogFragment
    private lateinit var enableButton: Button
    private lateinit var dismissButton: Button
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        activity = Robolectric.buildActivity(PhoneLaunchActivity::class.java).create().resume().get()
        activity.setTheme(R.style.NewLaunchTheme)
        prompt = SoftPromptDialogFragment()
        prompt.show(activity.supportFragmentManager, "fragment_dialog_soft_prompt")
        enableButton = prompt.dialog.findViewById<Button>(R.id.soft_prompt_enable_button)
        dismissButton = prompt.dialog.findViewById<Button>(R.id.soft_prompt_disable_text)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testSoftPromptButtonClickOmnitureTracking() {
        enableButton.performClick()
        assertLinkTracked("Soft Prompt", "App.LS.LocPermSP.Accept", mockAnalyticsProvider)
        dismissButton.performClick()
        assertLinkTracked("Soft Prompt", "App.LS.LocPermSP.Cancel", mockAnalyticsProvider)
    }

    @Test
    fun testNativePromptButtonClickOmnitureTracking() {
        activity.onRequestPermissionsResult(Constants.PERMISSION_REQUEST_LOCATION, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), intArrayOf(PackageManager.PERMISSION_GRANTED))
        assertLinkTracked("App Message", "App.DeviceLocation.Ok", OmnitureMatchers.withEventsString("event41"), mockAnalyticsProvider)
        activity.onRequestPermissionsResult(Constants.PERMISSION_REQUEST_LOCATION, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), intArrayOf(PackageManager.PERMISSION_DENIED))
        assertLinkTracked("App Message", "App.DeviceLocation.Opt-Out", OmnitureMatchers.withEventsString("event40"), mockAnalyticsProvider)
    }
}

