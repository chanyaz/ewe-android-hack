
package com.expedia.vm.test.rail

import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.rail.requests.RailSearchRequest
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.DateFormatUtils
import com.expedia.vm.rail.RailErrorViewModel
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class RailErrorViewModelTest {

    val context = RuntimeEnvironment.application

    lateinit private var subjectUnderTest: RailErrorViewModel

    @Before
    fun before() {
        subjectUnderTest = RailErrorViewModel(context)
    }

    @Test
    fun toolBarTitleAndSubTitle() {
        val searchRequest = RailSearchRequest.Builder(20, 100)
                .departDateTimeMillis(RailSearchRequestMock.departTime())
                .startDate(RailSearchRequestMock.departDate().plusDays(1))
                .origin(RailSearchRequestMock.origin("Origin"))
                .destination(RailSearchRequestMock.destination("Destination")).build() as RailSearchRequest
        subjectUnderTest.paramsSubject.onNext(searchRequest)
        val expectedDateText = DateFormatUtils.formatRailDateRange(context, searchRequest.startDate, searchRequest.endDate)
        assertEquals("Origin - Destination", subjectUnderTest.titleObservable.value)
        assertEquals("$expectedDateText - 1 traveler", subjectUnderTest.subTitleObservable.value)
    }

    @Test
    fun testSearchNoResults() {
        val testErrorImageSubscriber = TestSubscriber<Int>()
        val testErrorMessageSubscriber = TestSubscriber<String>()
        val testButtonTextSubscriber = TestSubscriber<String>()
        subjectUnderTest.imageObservable.subscribe(testErrorImageSubscriber)
        subjectUnderTest.errorMessageObservable.subscribe(testErrorMessageSubscriber)
        subjectUnderTest.buttonOneTextObservable.subscribe(testButtonTextSubscriber)

        subjectUnderTest.searchApiErrorObserver.onNext(ApiError(ApiError.Code.RAIL_SEARCH_NO_RESULTS))
        testErrorImageSubscriber.assertValueCount(1)
        testErrorMessageSubscriber.assertValueCount(1)
        assertEquals(context.getString(R.string.error_no_result_message), testErrorMessageSubscriber.onNextEvents[0])
        testButtonTextSubscriber .assertValueCount(1)
        assertEquals(context.getString(R.string.edit_search), testButtonTextSubscriber.onNextEvents[0])
    }

    @Test
    fun testOtherSearchErrors() {
        val testErrorImageSubscriber = TestSubscriber<Int>()
        val testErrorMessageSubscriber = TestSubscriber<String>()
        val testButtonTextSubscriber = TestSubscriber<String>()
        subjectUnderTest.imageObservable.subscribe(testErrorImageSubscriber)
        subjectUnderTest.errorMessageObservable.subscribe(testErrorMessageSubscriber)
        subjectUnderTest.buttonOneTextObservable.subscribe(testButtonTextSubscriber)

        val expectedErrorMessage = Phrase.from(context, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        subjectUnderTest.searchApiErrorObserver.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
        testErrorImageSubscriber.assertValueCount(1)
        testErrorMessageSubscriber.assertValueCount(1)
        assertEquals(expectedErrorMessage, testErrorMessageSubscriber.onNextEvents[0])
        testButtonTextSubscriber .assertValueCount(1)
        assertEquals(context.getString(R.string.retry), testButtonTextSubscriber.onNextEvents[0])
    }

    @Test
    fun testCreateTripErrors() {
        val testErrorImageSubscriber = TestSubscriber<Int>()
        val testErrorMessageSubscriber = TestSubscriber<String>()
        val testButtonTextSubscriber = TestSubscriber<String>()
        subjectUnderTest.imageObservable.subscribe(testErrorImageSubscriber)
        subjectUnderTest.errorMessageObservable.subscribe(testErrorMessageSubscriber)
        subjectUnderTest.buttonOneTextObservable.subscribe(testButtonTextSubscriber)

        subjectUnderTest.createTripErrorObserverable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
        testErrorImageSubscriber.assertValueCount(1)
        testErrorMessageSubscriber.assertValueCount(1)
        assertEquals(context.getString(R.string.rail_unknown_error_message), testErrorMessageSubscriber.onNextEvents[0])
        testButtonTextSubscriber .assertValueCount(1)
        assertEquals(context.getString(R.string.retry), testButtonTextSubscriber.onNextEvents[0])
    }

    @Test
    fun testCheckoutInvalidInputError() {
        val testErrorImageSubscriber = TestSubscriber<Int>()
        val testErrorMessageSubscriber = TestSubscriber<String>()
        val testButtonTextSubscriber = TestSubscriber<String>()
        subjectUnderTest.imageObservable.subscribe(testErrorImageSubscriber)
        subjectUnderTest.errorMessageObservable.subscribe(testErrorMessageSubscriber)
        subjectUnderTest.buttonOneTextObservable.subscribe(testButtonTextSubscriber)

        subjectUnderTest.checkoutApiErrorObserver.onNext(ApiError(ApiError.Code.INVALID_INPUT))
        testErrorImageSubscriber.assertValueCount(1)
        testErrorMessageSubscriber.assertValueCount(1)
        assertEquals(context.getString(R.string.rail_cko_retry_error_message), testErrorMessageSubscriber.onNextEvents[0])
        testButtonTextSubscriber .assertValueCount(1)
        assertEquals(context.getString(R.string.edit_button), testButtonTextSubscriber.onNextEvents[0])
    }

    @Test
    fun testCheckoutUnknownApiError() {
        val testErrorImageSubscriber = TestSubscriber<Int>()
        val testErrorMessageSubscriber = TestSubscriber<String>()
        val testButtonTextSubscriber = TestSubscriber<String>()
        subjectUnderTest.imageObservable.subscribe(testErrorImageSubscriber)
        subjectUnderTest.errorMessageObservable.subscribe(testErrorMessageSubscriber)
        subjectUnderTest.buttonOneTextObservable.subscribe(testButtonTextSubscriber)

        subjectUnderTest.checkoutApiErrorObserver.onNext(ApiError(ApiError.Code.RAIL_UNKNOWN_CKO_ERROR))
        testErrorImageSubscriber.assertValueCount(1)
        testErrorMessageSubscriber.assertValueCount(1)
        assertEquals(context.getString(R.string.rail_cko_retry_error_message), testErrorMessageSubscriber.onNextEvents[0])
        testButtonTextSubscriber .assertValueCount(1)
        assertEquals(context.getString(R.string.edit_button), testButtonTextSubscriber.onNextEvents[0])
    }

    @Test
    fun testCheckoutUnknownError() {
        val testErrorImageSubscriber = TestSubscriber<Int>()
        val testErrorMessageSubscriber = TestSubscriber<String>()
        val testButtonTextSubscriber = TestSubscriber<String>()
        subjectUnderTest.imageObservable.subscribe(testErrorImageSubscriber)
        subjectUnderTest.errorMessageObservable.subscribe(testErrorMessageSubscriber)
        subjectUnderTest.buttonOneTextObservable.subscribe(testButtonTextSubscriber)

        subjectUnderTest.checkoutApiErrorObserver.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
        testErrorImageSubscriber.assertValueCount(1)
        testErrorMessageSubscriber.assertValueCount(1)
        assertEquals(context.getString(R.string.rail_unknown_error_message), testErrorMessageSubscriber.onNextEvents[0])
        testButtonTextSubscriber .assertValueCount(1)
        assertEquals(context.getString(R.string.edit_button), testButtonTextSubscriber.onNextEvents[0])
    }
}