package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CaptchaWebViewActivityTest {
    private lateinit var sut: CaptchaWebViewActivity
    private lateinit var activityController: ActivityController<CaptchaWebViewActivity>
    private lateinit var context: Context
    val htmlString = "html"
    val baseUrl = "baseUrl"
    val originalUrl = "originalUrl"
    lateinit var intent: Intent

    @Before
    fun setup() {
        activityController = Robolectric.buildActivity(CaptchaWebViewActivity::class.java)
        sut = activityController.get()
        context = RuntimeEnvironment.application
        intent = CaptchaWebViewActivity.IntentBuilder(context, originalUrl, htmlString, baseUrl).intent
    }

    @Test
    fun captchaWebViewActivityIntentBulderTest() {
        assertEquals(htmlString, intent.getStringExtra("ARG_HTML_DATA"))
        assertEquals(originalUrl, intent.getStringExtra("ARG_ORIGINAL_URL"))
        assertTrue(intent.getBooleanExtra("ARG_USE_WEB_VIEW_TITLE", false))
        assertEquals(baseUrl, intent.getStringExtra("ARG_BASE_URL"))
        assertTrue(intent.flags == Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    @Test
    fun newUrlLoadedTest() {
        val shadow = Shadows.shadowOf(sut)
        sut.intent = intent
        sut.newUrlLoaded("rando")
        assertFalse(shadow.isFinishing)
        sut.newUrlLoaded(originalUrl)
        assertTrue(shadow.isFinishing)
    }
}
