package com.expedia.bookings.test

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.vm.packages.PackageErrorViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
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

    @Test fun observableEmissionsOnSearchApiError() {
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_unknown_error)
        observableEmissionsOnSearchError(PackageApiError.Code.search_response_null)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_destination_resolution_failed)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_origin_resolution_failed)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_flight_no_longer_available)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_too_many_children_in_lap)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_invalid_checkin_checkout_dates)
        observableEmissionsOnSearchError(PackageApiError.Code.pkg_hotel_no_longer_available)
    }

    fun observableEmissionsOnSearchError(apiError: PackageApiError.Code) {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val searchApiObservableTestSubscriber = TestSubscriber.create<PackageApiError.Code>()
        subjectUnderTest.packageSearchApiErrorObserver.subscribe(searchApiObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)


        subjectUnderTest.paramsSubject.onNext(getPackageSearchParams())

        subjectUnderTest.packageSearchApiErrorObserver.onNext(apiError)
        var expectedErrorMessage = when (apiError) {
            PackageApiError.Code.pkg_destination_resolution_failed -> "Could not resolve a destination for $destinationShortName"
            PackageApiError.Code.pkg_origin_resolution_failed -> "Could not resolve an origin for $originShortName"
            else -> RuntimeEnvironment.application.getString(R.string.error_package_search_message)
        }

        errorMessageObservableTestSubscriber.assertValues(expectedErrorMessage)
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

    @Test fun observableEmissionsOnPaymentCardApiError() {
        observableEmissionsOnPaymentApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS, "nameOnCard", R.string.error_name_on_card_mismatch)
        observableEmissionsOnPaymentApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS, "creditCardNumber", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS, "expirationDate", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS, "cvv", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS, "cardLimitExceeded", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError(ApiError.Code.PAYMENT_FAILED, "cvv", R.string.e3_error_checkout_payment_failed)
    }

    private fun observableEmissionsOnPaymentApiError(errorCode: ApiError.Code, field: String, @StringRes errorMessageId: Int) {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val checkoutCardErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.checkoutCardErrorObservable.subscribe(checkoutCardErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val titleObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.titleObservable.subscribe(titleObservableTestSubscriber)

        val subtitleObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.subTitleObservable.subscribe(subtitleObservableTestSubscriber)

        val apiError = ApiError(errorCode)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.field = field

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        checkoutCardErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_payment)
        errorMessageObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(errorMessageId))
        errorButtonObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.edit_payment))
        titleObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.payment_failed_label))
        subtitleObservableTestSubscriber.assertValues("")
    }

    @Test fun observableEmissionsOnUnknownError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val checkoutUnknownErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.checkoutUnknownErrorObservable.subscribe(checkoutUnknownErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN);

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO::PACKAGE_CHECKOUT_UNKNOWN", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        checkoutUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = Phrase.from(getContext(), R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
    }

    @Test fun observableEmissionsOnCreateTripUnknownError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val createTripUnknownErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.createTripUnknownErrorObservable.subscribe(createTripUnknownErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.UNKNOWN_ERROR);
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "UK"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:UK:UNKNOWN_ERROR", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        createTripUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = Phrase.from(getContext(), R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
    }

    @Test fun observableEmissionsOnCreateTripDateMismatchError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val createTripUnknownErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.createTripUnknownErrorObservable.subscribe(createTripUnknownErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "UK"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:UK:PACKAGE_DATE_MISMATCH_ERROR", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        createTripUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        errorMessageObservableTestSubscriber.assertValues("Sorry, this flight arrives way before the hotel check-in time. Please pick a different hotel or flights.")
        errorButtonObservableTestSubscriber.assertValues("Retry")
    }

    @Test fun observableEmissionsOnHotelOffersError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val hotelOfferErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(hotelOfferErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR);
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "Atlantis"
        apiError.errorInfo.sourceErrorId = "K2401"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:Atlantis:K2401", checkoutError)

        subjectUnderTest.hotelOffersApiErrorObserver.onNext(apiError.errorCode)
        subjectUnderTest.defaultErrorObservable.onNext(Unit)

        hotelOfferErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_search)
        errorMessageObservableTestSubscriber.assertValues(getContext().getString(R.string.error_package_search_message))
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.edit_search))
    }
}
