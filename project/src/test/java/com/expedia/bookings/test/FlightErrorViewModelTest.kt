package com.expedia.bookings.test

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.vm.flights.FlightErrorViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class FlightErrorViewModelTest {

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }
    private lateinit var subjectUnderTest: FlightErrorViewModel

    @Before
    fun setup() {
        subjectUnderTest = FlightErrorViewModel(RuntimeEnvironment.application)
    }

    @Test fun observableEmissionsOnSearchApiError() {
        observableEmissionsOnSearchError(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
    }

    @Test fun testRetrySearchError() {
        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        val retrySearchTestSubscriber = TestObserver.create<Unit>()
        val defaultErrorTestSubscriber = TestObserver.create<Unit>()

        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)
        subjectUnderTest.retrySearch.subscribe(retrySearchTestSubscriber)
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorTestSubscriber)

        subjectUnderTest.searchApiErrorObserver.onNext(ApiError(ApiError.Code.INVALID_INPUT))
        errorMessageObservableTestSubscriber.awaitTerminalEvent(100, TimeUnit.MILLISECONDS)
        errorMessageObservableTestSubscriber.assertValue(getContext().getString(R.string.error_try_again_warning))

        subjectUnderTest.retryBtnClicked.onNext(Unit)
        retrySearchTestSubscriber.assertValueCount(1)
        defaultErrorTestSubscriber.assertValueCount(0)

        subjectUnderTest.retryBtnClicked.onNext(Unit)
        retrySearchTestSubscriber.assertValueCount(2)
        defaultErrorTestSubscriber.assertValueCount(0)

        subjectUnderTest.retryBtnClicked.onNext(Unit)
        retrySearchTestSubscriber.assertValueCount(3)
        defaultErrorTestSubscriber.assertValueCount(0)

        subjectUnderTest.retryBtnClicked.onNext(Unit)
        retrySearchTestSubscriber.assertValueCount(3)
        defaultErrorTestSubscriber.assertValueCount(1)
    }

    @Test fun testCheckoutErrorMessageInvalidPaymentInfo() {
        val errorMessage = getContext().getString(R.string.e3_error_checkout_invalid_input)

        val apiCheckoutErrorBadCard = ApiError(ApiError.Code.INVALID_INPUT)
        apiCheckoutErrorBadCard.errorInfo = ApiError.ErrorInfo()
        apiCheckoutErrorBadCard.errorInfo.field = "creditCardNumber"
        apiCheckoutErrorBadCard.errorInfo.source = "London"
        apiCheckoutErrorBadCard.errorInfo.sourceErrorId = "44232"

        val checkoutError = FlightsV2Tracking.createCheckoutError(apiCheckoutErrorBadCard)

        assertEquals("CKO:London:44232", checkoutError)
        assertErrorMessageMatchesCheckoutError(apiCheckoutErrorBadCard, errorMessage)
    }

    @Test fun testCheckoutErrorButtonInvalidTravelerInfo() {
        val errorMessage = getContext().getString(R.string.edit_traveler_details)
        val apiCheckoutErrorBadCard = ApiError(ApiError.Code.INVALID_INPUT)
        apiCheckoutErrorBadCard.errorInfo = ApiError.ErrorInfo()
        apiCheckoutErrorBadCard.errorInfo.field = "mainFlightPassenger.firstName"
        apiCheckoutErrorBadCard.errorInfo.source = "USA"

        val checkoutError = FlightsV2Tracking.createCheckoutError(apiCheckoutErrorBadCard)

        assertEquals("CKO:USA:INVALID_INPUT", checkoutError)
        assertButtonTextMatchesCheckoutError(apiCheckoutErrorBadCard, errorMessage)
    }

    @Test fun testCheckoutErrorButtonInvalidPaymentInfo() {
        val errorMessage = getContext().getString(R.string.edit_payment)
        val apiCheckoutErrorBadCard = ApiError(ApiError.Code.INVALID_INPUT)
        apiCheckoutErrorBadCard.errorInfo = ApiError.ErrorInfo()
        apiCheckoutErrorBadCard.errorInfo.field = "creditCardNumber"

        val checkoutError = FlightsV2Tracking.createCheckoutError(apiCheckoutErrorBadCard)

        assertEquals("CKO::INVALID_INPUT", checkoutError)
        assertButtonTextMatchesCheckoutError(apiCheckoutErrorBadCard, errorMessage)
    }

    private fun assertButtonTextMatchesCheckoutError(apiError: ApiError, errorMessage: String) {
        val errorButtonTextSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonTextSubscriber)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        errorButtonTextSubscriber.awaitTerminalEvent(100, TimeUnit.MILLISECONDS)

        errorButtonTextSubscriber.assertValue(errorMessage)
    }

    private fun assertErrorMessageMatchesCheckoutError(apiError: ApiError, errorMessage: String) {
        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        errorMessageObservableTestSubscriber.awaitTerminalEvent(100, TimeUnit.MILLISECONDS)

        errorMessageObservableTestSubscriber.assertValue(errorMessage)
    }

    fun observableEmissionsOnSearchError(apiError: ApiError) {
        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        subjectUnderTest.searchApiErrorObserver.onNext(apiError)
        errorMessageObservableTestSubscriber.awaitTerminalEvent(100, TimeUnit.MILLISECONDS)

        errorMessageObservableTestSubscriber.assertValue(getContext().getString(R.string.error_no_result_message))
    }

    @Test fun toolBarTitleAndSubTitle() {
        val flightSearchParams = doFlightSearch()
        val expectedDate = LocaleBasedDateFormatUtils.dateTimeToEEEMMMddyyyy(flightSearchParams.departureDate)
        subjectUnderTest.paramsSubject.onNext(flightSearchParams)
        assertEquals("Select flight to Los Angles (LA)", subjectUnderTest.titleObservable.value)
        assertEquals(expectedDate + ", 1 traveler", subjectUnderTest.subTitleObservable.value)
    }

    private fun doFlightSearch(): FlightSearchParams {
        return FlightSearchParams.Builder(26, 500)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .build() as FlightSearchParams
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "Los <B>Ang</B>les (LA)"
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = "Los Angles"
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        return suggestion
    }
}
