package com.expedia.vm.test.robolectric

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.IOrbucksViewModel
import com.expedia.bookings.widget.OrbucksViewModel
import com.expedia.bookings.widget.OrbucksWidget
import com.expedia.util.notNullAndObservable
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class OrbucksViewModelTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()

    private var orbucksViewModel by notNullAndObservable<IOrbucksViewModel> {
        it.enableOrbucksToggle.subscribe(enableOrbucksToggleTestSubscriber)
        it.pointsAppliedMessageColor.subscribe(pointsAppliedMessageColorTestSubscriber)
        it.orbucksWidgetVisibility.subscribe(orbucksWidgetVisibilityTestSubscriber)
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    private val enableOrbucksToggleTestSubscriber = TestSubscriber.create<Unit>()
    private val pointsAppliedMessageColorTestSubscriber = TestSubscriber.create<Int>()
    private val orbucksWidgetVisibilityTestSubscriber = TestSubscriber.create<Boolean>()
    private val paymentSplitsTestSubscriber = TestSubscriber<PaymentSplits>()

    private var enableColor: Int by Delegates.notNull()
    private var disableColor: Int by Delegates.notNull()

    private var fullPayableWithPointsPaymentSplits: PaymentSplits by Delegates.notNull()
    private var fullPayableWithCardPaymentSplits: PaymentSplits by Delegates.notNull()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels)
        Ui.getApplication(activity).defaultHotelComponents()
        val orbucksWidget = LayoutInflater.from(activity).inflate(R.layout.orbucks_widget_stub, null) as OrbucksWidget

        enableColor = ContextCompat.getColor(getContext(), R.color.hotels_primary_color);
        disableColor = ContextCompat.getColor(getContext(), R.color.hotelsv2_checkout_text_color);

        val createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemableOrbucksCreateTripResponse()
        createTripResponse.tripId = "happy";
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        orbucksViewModel = OrbucksViewModel(paymentModel, activity.application)

        paymentModel.paymentSplits.subscribe(paymentSplitsTestSubscriber)

        fullPayableWithPointsPaymentSplits = PaymentSplits(PointsAndCurrency(771.40f, PointsType.BURN, Money("771.40", "USD")),
                PointsAndCurrency(0f, PointsType.EARN, Money("0", "USD")))

        val payingWithPoints = PointsAndCurrency(0f, PointsType.BURN, Money("0", createTripResponse.getTripTotal().currencyCode))
        val payingWithCards = PointsAndCurrency(createTripResponse.rewards?.totalPointsToEarn ?: 0f, PointsType.EARN, createTripResponse.getTripTotal())
        fullPayableWithCardPaymentSplits = PaymentSplits(payingWithPoints, payingWithCards)

        orbucksWidget.viewModel = orbucksViewModel
        paymentModel.createTripSubject.onNext(createTripResponse)
    }

    @Test
    fun testSubscribersAfterCreateTrip() {
        enableOrbucksToggleTestSubscriber.assertValueCount(1)
        pointsAppliedMessageColorTestSubscriber.assertValue(enableColor)
        orbucksWidgetVisibilityTestSubscriber.assertValue(true)

        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents[0], fullPayableWithPointsPaymentSplits))
    }

    @Test
    fun userToggleOrbucksSwitch() {
        orbucksViewModel.orbucksOpted.onNext(false)

        pointsAppliedMessageColorTestSubscriber.assertValues(enableColor, disableColor)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents[1], fullPayableWithCardPaymentSplits))

        orbucksViewModel.orbucksOpted.onNext(true)

        pointsAppliedMessageColorTestSubscriber.assertValues(enableColor, disableColor, enableColor)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.onNextEvents[2], fullPayableWithPointsPaymentSplits))
    }

    private fun comparePaymentSplits(paymentSplits: PaymentSplits, expectedPaymentSplits: PaymentSplits): Boolean {
        return paymentSplits.payingWithCards.amount.compareTo(expectedPaymentSplits.payingWithCards.amount) == 0 &&
                paymentSplits.payingWithCards.points.equals(expectedPaymentSplits.payingWithCards.points) &&
                paymentSplits.payingWithCards.pointsType.equals(expectedPaymentSplits.payingWithCards.pointsType) &&
                paymentSplits.payingWithPoints.amount.compareTo(expectedPaymentSplits.payingWithPoints.amount) == 0 &&
                paymentSplits.payingWithPoints.points.equals(expectedPaymentSplits.payingWithPoints.points) &&
                paymentSplits.payingWithPoints.pointsType.equals(expectedPaymentSplits.payingWithPoints.pointsType)
    }
}