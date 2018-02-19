package com.expedia.bookings.test.robolectric

import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.mia.activity.LastMinuteDealsActivity
import com.expedia.bookings.mia.arch.LastMinuteDealsArchViewModel
import com.expedia.bookings.services.os.IOfferService
import com.expedia.bookings.test.OmnitureMatchers
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LastMinuteDealsActivityTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var activityController: ActivityController<TestMemberDealsActivity>
    private lateinit var activity: TestMemberDealsActivity

    @Before
    fun setUp() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        activityController = Robolectric.buildActivity(TestMemberDealsActivity::class.java)
        activity = activityController.get()
        activity.setTheme(R.style.Theme_Deals)
        activityController.create().start().visible()
    }

    @Test
    fun testLiveDataIsBindToAdapterSubject() {
        val mockResponse = TestLastMinuteDealsResponse()
        activity.getDealsViewModel().responseLiveData.value = mockResponse
        assertEquals(1, activity.getDealsAdapter().itemCount)
    }

    @Test
    fun startingLastMinuteDealActivityIsTrackedInOmniture() {
        val activity = Robolectric.buildActivity(LastMinuteDealsActivity::class.java).create().start().postCreate(null).resume().get()
        OmnitureTestUtils.assertStateTracked("App.LastMinuteDeals", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(2 to "Merch")),
                OmnitureMatchers.withEvars(mapOf(12 to "App.LastMinuteDeals"))), mockAnalyticsProvider)
        activity.finish()
    }

    class TestMemberDealsActivity : LastMinuteDealsActivity() {
        fun getDealsAdapter() = adapter
        fun getDealsViewModel() = viewModel
        private val mockOfferService = Mockito.mock(IOfferService::class.java)
        override val viewModel = LastMinuteDealsArchViewModel(mockOfferService, LastMinuteDealsRequest("1234"))
    }

    inner class TestLastMinuteDealsResponse : LastMinuteDealsResponse() {
        init {
            val mockOffers = Offers()
            mockOffers.hotels = listOf(DealsDestination().Hotel())
            mockOffers.hotels[0].hotelPricingInfo = DealsDestination().Hotel().HotelPricingInfo()
            mockOffers.hotels[0].hotelPricingInfo!!.crossOutPriceValue = 146.34
            mockOffers.hotels[0].hotelPricingInfo!!.percentSavings = 37.5
            offers = mockOffers

            val mockOfferInfo = OfferInfo()
            mockOfferInfo.currency = "USD"
            offerInfo = mockOfferInfo
        }
    }
}
