package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import org.junit.runner.RunWith
import android.app.Activity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.ServicesRule
import com.expedia.vm.HotelCheckoutMainViewModel
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.concurrent.CountDownLatch
import kotlin.properties.Delegates
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Test
import com.expedia.bookings.data.TripBucketItemHotelV2
import org.junit.Rule
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class HotelCheckoutMainViewModelTest {

    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    public var loyaltyServiceRule = ServicesRule<LoyaltyServices>(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var sut: HotelCheckoutMainViewModel by Delegates.notNull()

    @Before
    fun before(){
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        sut = HotelCheckoutMainViewModel(paymentModel)
    }

    @Test
    fun testUpdateEarnedRewardsPoint(){
        val updateEarnPointsText = TestSubscriber.create<Int>()
        sut.updateEarnedRewards.subscribe(updateEarnPointsText)
        paymentModel.createTripSubject.onNext(getCreateTripResponse())

        updateEarnPointsText.assertValueCount(1)

        //When user chooses to pay through card and reward points
        val latch = CountDownLatch(1)
        paymentModel.burnAmountToPointsApiResponse.subscribe { latch.countDown() }
        paymentModel.burnAmountSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)

        updateEarnPointsText.assertValueCount(2)
        updateEarnPointsText.assertValues(1000,507)

    }

    private fun getCreateTripResponse(): HotelCreateTripResponse {
        var createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        createTripResponse.tripId = "happy"
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        return createTripResponse
    }
}

