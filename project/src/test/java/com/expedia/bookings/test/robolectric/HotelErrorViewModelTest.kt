package com.expedia.bookings.test.robolectric

import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.vm.HotelErrorViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class HotelErrorViewModelTest {

    @Test fun observableEmissionsOnSoldOutApiError() {
        val subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)

        val soldOutObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.soldOutObservable.subscribe(soldOutObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonOneTextObservable.subscribe(errorButtonObservableTestSubscriber)

        subjectUnderTest.apiErrorObserver.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        soldOutObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        errorMessageObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.error_room_sold_out))
        errorButtonObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.select_another_room))
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

    private fun observableEmissionsOnPaymentApiError(field: String, source: String?, sourceErrorId: String?, @StringRes errorMessageId: Int) {
        val subjectUnderTest = HotelErrorViewModel(RuntimeEnvironment.application)

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

        val apiError = ApiError(ApiError.Code.HOTEL_CHECKOUT_CARD_DETAILS);
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