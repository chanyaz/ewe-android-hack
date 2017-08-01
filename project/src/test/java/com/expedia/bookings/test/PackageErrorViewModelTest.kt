package com.expedia.bookings.test

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.vm.packages.PackageErrorViewModel
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
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

        val searchApiObservableTestObserver = TestObserver.create<PackageApiError.Code>()
        subjectUnderTest.packageSearchApiErrorObserver.subscribe(searchApiObservableTestObserver)

        val errorMessageObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestObserver)


        subjectUnderTest.paramsSubject.onNext(getPackageSearchParams())

        subjectUnderTest.packageSearchApiErrorObserver.onNext(apiError)
        val expectedErrorMessage = when (apiError) {
            PackageApiError.Code.pkg_destination_resolution_failed -> "Sorry, we could not resolve the entered destination for $destinationShortName. Please retry."
            PackageApiError.Code.pkg_origin_resolution_failed -> "Sorry, we could not resolve the entered origin for $originShortName. Please retry."
            else -> RuntimeEnvironment.application.getString(R.string.error_package_search_message)
        }

        errorMessageObservableTestObserver.assertValues(expectedErrorMessage)
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

        val checkoutCardErrorObservableTestObserver = TestObserver.create<Unit>()
        subjectUnderTest.checkoutCardErrorObservable.subscribe(checkoutCardErrorObservableTestObserver)

        val errorImageObservableTestObserver = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestObserver)

        val errorMessageObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestObserver)

        val errorButtonObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestObserver)

        val titleObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.titleObservable.subscribe(titleObservableTestObserver)

        val subtitleObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.subTitleObservable.subscribe(subtitleObservableTestObserver)

        val apiError = ApiError(errorCode)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.field = field

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        checkoutCardErrorObservableTestObserver.assertValues(Unit)
        errorImageObservableTestObserver.assertValues(R.drawable.error_payment)
        errorMessageObservableTestObserver.assertValues(RuntimeEnvironment.application.getString(errorMessageId))
        errorButtonObservableTestObserver.assertValues(RuntimeEnvironment.application.getString(R.string.edit_payment))
        titleObservableTestObserver.assertValues(RuntimeEnvironment.application.getString(R.string.payment_failed_label))
        subtitleObservableTestObserver.assertValues("")
    }

    @Test fun observableEmissionsOnUnknownError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val checkoutUnknownErrorObservableTestObserver = TestObserver.create<Unit>()
        subjectUnderTest.checkoutUnknownErrorObservable.subscribe(checkoutUnknownErrorObservableTestObserver)

        val errorImageObservableTestObserver = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestObserver)

        val errorMessageObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestObserver)

        val errorButtonObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestObserver)

        val apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN)

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO::PACKAGE_CHECKOUT_UNKNOWN", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

<<<<<<< HEAD
        checkoutUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = getContext().getString(R.string.package_error_server)
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
=======
        checkoutUnknownErrorObservableTestObserver.assertValues(Unit)
        errorImageObservableTestObserver.assertValues(R.drawable.error_default)
        val message = Phrase.from(getContext(), R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservableTestObserver.assertValues(message)
        errorButtonObservableTestObserver.assertValues(getContext().getString(R.string.retry))
>>>>>>> 7df61dae81... WIP
    }

    @Test fun observableEmissionsOnCreateTripUnknownError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val createTripUnknownErrorObservableTestObserver = TestObserver.create<Unit>()
        subjectUnderTest.createTripUnknownErrorObservable.subscribe(createTripUnknownErrorObservableTestObserver)

        val errorImageObservableTestObserver = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestObserver)

        val errorMessageObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestObserver)

        val errorButtonObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestObserver)

        val apiError = ApiError(ApiError.Code.UNKNOWN_ERROR);
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "UK"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:UK:UNKNOWN_ERROR", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

<<<<<<< HEAD
        createTripUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = getContext().getString(R.string.package_error_server)
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
=======
        createTripUnknownErrorObservableTestObserver.assertValues(Unit)
        errorImageObservableTestObserver.assertValues(R.drawable.error_default)
        val message = Phrase.from(getContext(), R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservableTestObserver.assertValues(message)
        errorButtonObservableTestObserver.assertValues(getContext().getString(R.string.retry))
>>>>>>> 7df61dae81... WIP
    }

    @Test fun observableEmissionsOnCreateTripDateMismatchError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val createTripUnknownErrorObservableTestObserver = TestObserver.create<Unit>()
        subjectUnderTest.createTripUnknownErrorObservable.subscribe(createTripUnknownErrorObservableTestObserver)

        val errorImageObservableTestObserver = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestObserver)

        val errorMessageObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestObserver)

        val errorButtonObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestObserver)

        val apiError = ApiError(ApiError.Code.PACKAGE_DATE_MISMATCH_ERROR)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "UK"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:UK:PACKAGE_DATE_MISMATCH_ERROR", checkoutError)

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        createTripUnknownErrorObservableTestObserver.assertValues(Unit)
        errorImageObservableTestObserver.assertValues(R.drawable.error_default)
        errorMessageObservableTestObserver.assertValues("Sorry, this flight arrives way before the hotel check-in time. Please pick a different hotel or flights.")
        errorButtonObservableTestObserver.assertValues("Retry")
    }

    @Test fun observableEmissionsOnHotelOffersError() {
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val hotelOfferErrorObservableTestObserver = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(hotelOfferErrorObservableTestObserver)

        val errorImageObservableTestObserver = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestObserver)

        val errorMessageObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestObserver)

        val errorButtonObservableTestObserver = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestObserver)

        val apiError = ApiError(ApiError.Code.PACKAGE_SEARCH_ERROR);
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.source = "Atlantis"
        apiError.errorInfo.sourceErrorId = "K2401"

        val checkoutError = PackagesTracking().createCheckoutError(apiError)

        assertEquals("CKO:Atlantis:K2401", checkoutError)

        subjectUnderTest.hotelOffersApiErrorObserver.onNext(apiError.errorCode)
        subjectUnderTest.defaultErrorObservable.onNext(Unit)

        hotelOfferErrorObservableTestObserver.assertValues(Unit)
        errorImageObservableTestObserver.assertValues(R.drawable.error_search)
        errorMessageObservableTestObserver.assertValues(getContext().getString(R.string.error_package_search_message))
        errorButtonObservableTestObserver.assertValues(getContext().getString(R.string.edit_search))
    }
}
