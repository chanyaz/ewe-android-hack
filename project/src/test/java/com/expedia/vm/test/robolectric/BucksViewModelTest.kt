package com.expedia.vm.test.robolectric

import android.app.Activity
import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplits
import com.expedia.bookings.data.payment.PointsAndCurrency
import com.expedia.bookings.data.payment.PointsType
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.vm.interfaces.IBucksViewModel
import com.expedia.vm.BucksViewModel
import com.expedia.bookings.widget.BucksWidget
import com.expedia.util.notNullAndObservable
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class BucksViewModelTest {
    var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()

    private var bucksViewModel by notNullAndObservable<IBucksViewModel> {
        it.updateToggle.subscribe(updateToggleTestSubscriber)
        it.pointsAppliedMessageColor.subscribe(pointsAppliedMessageColorTestSubscriber)
        it.bucksWidgetVisibility.subscribe(bucksWidgetVisibilityTestSubscriber)
    }

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    private val updateToggleTestSubscriber = TestObserver.create<Boolean>()
    private val pointsAppliedMessageColorTestSubscriber = TestObserver.create<Int>()
    private val bucksWidgetVisibilityTestSubscriber = TestObserver.create<Boolean>()
    private val paymentSplitsTestSubscriber = TestObserver<PaymentSplits>()

    private var enableColor: Int by Delegates.notNull()
    private var disableColor: Int by Delegates.notNull()

    private var fullPayableWithPointsPaymentSplits: PaymentSplits by Delegates.notNull()
    private var fullPayableWithCardPaymentSplits: PaymentSplits by Delegates.notNull()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        val bucksWidget = LayoutInflater.from(activity).inflate(R.layout.bucks_widget_stub, null) as BucksWidget

        enableColor = ContextCompat.getColor(getContext(), R.color.hotels_primary_color)
        disableColor = ContextCompat.getColor(getContext(), R.color.hotelsv2_checkout_text_color)

        val createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemableOrbucksCreateTripResponse()
        createTripResponse.tripId = "happy"
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        bucksViewModel = BucksViewModel(paymentModel, activity.application)

        paymentModel.paymentSplits.subscribe(paymentSplitsTestSubscriber)

        fullPayableWithPointsPaymentSplits = PaymentSplits(PointsAndCurrency(771.40f, PointsType.BURN, Money("771.40", "USD")),
                PointsAndCurrency(0f, PointsType.EARN, Money("0", "USD")))

        val payingWithPoints = PointsAndCurrency(0f, PointsType.BURN, Money("0", createTripResponse.getTripTotalExcludingFee().currencyCode))
        val payingWithCards = PointsAndCurrency(createTripResponse.rewards?.totalPointsToEarn ?: 0f, PointsType.EARN, createTripResponse.getTripTotalExcludingFee())
        fullPayableWithCardPaymentSplits = PaymentSplits(payingWithPoints, payingWithCards)

        bucksWidget.viewModel = bucksViewModel
        paymentModel.createTripSubject.onNext(createTripResponse)
    }

    @Test @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testSubscribersAfterCreateTrip() {
        updateToggleTestSubscriber.assertValueCount(1)
        pointsAppliedMessageColorTestSubscriber.assertValue(enableColor)
        bucksWidgetVisibilityTestSubscriber.assertValue(true)

        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.values()[0], fullPayableWithPointsPaymentSplits))
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun userToggleBucksSwitch() {
        bucksViewModel.bucksOpted.onNext(false)

        pointsAppliedMessageColorTestSubscriber.assertValues(enableColor, disableColor)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.values()[1], fullPayableWithCardPaymentSplits))

        bucksViewModel.bucksOpted.onNext(true)

        pointsAppliedMessageColorTestSubscriber.assertValues(enableColor, disableColor, enableColor)
        Assert.assertTrue(comparePaymentSplits(paymentSplitsTestSubscriber.values()[2], fullPayableWithPointsPaymentSplits))
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
