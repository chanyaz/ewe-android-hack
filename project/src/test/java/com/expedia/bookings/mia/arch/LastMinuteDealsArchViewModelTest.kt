package com.expedia.bookings.mia.arch

import com.expedia.bookings.data.BaseDealsResponse
import com.expedia.bookings.data.os.LastMinuteDealsRequest
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.services.os.IOfferService
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LastMinuteDealsArchViewModelTest {

    private lateinit var viewModel: LastMinuteDealsArchViewModel
    lateinit var capturedObserver: Observer<LastMinuteDealsResponse>
    lateinit var capturedRequest: LastMinuteDealsRequest

    @Before
    fun setup() {
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
