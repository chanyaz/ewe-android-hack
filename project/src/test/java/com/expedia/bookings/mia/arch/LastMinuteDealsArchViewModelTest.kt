package com.expedia.bookings.mia.arch

import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.BaseDealsResponse
import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.services.os.IOfferService
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observer
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LastMinuteDealsArchViewModelTest {

    private lateinit var viewModel: LastMinuteDealsArchViewModel
    lateinit var capturedObserver: Observer<LastMinuteDealsResponse>
    lateinit var capturedRequest: LastMinuteDealsRequest
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        viewModel = LastMinuteDealsArchViewModel(MockSmartOfferService(), createMockRequest())
        viewModel.responseLiveData
    }

    @Test
    fun testRequestIsPassedToFetchCall() {
        assertEquals("1234", capturedRequest.uid)
        assertEquals("10", capturedRequest.siteId)
        assertEquals("en_US", capturedRequest.locale)
    }

    @Test
    fun testResponseIsPassedToLiveData() {
        capturedObserver.onNext(createMockResponse())
        assertEquals("USD", viewModel.responseLiveData.value!!.offerInfo!!.currency)
    }

    @Test
    fun testEmptyListIsTrackedInOmniture() {
        capturedObserver.onNext(createMockResponse())
        OmnitureTestUtils.assertStateTracked("App.LastMinuteDeals", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(36 to "App.LMD.NoResults")),
                OmnitureMatchers.withEvars(mapOf(18 to "App.LastMinuteDeals"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testErrorIsTrackedInOmniture() {
        capturedObserver.onError(RuntimeException("Test exception to make sure that this event was tracked in Omniture"))
        OmnitureTestUtils.assertStateTracked("App.LastMinuteDeals", Matchers.allOf(
                OmnitureMatchers.withProps(mapOf(36 to "App.LMD.Error")),
                OmnitureMatchers.withEvars(mapOf(18 to "App.LastMinuteDeals"))),
                mockAnalyticsProvider)
    }

    inner class MockSmartOfferService : IOfferService {
        override fun fetchDeals(request: LastMinuteDealsRequest, dealsObserver: Observer<LastMinuteDealsResponse>) {
            capturedRequest = request
            capturedObserver = dealsObserver
        }
    }

    private fun createMockResponse(): LastMinuteDealsResponse {
        val response = LastMinuteDealsResponse()
        response.offerInfo = BaseDealsResponse.OfferInfo()
        response.offerInfo?.currency = "USD"
        return response
    }

    private fun createMockRequest(): LastMinuteDealsRequest {
        val request = LastMinuteDealsRequest("1234")
        request.siteId = "10"
        request.locale = "en_US"
        return request
    }
}
