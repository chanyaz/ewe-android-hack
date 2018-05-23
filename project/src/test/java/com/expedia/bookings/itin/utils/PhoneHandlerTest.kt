package com.expedia.bookings.itin.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PhoneHandlerTest {
    lateinit var sut: PhoneHandler
    lateinit var activity: Activity
    lateinit var mockPackageManager: PackageManager

    @Before
    fun setup() {
        mockPackageManager = Mockito.mock(PackageManager::class.java)
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        sut = PhoneHandler(context = activity, packageManager = mockPackageManager)
    }

    @Test
    fun testWithTelephony() {
        Mockito.`when`(mockPackageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)).thenReturn(true)
        val shadowActivity = Shadows.shadowOf(activity)
        sut.handle("113 323 4444")
        val intent = shadowActivity.peekNextStartedActivity()
        assertEquals("tel", intent.data.scheme)
        assertEquals(Intent.ACTION_VIEW, intent.action)
    }

    @Test
    fun testWithoutTelephony() {
        val number = "113 323 4444"
        Mockito.`when`(mockPackageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)).thenReturn(false)
        val expectedString = activity.getString(R.string.toast_copied_to_clipboard)

        sut.handle(number)

        assertEquals(expectedString, ShadowToast.getTextOfLatestToast())
        assertEquals(ClipboardUtils.getText(activity), number)
    }
}
