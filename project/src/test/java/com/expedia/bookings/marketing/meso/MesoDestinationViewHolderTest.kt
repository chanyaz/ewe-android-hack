package com.expedia.bookings.marketing.meso

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.marketing.meso.model.MesoDestinationAdResponse
import com.expedia.bookings.marketing.meso.vm.MesoDestinationViewModel
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class MesoDestinationViewHolderTest {

    lateinit var sut: MesoDestinationViewHolder
    lateinit var vm: MesoDestinationViewModel
    lateinit var mockAnalyticsProvider: AnalyticsProvider
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    @Before
    fun before() {
        createSystemUnderTest()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        Mockito.`when`(mockAnalyticsProvider.getUrlWithVisitorData(Mockito.anyString())).thenAnswer { it.arguments[0] }
    }

    @Test
    fun testPerformClickGoToWebView_WithTitle_WithURLAppendedParameters() {
        val shadowActivity = Shadows.shadowOf(activity)
        sut.itemView.performClick()
        val startedIntent = shadowActivity.nextStartedActivity
        val extras = startedIntent.extras
        assertEquals(WebViewActivity::class.java.name, startedIntent.component.className)
        assertEquals("Las Vegas", extras.getString("ARG_TITLE"))
        assertEquals("https://viewfinder.expedia.com/features/vintage-las-vegas?rfrr=App.LS.MeSo&mcicid=App.LS.MeSo.Dest.Las%20Vegas", extras.getString("ARG_URL"))
    }

    @Test
    fun testPerformClickIsTrackedInOmniture() {
        sut.itemView.performClick()
        OmnitureTestUtils.assertLinkTracked("App Landing", "App.LS.MeSo",
                OmnitureMatchers.withEvars(mapOf(12 to "App.LS.MeSo.Dest.Las Vegas")), mockAnalyticsProvider)
    }

    private fun createSystemUnderTest() {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.meso_destination_launch_card, null)
        vm = MesoDestinationViewModel(activity)
        vm.mesoDestinationAdResponse = MesoDestinationAdResponse("Las Vegas", "Vintage Las Vegas", "Sponsored",
                "https://viewfinder.expedia.com/features/vintage-las-vegas", "https://a.travel-assets.com/dynamic_images/178276.jpg")
        sut = MesoDestinationViewHolder(view, vm)
        sut.bindData()
    }
}
