package com.expedia.bookings.webview

import android.app.Activity
import android.webkit.WebView
import com.expedia.bookings.fragment.WebViewFragment
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BaseWebViewClientTest {

    private lateinit var activity: Activity
    private lateinit var sut: BaseWebViewClient

    @Test
    fun webViewIsClosedOnExpdaSuccessLink() {
        createSystemUnderTest()
        assertShouldOverrideUrlLoading(url = "expda://success", expectedResult = true)
        assertWebViewActivityClosed()
    }

    @Test
    fun webViewIsClosedOnExpdaDismissLink() {
        createSystemUnderTest()
        assertShouldOverrideUrlLoading(url = "expda://dismiss", expectedResult = true)
        assertWebViewActivityClosed()
    }

    @Test
    fun loadCookiesTrueDontOverrideUrlLoading() {
        createSystemUnderTest(loadCookies = true)
        assertShouldOverrideUrlLoading(url = "http://any-url", expectedResult = false)
    }

    @Test
    fun logoutLinkOverrideUrlLoading() {
        createSystemUnderTest(loadCookies = true)
        assertShouldOverrideUrlLoading(url = "http://user/logout", expectedResult = false)
    }

    @Test
    fun mailToLinksOpenEmailClient() {
        val emailAddress = "tharman@expedia.com"
        val url = "mailto:$emailAddress"
        createSystemUnderTest(expectedEmailUrl = url)

        assertShouldOverrideUrlLoading(url = url, expectedResult = true)
    }

    private fun createSystemUnderTest(loadCookies: Boolean = false, expectedEmailUrl: String = "") {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        sut = TestBaseWebViewClient(activity = activity, loadCookies = loadCookies, expectedEmailUrl = expectedEmailUrl)
    }

    private fun assertShouldOverrideUrlLoading(url: String, expectedResult: Boolean) {
        val mockWebView = WebView(activity.applicationContext)
        val actualResult = sut.shouldOverrideUrlLoading(mockWebView, url)
        assertEquals(expectedResult, actualResult)
    }

    private fun assertWebViewActivityClosed() {
        val shadowActivity = shadowOf(activity)
        val webViewActivityClosed = shadowActivity.isFinishing
        assertTrue(webViewActivityClosed)
    }

    class TestBaseWebViewClient(activity: Activity, loadCookies: Boolean, val expectedEmailUrl: String)
        : BaseWebViewClient(activity, loadCookies, WebViewFragment.TrackingName.Default) {

        override fun doSupportEmail(url: String) {
            assertEquals(expectedEmailUrl, url)
        }
    }
}
