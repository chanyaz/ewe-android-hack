package com.expedia.bookings.mia.arch

import com.expedia.bookings.data.BaseDealsResponse
import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.data.sos.MemberDealsResponse
import com.expedia.bookings.services.sos.ISmartOfferService
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.Observer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class MemberDealsArchViewModelTest {

    private lateinit var viewModel: MemberDealsArchViewModel
    lateinit var capturedObserver: Observer<MemberDealsResponse>
    lateinit var capturedRequest: MemberDealsRequest

    @Before
    fun setup() {
        viewModel = MemberDealsArchViewModel(MockSmartOfferService(), createMockRequest())
        viewModel.responseLiveData
    }

    @Test
    fun testRequestIsPassedToFetchCall() {
        assertEquals("10", capturedRequest.siteId)
        assertEquals("en_US", capturedRequest.locale)
    }

    @Test
    fun testResponseIsPassedToLiveData() {
        capturedObserver.onNext(createMockResponse())
        assertEquals("USD", viewModel.responseLiveData.value!!.offerInfo!!.currency)
    }

    inner class MockSmartOfferService : ISmartOfferService {
        override fun fetchDeals(request: MemberDealsRequest, dealsObserver: Observer<MemberDealsResponse>) {
            capturedRequest = request
            capturedObserver = dealsObserver
        }
    }

    private fun createMockResponse(): MemberDealsResponse {
        val response = MemberDealsResponse()
        response.offerInfo = BaseDealsResponse.OfferInfo()
        response.offerInfo?.currency = "USD"
        return response
    }

    private fun createMockRequest(): MemberDealsRequest {
        val request = MemberDealsRequest()
        request.siteId = "10"
        request.locale = "en_US"
        return request
    }
}
