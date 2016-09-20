package com.expedia.bookings.test.robolectric

import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.vm.HotelErrorViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

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
        observableEmissionsOnPaymentApiError("creditCardNumber", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError("expirationDate", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError("cvv", R.string.e3_error_checkout_payment_failed)
        observableEmissionsOnPaymentApiError("cardLimitExceeded", R.string.e3_error_checkout_payment_failed)
    }

    @Test fun observableEmissionsOnPaymentNameOnCardApiError() {
        observableEmissionsOnPaymentApiError("nameOnCard", R.string.error_name_on_card_mismatch)
    }

    private fun observableEmissionsOnPaymentApiError(field: String, @StringRes errorMessageId: Int) {
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

        subjectUnderTest.apiErrorObserver.onNext(apiError)
        subjectUnderTest.errorButtonClickedObservable.onNext(Unit)

        checkoutCardErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_payment)
        errorMessageObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(errorMessageId))
        errorButtonObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.edit_payment))
        titleObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.payment_failed_label))
        subtitleObservableTestSubscriber.assertValues("")
    }
}