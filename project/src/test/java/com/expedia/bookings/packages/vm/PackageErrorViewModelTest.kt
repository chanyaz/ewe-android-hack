package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.PackagesTracking
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageErrorViewModelTest {

    val originFullName = "Origin fullname"
    val originShortName = "Origin shortname"
    val destinationFullName = "Destination fullname"
    val destinationShortName = "Destination shortname"
    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun observableEmissionsOnSearchApiError() {
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_unknown_error)
        observableEmissionsOnSearchError(PackageApiError.Code.search_response_null)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_destination_resolution_failed)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_origin_resolution_failed)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_flight_no_longer_available)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_too_many_children_in_lap)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_invalid_checkin_checkout_dates)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_hotel_no_longer_available)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_search_from_date_too_near)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_piid_expired)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_pss_downstream_service_timeout)
        observableEmissionsOnSearchError(PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight)
        observableEmissionsOnSearchError(PackageApiError.Code.mid_no_offers_post_filtering)
        observableEmissionsOnSearchError(PackageApiError.Code.no_internet)
        assertButtonTextOnError(PackageApiError.Code.pkg_piid_expired)
        assertButtonTextOnError(PackageApiError.Code.pkg_pss_downstream_service_timeout)
        assertButtonTextOnError(PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight)
        assertButtonTextOnError(PackageApiError.Code.pkg_destination_resolution_failed)
        assertButtonTextOnError(PackageApiError.Code.pkg_origin_resolution_failed)
        assertButtonTextOnError(PackageApiError.Code.pkg_unknown_error)
        assertButtonTextOnError(PackageApiError.Code.search_response_null)
        assertButtonTextOnError(PackageApiError.Code.pkg_flight_no_longer_available)
        assertButtonTextOnError(PackageApiError.Code.pkg_too_many_children_in_lap)
        assertButtonTextOnError(PackageApiError.Code.pkg_no_flights_available)
        assertButtonTextOnError(PackageApiError.Code.pkg_hotel_no_longer_available)
        assertButtonTextOnError(PackageApiError.Code.pkg_search_from_date_too_near)
        assertButtonTextOnError(PackageApiError.Code.mid_could_not_find_results)
        assertButtonTextOnError(PackageApiError.Code.pkg_invalid_checkin_checkout_dates)
        assertButtonTextOnError(PackageApiError.Code.mid_no_offers_post_filtering)
    }

    private fun getSearchAPIErrorDetails(errorCode: PackageApiError.Code): Pair<PackageApiError.Code, ApiCallFailing> {
        return Pair(errorCode, ApiCallFailing.PackageHotelSearch(errorCode.name))
    }

    fun observableEmissionsOnSearchError(apiError: PackageApiError.Code) {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val searchApiObservableTestSubscriber = TestObserver.create<Pair<PackageApiError.Code, ApiCallFailing>>()
        subjectUnderTest.packageSearchApiErrorObserver.subscribe(searchApiObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        subjectUnderTest.paramsSubject.onNext(getPackageSearchParams())

        subjectUnderTest.packageSearchApiErrorObserver.onNext(getSearchAPIErrorDetails(apiError))
        val expectedErrorMessage = when (apiError) {
            PackageApiError.Code.pkg_piid_expired,
            PackageApiError.Code.pkg_pss_downstream_service_timeout -> RuntimeEnvironment.application.getString(R.string.reservation_time_out)
            PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight -> RuntimeEnvironment.application.getString(R.string.error_package_search_red_eye_flight_message)
            PackageApiError.Code.pkg_destination_resolution_failed -> "Sorry, we could not resolve the entered destination for $destinationShortName. Please retry."
            PackageApiError.Code.pkg_origin_resolution_failed -> "Sorry, we could not resolve the entered origin for $originShortName. Please retry."
            PackageApiError.Code.mid_no_offers_post_filtering -> RuntimeEnvironment.application.getString(R.string.error_no_filter_result_message)
            PackageApiError.Code.no_internet -> "Sorry, we are facing some technical issues. Please try again later."
            else -> RuntimeEnvironment.application.getString(R.string.error_package_search_message)
        }

        errorMessageObservableTestSubscriber.assertValues(expectedErrorMessage)
    }

    fun assertButtonTextOnError(apiError: PackageApiError.Code) {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val searchApiObservableTestSubscriber = TestObserver.create<Pair<PackageApiError.Code, ApiCallFailing>>()
        subjectUnderTest.packageSearchApiErrorObserver.subscribe(searchApiObservableTestSubscriber)

        val buttonTextObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(buttonTextObservableTestSubscriber)

        subjectUnderTest.paramsSubject.onNext(getPackageSearchParams())

        subjectUnderTest.packageSearchApiErrorObserver.onNext(getSearchAPIErrorDetails(apiError))

        val expectedButtonText = when (apiError) {
            PackageApiError.Code.pkg_piid_expired -> RuntimeEnvironment.application.getString(R.string.search_again)
            PackageApiError.Code.pkg_pss_downstream_service_timeout -> RuntimeEnvironment.application.getString(R.string.search_again)
            PackageApiError.Code.mid_fss_hotel_unavailable_for_red_eye_flight -> RuntimeEnvironment.application.getString(R.string.retry)
            PackageApiError.Code.pkg_destination_resolution_failed -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_origin_resolution_failed -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_unknown_error -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.search_response_null -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_flight_no_longer_available -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_too_many_children_in_lap -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_no_flights_available -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_hotel_no_longer_available -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_search_from_date_too_near -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.mid_could_not_find_results -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.pkg_invalid_checkin_checkout_dates -> RuntimeEnvironment.application.getString(R.string.edit_search)
            PackageApiError.Code.mid_no_offers_post_filtering -> RuntimeEnvironment.application.getString(R.string.clear_filters)
            else -> RuntimeEnvironment.application.getString(R.string.retry)
        }
        buttonTextObservableTestSubscriber.assertValues(expectedButtonText)
    }

    private fun getPackageSearchParams(): PackageSearchParams {
        val origin = SuggestionV4()
        val originRegionNames = SuggestionV4.RegionNames()
        originRegionNames.fullName = originFullName
        originRegionNames.shortName = originShortName
        origin.regionNames = originRegionNames

        val destination = SuggestionV4()
        val destinationRegionNames = SuggestionV4.RegionNames()
        destinationRegionNames.fullName = destinationFullName
        destinationRegionNames.shortName = destinationShortName
        destination.regionNames = destinationRegionNames

        return PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(origin)
                .destination(destination)
                .build() as PackageSearchParams
    }

    @Test
    fun observableEmissionsOnUnknownError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val checkoutUnknownErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.checkoutUnknownErrorObservable.subscribe(checkoutUnknownErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN)

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO::PACKAGE_CHECKOUT_UNKNOWN", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        checkoutUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = getContext().getString(R.string.package_error_server)
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
    }

    @Test
    fun testObservableEmissionsUnknownError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val unknownErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.createTripUnknownErrorObservable.subscribe(unknownErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.UNKNOWN_ERROR)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "UK"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:UK:UNKNOWN_ERROR", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        unknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = getContext().getString(R.string.package_error_server)
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
    }

    @Test
    fun testObservableEmissionsNoResultsOnFilterError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val filterNoResultsObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.filterNoResultsObservable.subscribe(filterNoResultsObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.PACKAGE_HOTEL_NO_RESULTS_POST_FILTER)
        subjectUnderTest.error = apiError

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        filterNoResultsObservableTestSubscriber.assertValues(Unit)
    }

    @Test
    fun testObservableEmissionsDefaultError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.UNMAPPED_ERROR)
        subjectUnderTest.error = apiError

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)
    }

    @Test
    fun observableEmissionsOnHotelOffersError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val hotelOfferErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(hotelOfferErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        var apiError = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "Atlantis"
        apiError.errorInfo.sourceErrorId = "K2401"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:Atlantis:K2401", checkoutError)

        subjectUnderTest.hotelOffersApiErrorObserver.onNext(Pair(apiError.errorCode!!, ApiCallFailing.PackageHotelRoom(apiError.errorCode!!.name)))
        subjectUnderTest.defaultErrorObservable.onNext(Unit)

        apiError = ApiError(ApiError.Code.UNKNOWN_ERROR)
        subjectUnderTest.hotelOffersApiErrorObserver.onNext(Pair(apiError.errorCode!!, ApiCallFailing.PackageHotelRoom(apiError.errorCode!!.name)))
        subjectUnderTest.defaultErrorObservable.onNext(Unit)

        hotelOfferErrorObservableTestSubscriber.assertValues(Unit, Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_search, R.drawable.error_default)
        errorMessageObservableTestSubscriber.assertValues(getContext().getString(R.string.error_package_search_message), getContext().getString(R.string.package_error_server))
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.edit_search), getContext().getString(R.string.retry))
    }
}
