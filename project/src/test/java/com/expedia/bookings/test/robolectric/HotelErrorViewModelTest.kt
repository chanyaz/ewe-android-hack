package com.expedia.bookings.test.robolectric

import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.vm.HotelErrorViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelErrorViewModelTest {
    lateinit private var subjectUnderTest: HotelErrorViewModel

    @Before
    fun before() {
        subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)
    }

    @Test fun observableEmissionsOnSoldOutApiError() {
        validateImageErrorMessageButtonTextForError(R.drawable.error_default,
                RuntimeEnvironment.application.getString(R.string.error_room_sold_out),
                RuntimeEnvironment.application.getString(R.string.select_another_room),
                ApiError.Code.HOTEL_ROOM_UNAVAILABLE)

        val soldOutObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.soldOutObservable.subscribe(soldOutObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        soldOutObservableTestSubscriber.assertValues(Unit)
    }

    @Test fun observableSearchInvalidInputError() {
        validateImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_result_message),
                RuntimeEnvironment.application.getString(R.string.edit_search),
                ApiError.Code.INVALID_INPUT)

        val defaultErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.defaultErrorObservable.subscribe(defaultErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        defaultErrorObservableTestSubscriber.assertValues(Unit)
    }

    @Test fun observablePinnedSearchNotFoundError() {
        validateImageErrorMessageButtonTextForError(R.drawable.error_search,
                RuntimeEnvironment.application.getString(R.string.error_no_pinned_result_message),
                RuntimeEnvironment.application.getString(R.string.nearby_results),
                ApiError.Code.HOTEL_PINNED_NOT_FOUND)

        val pinnedNotFoundErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.pinnedNotFoundToNearByHotelObservable.subscribe(pinnedNotFoundErrorObservableTestSubscriber)

        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)
        pinnedNotFoundErrorObservableTestSubscriber.assertValues(Unit)
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

    private fun validateImageErrorMessageButtonTextForError(imageId: Int, errorMessage: String, buttonText: String, errorCode: ApiError.Code) {
        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        subjectUnderTest.apiErrorObserver.onNext(ApiError(errorCode))

        errorImageObservableTestSubscriber.assertValues(imageId)
        errorMessageObservableTestSubscriber.assertValues(errorMessage)
        errorButtonObservableTestSubscriber.assertValues(buttonText)
    }

    private fun observableEmissionsOnPaymentApiError(field: String, source: String?, sourceErrorId: String?, @StringRes errorMessageId: Int) {
        subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)

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

        val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS)
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.field = field
        apiError.errorInfo.source = source
        apiError.errorInfo.sourceErrorId = sourceErrorId

        val checkoutError = HotelTracking.createCheckoutError(apiError)

        assertEquals(validateError(apiError.errorCode, apiError.errorInfo.source, apiError.errorInfo.sourceErrorId), checkoutError)

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