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
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PayWithPointsWidget
import com.expedia.bookings.widget.PaymentWidgetV2
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PayWithPointsViewModel
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class PayWithPointsViewModelTest {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    public var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var createTripResponse: HotelCreateTripResponse by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private val payWithPointsViewModelTestSubject = PublishSubject.create<Pair<String, Boolean>>()

    private var payWithPointsViewModel by notNullAndObservable<IPayWithPointsViewModel> {
        it.burnAmountUpdate.subscribe(burnAmountUpdateTestSubscriber)
        it.totalPointsAndAmountAvailableToRedeem.subscribe(totalPointsAndAmountAvailableToRedeemTestSubscriber)
        it.currencySymbol.subscribe(currencySymbolTestSubscriber)
        it.pointsAppliedMessage.subscribe(pointsAppliedMessageTestSubscriber)
        it.pointsAppliedMessage.subscribe(payWithPointsViewModelTestSubject)
    }

    private val burnAmountUpdateTestSubscriber = TestSubscriber.create<String>()
    private val totalPointsAndAmountAvailableToRedeemTestSubscriber = TestSubscriber.create<String>()
    private val currencySymbolTestSubscriber = TestSubscriber.create<String>()
    private val pointsAppliedMessageTestSubscriber = TestSubscriber.create<Pair<String, Boolean>>()

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

        payWithPointsViewModel.pwpOpted.onNext(true)

        pwpWidget.payWithPointsViewModel = payWithPointsViewModel
        paymentModel.createTripSubject.onNext(createTripResponse)
    }

    @Test
    fun testSubscribersAfterCreateTrip() {
        totalPointsAndAmountAvailableToRedeemTestSubscriber.assertNoErrors()
        totalPointsAndAmountAvailableToRedeemTestSubscriber.assertValue("$100.00 available (1,000 Expedia+ points)")

        burnAmountUpdateTestSubscriber.assertNoErrors()
        burnAmountUpdateTestSubscriber.assertValue("100.00")

        currencySymbolTestSubscriber.assertNoErrors()
        currencySymbolTestSubscriber.assertValue("$")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValue(Pair("1,000 points applied", true))
    }

    @Test
    fun userEntersBlankEditBoxAndSubmit() {
        payWithPointsViewModel.userEnteredBurnAmount.onNext("")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))
    }

    @Test
    fun userEntersMoreValueThanTripTotal() {
        payWithPointsViewModel.userEnteredBurnAmount.onNext("140")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("The points value can not exceed the payment due today.", false))
    }

    @Test
    fun userEntersMoreValueThanAvailablePoints() {
        payWithPointsViewModel.userEnteredBurnAmount.onNext("110")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("The points value exceeds your available balance.\\nPlease enter $100.00 or less.", false))
    }

    @Test
    fun userEntersZeroAmount() {
        payWithPointsViewModel.userEnteredBurnAmount.onNext("0")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))
    }

    @Test
    fun userEntersAmountLessThanTripTotalAndAvailablePoints() {
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("14,005 points applied", true))
    }

    @Test
    fun userEntersAmountLessThanTripTotalAndAvailablePointsBeforePreviousAPIResponse() {
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }

        createTripResponse.tripId = "happy|500"
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        createTripResponse.tripId = "happy_avaliable_points_less_than_trip"
        payWithPointsViewModel.userEnteredBurnAmount.onNext("30")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("Calculating points…", true), Pair("300 points applied", true))
    }

    @Test
    fun userTogglePwpSwitch() {
        //Toggle switch off
        payWithPointsViewModel.pwpOpted.onNext(false)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))

        //Toggle switch on
        val latch1 = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch1.countDown() }
        payWithPointsViewModel.pwpOpted.onNext(true)
        latch1.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true), Pair("Calculating points…", true), Pair("14,005 points applied", true))

        //New value entered and before calculation API response, PwP toggle off (call gets ignored)
        createTripResponse.tripId = "happy|500"
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        payWithPointsViewModel.pwpOpted.onNext(false)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true), Pair("Calculating points…", true),
                Pair("14,005 points applied", true), Pair("Calculating points…", true), Pair("0 points applied", true))

        //Toggle switch on, last entered value in pwp edit box is used to make API call
        createTripResponse.tripId = "happy"
        val latch3 = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch3.countDown() }
        payWithPointsViewModel.pwpOpted.onNext(true)
        latch3.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true), Pair("Calculating points…", true),
                Pair("14,005 points applied", true), Pair("Calculating points…", true), Pair("0 points applied", true), Pair("Calculating points…", true), Pair("14,005 points applied", true))
    }

    @Test
    fun clearButtonClicked() {
        payWithPointsViewModel.clearUserEnteredBurnAmount.onNext(Unit)

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))
    }

    @Test
    fun userEntersPointAndAPIErrorThrown() {
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("An unknown error occurred. Please try again.", false))
    }

    @Test
    fun pointsNotRedeemable() {
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("An unknown error occurred. Please try again.", false))
    }

    @Test
    fun pwpCurrencyToPoints_TripServiceError() {
        createTripResponse.tripId = "trip_service_error";
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("We're sorry but we experienced a server problem. Please try again.", false))
    }

    @Test
    fun pwpCurrencyToPoints_PointsConversionUnauthenticated() {
        createTripResponse.tripId = "points_conversion_unauthenticated";

        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("We're sorry but we experienced a server problem. Please try again.", false))
    }

    @Test
    fun testingMixedScenarios() {
        val expectedMessagesList = ArrayList<Pair<String, Boolean>>()

        //Default Splits
        expectedMessagesList.add(Pair("1,000 points applied", true))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User entered amount 20
        val latch1 = CountDownLatch(2)
        payWithPointsViewModelTestSubject.subscribe { latch1.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("20")
        latch1.await(10, TimeUnit.SECONDS)

        expectedMessagesList.add(Pair("Calculating points…", true))
        expectedMessagesList.add(Pair("14,005 points applied", true))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User again entered 20 which should be ignored
        payWithPointsViewModel.userEnteredBurnAmount.onNext("20")

        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User entered 30 but gets API Error
        createTripResponse.tripId = "trip_service_error";
        val latch2 = CountDownLatch(2)
        payWithPointsViewModelTestSubject.subscribe { latch2.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("30")
        latch2.await(10, TimeUnit.SECONDS)

        expectedMessagesList.add(Pair("Calculating points…", true))
        expectedMessagesList.add(Pair("We're sorry but we experienced a server problem. Please try again.", false))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User entered value exceeds available balance
        createTripResponse.tripId = "happy";

        payWithPointsViewModel.userEnteredBurnAmount.onNext("110")

        expectedMessagesList.add(Pair("The points value exceeds your available balance.\\nPlease enter $100.00 or less.", false))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User entered amount 20
        val latch3 = CountDownLatch(2)
        payWithPointsViewModelTestSubject.subscribe { latch3.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("20")
        latch3.await(10, TimeUnit.SECONDS)

        expectedMessagesList.add(Pair("Calculating points…", true))
        expectedMessagesList.add(Pair("14,005 points applied", true))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User press back
        paymentModel.discardPendingCurrencyToPointsAPISubscription.onNext(Unit)
        expectedMessagesList.add(Pair("14,005 points applied", true))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

        //User entered 0 amount
        payWithPointsViewModel.userEnteredBurnAmount.onNext("0")
        expectedMessagesList.add(Pair("0 points applied", true))
        assertExpectedValuesOfSubscriber(pointsAppliedMessageTestSubscriber, expectedMessagesList)

    }

    private fun <T> assertExpectedValuesOfSubscriber(testSubscriber: TestSubscriber<T>, expectedValues: List<T>) {
        testSubscriber.assertNoErrors()
        testSubscriber.assertValueCount(expectedValues.size)
        testSubscriber.assertReceivedOnNext(expectedValues)
    }
}
