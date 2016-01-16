package com.expedia.vm.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.ServicesRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.IPayWithPointsViewModel
import com.expedia.bookings.widget.PayWithPointsViewModel
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

        createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsLessThanTripTotalCreateTripResponse()
        createTripResponse.tripId = "happy";
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        payWithPointsViewModel = PayWithPointsViewModel(paymentModel, activity.application.resources)
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
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "14005 points applied")

        payWithPointsViewModel.pwpStateChange.onNext(false)

        pwpConversionResponseTestSubscriber.assertValueCount(4)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "14005 points applied", "0 points applied")

        val latch2 = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch2.countDown() }
        payWithPointsViewModel.pwpStateChange.onNext(true)
        latch2.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(6)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "14005 points applied", "0 points applied",
                "Calculating points…", "14005 points applied")
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
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "Error: connection failed")
    }

    @Test
    public fun pointsNotRedeemable() {
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.amountSubmittedByUser.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pwpConversionResponseTestSubscriber.assertValueCount(3)
        pwpConversionResponseTestSubscriber.assertValues("1000 points applied", "Calculating points…", "Error: connection failed")
    }
}
