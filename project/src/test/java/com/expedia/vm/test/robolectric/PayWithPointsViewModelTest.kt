package com.expedia.vm.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.PayWithPointsWidget
import com.expedia.bookings.widget.PaymentWidgetV2
import com.expedia.model.UserLoginStateChangedModel
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PayWithPointsViewModel
import com.expedia.vm.ShopWithPointsViewModel
import com.expedia.vm.interfaces.IPayWithPointsViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import java.util.ArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class) @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
class PayWithPointsViewModelTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    //This test is so weird that it only works on Schedulers.io()
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java, Schedulers.io())
        @Rule get

    private var createTripResponse: HotelCreateTripResponse by Delegates.notNull()
    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var shopWithPointsViewModel: ShopWithPointsViewModel by Delegates.notNull()
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
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        val paymentWidget = LayoutInflater.from(activity).inflate(R.layout.payment_widget_v2, null) as PaymentWidgetV2
        val pwpWidget = paymentWidget.findViewById<View>(R.id.pwp_widget) as PayWithPointsWidget

        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        shopWithPointsViewModel = ShopWithPointsViewModel(activity.applicationContext, paymentModel, UserLoginStateChangedModel())
        payWithPointsViewModel = PayWithPointsViewModel(paymentModel, shopWithPointsViewModel, activity.applicationContext)

        pwpWidget.payWithPointsViewModel = payWithPointsViewModel
    }

    @Test
    fun testSubscribersAfterCreateTrip() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
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
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        payWithPointsViewModel.userEnteredBurnAmount.onNext("")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))
    }

    @Test
    fun userEntersMoreValueThanTripTotal() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        payWithPointsViewModel.userEnteredBurnAmount.onNext("140")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("The points value can not exceed the payment due today.", false))
    }

    @Test
    fun userEntersMoreValueThanAvailablePoints() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        payWithPointsViewModel.userEnteredBurnAmount.onNext("110")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("The points value exceeds your available balance.\nPlease enter $100.00 or less.", false))
    }

    @Test
    fun userEntersZeroAmount() {
        createTripWithShopWithPointsOpted(true);
        optForPwp(true)
        payWithPointsViewModel.userEnteredBurnAmount.onNext("0")

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))
    }

    @Test
    fun userEntersAmountLessThanTripTotalAndAvailablePoints() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("14,005 points applied", true))
    }

    @Test
    fun userEntersAmountLessThanTripTotalAndAvailablePointsBeforePreviousAPIResponse() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)

        createTripResponse.tripId = "happy|5000"
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        createTripResponse.tripId = "happy_avaliable_points_less_than_trip"
        payWithPointsViewModel.userEnteredBurnAmount.onNext("30")
        pointsAppliedMessageTestSubscriber.awaitValueCount(4, 10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("Calculating points…", true), Pair("300 points applied", true))
    }

    @Test
    fun userTogglePwpSwitch() {
        createTripWithShopWithPointsOpted(true);
        //Toggle switch off
        optForPwp(false)

        pointsAppliedMessageTestSubscriber.assertValuesAndClear(Pair("1,000 points applied", true), Pair("0 points applied", true))

        //Toggle switch on
        payWithPointsViewModel.pwpOpted.onNext(true)
        pointsAppliedMessageTestSubscriber.awaitValueCount(2, 10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValuesAndClear(Pair("Calculating points…", true), Pair("14,005 points applied", true))

        //New value entered and before calculation API response, PwP toggle off (call gets ignored)
        createTripResponse.tripId = "happy|500"
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")

        payWithPointsViewModel.pwpOpted.onNext(false)

        pointsAppliedMessageTestSubscriber.assertValuesAndClear(Pair("Calculating points…", true), Pair("0 points applied", true))

        //Toggle switch on, last entered value in pwp edit box is used to make API call
        createTripResponse.tripId = "happy"
        payWithPointsViewModel.pwpOpted.onNext(true)
        pointsAppliedMessageTestSubscriber.awaitValueCount(2, 10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("Calculating points…", true), Pair("14,005 points applied", true))
    }

    @Test
    fun pwpToggleStateWithRespectToSwpToggleState() {
        //Toggle SwP OFF
        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(false)
        createTripWithShopWithPointsOpted(false)

        assertFalse(payWithPointsViewModel.pwpOpted.value)

        //Toggle Swp ON
        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(true)
        createTripWithShopWithPointsOpted(true)

        assertTrue(payWithPointsViewModel.pwpOpted.value)

        //Toggle Pwp OFF
        optForPwp(false)
        //Toggle Swp ON after you have toggled Pwp OFF
        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(true)
        createTripWithShopWithPointsOpted(true)

        assertTrue(payWithPointsViewModel.pwpOpted.value)

        //Toggle Pwp ON
        optForPwp(true)
        //Toggle Swp OFF after you have toggle Pwp ON
        shopWithPointsViewModel.shopWithPointsToggleObservable.onNext(false)
        createTripWithShopWithPointsOpted(false)

        assertFalse(payWithPointsViewModel.pwpOpted.value)
    }


    @Test
    fun clearButtonClicked() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        payWithPointsViewModel.clearUserEnteredBurnAmount.onNext(Unit)

        pointsAppliedMessageTestSubscriber.assertNoErrors()
        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("0 points applied", true))
    }

    @Test
    fun userEntersPointAndAPIErrorThrown() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("An unknown error occurred. Please try again.", false))
    }

    @Test
    fun pointsNotRedeemable() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        createTripResponse.tripId = "garbage";
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("An unknown error occurred. Please try again.", false))
    }

    @Test
    fun pwpCurrencyToPoints_TripServiceError() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        createTripResponse.tripId = "trip_service_error";
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("We're sorry but we experienced a server problem. Please try again.", false))
    }

    @Test
    fun pwpCurrencyToPoints_PointsConversionUnauthenticated() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
        createTripResponse.tripId = "points_conversion_unauthenticated";

        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiError.subscribe { latch.countDown() }
        payWithPointsViewModel.userEnteredBurnAmount.onNext("32")
        latch.await(10, TimeUnit.SECONDS)

        pointsAppliedMessageTestSubscriber.assertValues(Pair("1,000 points applied", true), Pair("Calculating points…", true), Pair("We're sorry but we experienced a server problem. Please try again.", false))
    }

    @Test
    fun testingMixedScenarios() {
        createTripWithShopWithPointsOpted(true)
        optForPwp(true)
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

        expectedMessagesList.add(Pair("The points value exceeds your available balance.\nPlease enter $100.00 or less.", false))
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

    private fun createTripWithShopWithPointsOpted(shopWithPoints: Boolean) {
        if (shopWithPoints) {
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsLessThanTripTotalCreateTripResponse()
        } else {
            createTripResponse = mockHotelServiceTestRule.getHappyCreateTripResponse()
        }
        createTripResponse.tripId = "happy";
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel.createTripSubject.onNext(createTripResponse)
    }

    private fun optForPwp(pwpToggledOn : Boolean) {
        payWithPointsViewModel.updatePwPToggle.onNext(pwpToggledOn)
    }
}
