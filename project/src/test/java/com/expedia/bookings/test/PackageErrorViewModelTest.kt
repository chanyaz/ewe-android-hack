package com.expedia.bookings.test

import android.content.Context
import android.support.annotation.StringRes
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.PackageErrorViewModel
import com.squareup.phrase.Phrase
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
class PackageErrorViewModelTest {
    private fun getContext(): Context {
        return RuntimeEnvironment.application
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
        val subjectUnderTest = PackageErrorViewModel(RuntimeEnvironment.application)

        val checkoutCardErrorObservableTestSubscriber = TestSubscriber.create<Unit>()
        subjectUnderTest.checkoutCardErrorObservable.subscribe(checkoutCardErrorObservableTestSubscriber)

        val errorImageObservableTestSubscriber = TestSubscriber.create<Int>()
        subjectUnderTest.imageObservable.subscribe(errorImageObservableTestSubscriber)

        val errorMessageObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.errorMessageObservable.subscribe(errorMessageObservableTestSubscriber)

        val errorButtonObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.buttonTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val titleObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.titleObservable.subscribe(titleObservableTestSubscriber)

        val subtitleObservableTestSubscriber = TestSubscriber.create<String>()
        subjectUnderTest.subTitleObservable.subscribe(subtitleObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.PACKAGE_CHECKOUT_CARD_DETAILS);
        apiError.errorInfo = ApiError.ErrorInfo()
        apiError.errorInfo.field = field

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.actionObservable.onNext(Unit)

        checkoutCardErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_payment)
        errorMessageObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(errorMessageId))
        errorButtonObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.edit_payment))
        titleObservableTestSubscriber.assertValues(RuntimeEnvironment.application.getString(R.string.hotel_payment_failed_text))
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
        subjectUnderTest.buttonTextObservable.subscribe(errorButtonObservableTestSubscriber)

        val apiError = ApiError(ApiError.Code.UNKNOWN_ERROR);

        subjectUnderTest.checkoutApiErrorObserver.onNext(apiError)
        subjectUnderTest.actionObservable.onNext(Unit)

        checkoutUnknownErrorObservableTestSubscriber.assertValues(Unit)
        errorImageObservableTestSubscriber.assertValues(R.drawable.error_default)
        val message = Phrase.from(getContext(), R.string.error_server_TEMPLATE)
                .put("brand", BuildConfig.brand)
                .format()
                .toString()
        errorMessageObservableTestSubscriber.assertValues(message)
        errorButtonObservableTestSubscriber.assertValues(getContext().getString(R.string.retry))
    }
}
