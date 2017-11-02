package com.expedia.bookings.test.robolectric

import android.support.annotation.StringRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.vm.HotelErrorViewModel
import com.squareup.phrase.Phrase
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelErrorViewModelTest {
    lateinit private var subjectUnderTest: HotelErrorViewModel
    lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun before() {
        subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test fun observableEmissionsOnSoldOutApiError() {
        validateImageErrorMessageButtonTextForError(R.drawable.error_default,
                RuntimeEnvironment.application.getString(R.string.error_room_sold_out),
                RuntimeEnvironment.application.getString(R.string.select_another_room),
                ApiError.Code.HOTEL_ROOM_UNAVAILABLE)

        val soldOutObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.soldOutObservable.subscribe(soldOutObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        soldOutObservableTestSubscriber.assertValues(Unit)
    }

    @Test fun observableEmissionsOnPaymentCardApiError() {
        observableEmissionsOnPaymentApiError("creditCardNumber", null, null, R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError("expirationDate", "USA", "4232", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError("cvv", null, "3212", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError("cardLimitExceeded", "Atlantis", null, R.string.e3_error_checkout_payment_failed)
    }

    @Test fun observableEmissionsOnPaymentNameOnCardApiError() {
        observableEmissionsOnPaymentApiError("nameOnCard", null, null, R.string.error_name_on_card_mismatch)
    }

    @Test fun observableSearchInvalidInputError() {
        val imageId = R.drawable.error_search
        val errorMessage = RuntimeEnvironment.application.getString(R.string.error_no_result_message)
        val buttonText = RuntimeEnvironment.application.getString(R.string.edit_search)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val error = createInvalidInputApiError("field")
        subjectUnderTest.searchApiErrorObserver.onNext(error)

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "INVALID_INPUT:field")
    }

    @Test fun observablePinnedSearchInvalidInputError() {
        val param = createPinnedSearchparams()
        subjectUnderTest.paramsSubject.onNext(param)

        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_result_message),
                RuntimeEnvironment.application.getString(R.string.edit_search),
                ApiError.Code.INVALID_INPUT)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        val error = createInvalidInputApiError("pinnedField")
        subjectUnderTest.searchApiErrorObserver.onNext(error)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "INVALID_INPUT:pinnedField")
    }

    @Test fun observableSearchInvalidInputErrorNoField() {
        val error = createInvalidInputApiError()
        subjectUnderTest.searchApiErrorObserver.onNext(error)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "INVALID_INPUT:")
    }

    @Test fun observableSearchNoResultError() {
        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_result_message),
                RuntimeEnvironment.application.getString(R.string.edit_search),
                ApiError.Code.HOTEL_SEARCH_NO_RESULTS)

        val searchErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.searchErrorObservable.subscribe(searchErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        searchErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "HOTEL_SEARCH_NO_RESULTS")
    }

    @Test fun observableMapSearchNoResultError() {
        val titleObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.titleObservable.subscribe(titleObservableTestSubscriber)

        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_result_message),
                RuntimeEnvironment.application.getString(R.string.edit_search),
                ApiError.Code.HOTEL_MAP_SEARCH_NO_RESULTS)

        titleObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.visible_map_area))

        val searchErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.searchErrorObservable.subscribe(searchErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        searchErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "HOTEL_MAP_SEARCH_NO_RESULTS")
    }

    @Test fun observableFilterNoResultError() {
        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_filter_result_message),
                RuntimeEnvironment.application.getString(R.string.reset_filter),
                ApiError.Code.HOTEL_FILTER_NO_RESULTS)

        val filterNoResultObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.filterNoResultsObservable.subscribe(filterNoResultObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        filterNoResultObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "HOTEL_FILTER_NO_RESULTS")
    }

    @Test fun observablePinnedSearchNotFoundError() {
        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_pinned_result_message),
                RuntimeEnvironment.application.getString(R.string.nearby_results),
                ApiError.Code.HOTEL_PINNED_NOT_FOUND)

        val pinnedNotFoundErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.pinnedNotFoundToNearByHotelObservable.subscribe(pinnedNotFoundErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        pinnedNotFoundErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.SelectedHotelNotFound", "Selected hotel not returned in position 0")
    }

    @Test fun observableUnknownPinnedSearchError() {
        val param = createPinnedSearchparams()
        subjectUnderTest.paramsSubject.onNext(param)

        val message = Phrase.from(RuntimeEnvironment.application, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_default,
                message,
                RuntimeEnvironment.application.getString(R.string.retry),
                ApiError.Code.UNKNOWN_ERROR)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "UNKNOWN_ERROR")
    }

    @Test fun observableUnknownSearchError() {
        val message = Phrase.from(RuntimeEnvironment.application, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        validateSearchApiImageErrorMessageButtonTextForError(R.drawable.error_default,
                message,
                RuntimeEnvironment.application.getString(R.string.retry),
                ApiError.Code.UNKNOWN_ERROR)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "UNKNOWN_ERROR")
    }

    @Test fun observableSearchEmptyError() {
        val imageId = R.drawable.error_default
        val errorMessage = Phrase.from(RuntimeEnvironment.application, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        val buttonText = RuntimeEnvironment.application.getString(R.string.retry)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        subjectUnderTest.searchApiErrorObserver.onNext(ApiError())

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "UNMAPPED_ERROR")
    }

    @Test fun observableSearchUnmappedError() {
        val imageId = R.drawable.error_default
        val errorMessage = Phrase.from(RuntimeEnvironment.application, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        val buttonText = RuntimeEnvironment.application.getString(R.string.retry)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val error = ApiError()
        val errorInfo = ApiError.ErrorInfo()
        errorInfo.summary = "Summary"
        errorInfo.source = "Source"
        errorInfo.sourceErrorId = "SourceErrorId"
        errorInfo.field = "Field"
        errorInfo.cause = "Cause"
        errorInfo.couponErrorType = "CouponErrorType"
        error.errorInfo = errorInfo
        subjectUnderTest.searchApiErrorObserver.onNext(error)

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Search.NoResults", "UNMAPPED_ERROR")
    }

    @Test fun observableInfositeNoCodeApiErrorObserver() {
        val imageId = R.drawable.error_default
        val errorMessage = Phrase.from(RuntimeEnvironment.application, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        val buttonText = RuntimeEnvironment.application.getString(R.string.retry)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val error = ApiError()
        subjectUnderTest.infositeApiErrorObserver.onNext(error)

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Infosite.Error", "UNMAPPED_ERROR")
    }

    @Test fun observableInfositeApiErrorObserver() {
        val imageId = R.drawable.error_default
        val errorMessage = Phrase.from(RuntimeEnvironment.application, R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        val buttonText = RuntimeEnvironment.application.getString(R.string.retry)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val error = ApiError()
        error.errorCode = ApiError.Code.REGION_BLOCKED
        subjectUnderTest.infositeApiErrorObserver.onNext(error)

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)

        val defaultErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)

        validateOmnitureTracking("App.Hotels.Infosite.Error", "REGION_BLOCKED")
    }

    private fun validateImageErrorMessageButtonTextForError(imageId: Int, errorMessage: String, buttonText: String, errorCode: ApiError.Code) {
        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        subjectUnderTest.apiErrorObserver.onNext(ApiError(errorCode))

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)
    }

    private fun validateSearchApiImageErrorMessageButtonTextForError(imageId: Int, errorMessage: String, buttonText: String, errorCode: ApiError.Code) {
        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        subjectUnderTest.searchApiErrorObserver.onNext(ApiError(errorCode))

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)
    }

    private fun validateOmnitureTracking(pageName: String, error: String) {
        OmnitureTestUtils.assertStateTracked(
                pageName,
                Matchers.allOf(
                        OmnitureMatchers.withEvars(mapOf(2 to "D=c2", 18 to pageName)),
                        OmnitureMatchers.withProps(mapOf(2 to "hotels", 36 to error))),
                mockAnalyticsProvider)
    }

    private fun createPinnedSearchparams(): HotelSearchParams {
        val suggestion = SuggestionV4()
        suggestion.hotelId = "some-id"
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "some name"
        suggestion.regionNames = regionName
        val param = HotelSearchParams(suggestion,
                LocalDate(), LocalDate(),
                1, ArrayList<Int>(),
                false)
        return param
    }

    private fun createInvalidInputApiError(field: String? = null): ApiError {
        val error = ApiError(ApiError.Code.INVALID_INPUT)
        val errorInfo = ApiError.ErrorInfo()
        if (field != null) {
            errorInfo.field = field
        }
        error.errorInfo = errorInfo
        return error
    }

    private fun observableEmissionsOnPaymentApiError(field: String, source: String?, sourceErrorId: String?, @StringRes errorMessageId: Int) {
        subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)

        val checkoutCardErrorObservableTestSubscriber = TestObserver.create<Unit>()
        subjectUnderTest.checkoutCardErrorObservable.subscribe(checkoutCardErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestObserver.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val titleObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.titleObservable.subscribe(titleObservableTestSubscriber)

        val subtitleObservableTestSubscriber = TestObserver.create<String>()
        subjectUnderTest.subTitleObservable.subscribe(subtitleObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.field = field
        apiError.errorInfo.source = source
        apiError.errorInfo.sourceErrorId = sourceErrorId

        val checkoutError = HotelTracking.createCheckoutError(apiError)

        assertEquals(validateError(apiError.errorCode!!, apiError.errorInfo.source, apiError.errorInfo.sourceErrorId), checkoutError)

        subjectUnderTest.apiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        checkoutCardErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_payment)
        errorMessageObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(errorMessageId))
        errorButtonObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.edit_payment))
        titleObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.payment_failed_label))
        subtitleObservableTestSubscriber.assertValues("")
    }

    private fun validateError(errorCode: ApiError.Code, source: String?, sourceErrorId: String?): String {
        var errorCheck = "CKO:"
        errorCheck += if (!source.isNullOrEmpty()) "${source}:" else ":"
        errorCheck += if (!sourceErrorId.isNullOrEmpty()) "${sourceErrorId}" else "${errorCode}"
        return errorCheck
    }
}
