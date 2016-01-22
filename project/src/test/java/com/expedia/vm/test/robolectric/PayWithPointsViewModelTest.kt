package com.expedia.vm.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.ServicesRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.IPayWithPointsViewModel
import com.expedia.bookings.widget.PayWithPointsViewModel
import com.expedia.bookings.widget.PayWithPointsWidget
import com.expedia.bookings.widget.PaymentWidgetV2
import com.expedia.util.notNullAndObservable
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
public class PayWithPointsViewModelTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    public var loyaltyServiceRule = ServicesRule<LoyaltyServices>(LoyaltyServices::class.java)
        @Rule get

    private var createTripResponse: HotelCreateTripResponse by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()

    private var payWithPointsViewModel by notNullAndObservable<IPayWithPointsViewModel> {
        it.updateAmountOfEditText.subscribe(updateAmountOfEditTextTestSubscriber)
        it.totalPointsAndAmountAvailableToRedeem.subscribe(totalPointsAndAmountAvailableToRedeemTestSubscriber)
        it.currencySymbol.subscribe(currencySymbolTestSubscriber)
        it.pwpConversionResponse.subscribe(pwpConversionResponseTestSubscriber)
    }

    private val updateAmountOfEditTextTestSubscriber = TestSubscriber.create<String>()
    private val totalPointsAndAmountAvailableToRedeemTestSubscriber = TestSubscriber.create<String>()
    private val currencySymbolTestSubscriber = TestSubscriber.create<String>()
    private val pwpConversionResponseTestSubscriber = TestSubscriber.create<String>()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        Ui.getApplication(activity).defaultHotelComponents()
        val paymentWidget = LayoutInflater.from(activity).inflate(R.layout.payment_widget_v2, null) as PaymentWidgetV2
        val pwpWidget = paymentWidget.findViewById(R.id.pwp_widget) as PayWithPointsWidget

        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsLessThanTripTotalCreateTripResponse()
        createTripResponse.tripId = "happy";
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        payWithPointsViewModel = PayWithPointsViewModel(paymentModel, activity.application.resources)
        pwpWidget.payWithPointsViewModel = payWithPointsViewModel
        paymentModel.createTripSubject.onNext(createTripResponse)
    }

    @Test
    public fun testSubscribersAfterCreateTrip() {
        totalPointsAndAmountAvailableToRedeemTestSubscriber.assertNoErrors()
        totalPointsAndAmountAvailableToRedeemTestSubscriber.assertValueCount(1)
        totalPointsAndAmountAvailableToRedeemTestSubscriber.assertValue("$100.00 (1000 points) available")

        updateAmountOfEditTextTestSubscriber.assertNoErrors()
        updateAmountOfEditTextTestSubscriber.assertValueCount(1)
        updateAmountOfEditTextTestSubscriber.assertValue("100.00")

        currencySymbolTestSubscriber.assertNoErrors()
        currencySymbolTestSubscriber.assertValueCount(1)
        currencySymbolTestSubscriber.assertValue("$")

        pwpConversionResponseTestSubscriber.assertNoErrors()
        pwpConversionResponseTestSubscriber.assertValueCount(1)
        pwpConversionResponseTestSubscriber.assertValue("1000 points applied")
    }

    @Test
    public fun userEntersBlankEditBoxAndSubmit() {
        payWithPointsViewModel.amountSubmittedByUser.onNext("")

        pwpConversionResponseTestSubscriber.assertNoErrors()
        pwpConversionResponseTestSubscriber.assertValueCount(2)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied")
    }

    @Test
    public fun userEntersMoreValueThanTripTotal() {
        payWithPointsViewModel.amountSubmittedByUser.onNext("140")

        pwpConversionResponseTestSubscriber.assertNoErrors()
        pwpConversionResponseTestSubscriber.assertValueCount(2)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "The points value can not exceed the payment due today.")
    }

    @Test
    public fun userEntersMoreValueThanAvailablePoints() {
        payWithPointsViewModel.amountSubmittedByUser.onNext("110")

        pwpConversionResponseTestSubscriber.assertNoErrors()
        pwpConversionResponseTestSubscriber.assertValueCount(2)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "The points value exceeds your available balance.\\nPlease enter $100.00 or less.")
    }

    @Test
    public fun userEntersZeroAmount() {
        payWithPointsViewModel.amountSubmittedByUser.onNext("0")

        pwpConversionResponseTestSubscriber.assertNoErrors()
        pwpConversionResponseTestSubscriber.assertValueCount(2)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied")
    }

    @Test
    public fun userEntersAmountLessThanTripTotalAndAvailablePoints() {
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "14005 points applied")
    }

    @Test
    public fun userEntersAmountLessThanTripTotalAndAvailablePointsBeforePreviousAPIResponse() {
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        createTripResponse.tripId = "happy_avaliable_points_less_than_trip"
        payWithPointsViewModel.amountSubmittedByUser.onNext("30")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(4)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "Calculating points…", "300 points applied")
    }

    @Test
    public fun userTogglePwpSwitch() {
        //Toggle switch off
        payWithPointsViewModel.pwpStateChange.onNext(false)

        pwpConversionResponseTestSubscriber.assertValueCount(2)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied")

        //Toggle switch on
        val latch1 = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch1.countDown() }
        payWithPointsViewModel.pwpStateChange.onNext(true)
        latch1.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(4)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied", "Calculating points…", "14005 points applied")

        //New value entered and before calculation API response, PwP toggle off (call gets ignored)
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        payWithPointsViewModel.pwpStateChange.onNext(false)

        pwpConversionResponseTestSubscriber.assertValueCount(6)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied", "Calculating points…",
                "14005 points applied", "Calculating points…", "0 points applied")

        //Toggle switch on, last entered value in pwp edit box is used to make API call
        val latch3 = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch3.countDown() }
        payWithPointsViewModel.pwpStateChange.onNext(true)
        latch3.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(8)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied", "Calculating points…",
                "14005 points applied", "Calculating points…", "0 points applied", "Calculating points…", "14005 points applied")
    }

    @Test
    public fun clearButtonClicked() {
        payWithPointsViewModel.clearButtonClick.onNext(Unit)

        pwpConversionResponseTestSubscriber.assertNoErrors()
        pwpConversionResponseTestSubscriber.assertValueCount(2)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "0 points applied")
    }

    @Test
    public fun userEntersPointAndAPIErrorThrown() {
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "An unknown error occurred. Please try again.")
    }

    @Test
    public fun pointsNotRedeemable() {
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "An unknown error occurred. Please try again.")
    }

    @Test
    public fun pwpCurrencyToPoints_TripServiceError() {
        createTripResponse.tripId = "trip_service_error";
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "We're sorry but we experienced a server problem. Please try again.")
    }

    @Test
    public fun pwpCurrencyToPoints_PointsConversionUnauthenticated() {
        createTripResponse.tripId = "points_conversion_unauthenticated";
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "We're sorry but we experienced a server problem. Please try again.")
    }
}
