package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.test.MockHotelServiceTestRule
import com.expedia.bookings.test.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.vm.PaymentWidgetViewModel
import com.expedia.bookings.widget.PaymentWidgetV2
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.RoundImageView
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.User
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.TripBucketItemHotelV2
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.BillingInfo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
public class PaymentWidgetV2Test {
    public var mockHotelServiceTestRule: MockHotelServiceTestRule = MockHotelServiceTestRule()
        @Rule get

    public var loyaltyServiceRule = ServicesRule<LoyaltyServices>(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var sut: PaymentWidgetV2 by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    lateinit var paymentTileInfo: TextView
    lateinit var paymentTileOption: TextView
    lateinit var paymentTileIcon: RoundImageView
    lateinit var pwpSmallIcon: ImageView

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Hotels);
        Ui.getApplication(activity).defaultHotelComponents()
        sut = android.view.LayoutInflater.from(activity).inflate(R.layout.payment_widget_v2, null) as PaymentWidgetV2

        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        sut.paymentWidgetViewModel = PaymentWidgetViewModel(paymentModel, activity.application.resources)

        paymentTileInfo = sut.findViewById(R.id.card_info_name) as TextView
        paymentTileOption = sut.findViewById(R.id.card_info_expiration) as TextView
        paymentTileIcon = sut.findViewById(R.id.card_info_icon) as RoundImageView
        pwpSmallIcon = sut.findViewById(R.id.pwp_small_icon) as ImageView
    }

    @Test
    fun testPaymentTile() {
        sut.bind()

        //For Paying With Points Only
        paymentModel.createTripSubject.onNext(getCreateTripResponse(true))
        testPaymentTileInfo("Paying with Points", "Tap to edit", activity.resources.getDrawable(R.drawable.pwp_icon), View.GONE)

        //When user chooses to pay through card and reward points
        val latch = CountDownLatch(1)
        paymentModel.currencyToPointsApiResponse.subscribe { latch.countDown() }
        paymentModel.amountChosenToBePaidWithPointsSubject.onNext(BigDecimal(32))
        latch.await(10, TimeUnit.SECONDS)
        setUserWithStoredCard()
        testPaymentTileInfo("Paying with Points & Visa 4111", "Tap to edit", activity.resources.getDrawable(R.drawable.ic_tablet_checkout_visa), View.VISIBLE)

        //WithoutPayingWithPoints
        paymentModel.createTripSubject.onNext(getCreateTripResponse(false))
        setUserWithStoredCard()
        testPaymentTileInfo("Visa 4111", "Stored Credit Card", activity.resources.getDrawable(R.drawable.ic_tablet_checkout_visa), View.GONE)

    }

    private fun testPaymentTileInfo(paymentInfo: String, paymentOption: String, paymentIcon: Drawable, pwpSmallIconVisibility: Int) {
        assertEquals(paymentInfo, paymentTileInfo.text)
        assertEquals(paymentOption, paymentTileOption.text)
        assertEquals(paymentIcon, paymentTileIcon.drawable)
        assertEquals(pwpSmallIconVisibility, pwpSmallIcon.visibility)
    }

    private fun getCreateTripResponse(hasRedeemablePoints: Boolean): HotelCreateTripResponse {
        var createTripResponse: HotelCreateTripResponse
        if (hasRedeemablePoints)
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithRedeemablePointsCreateTripResponse()
        else
            createTripResponse = mockHotelServiceTestRule.getLoggedInUserWithNonRedeemeblePointsCreateTripResponse()

        createTripResponse.tripId = "happy"
        Db.getTripBucket().add(TripBucketItemHotelV2(createTripResponse))

        return createTripResponse

    }

    private fun setUserWithStoredCard() {
        val user = User()
        val card = StoredCreditCard()
        card.cardNumber = "4111111111111111"
        card.type = PaymentType.CARD_AMERICAN_EXPRESS
        card.description = "Visa 4111"

        user.addStoredCreditCard(card)
        Db.setUser(user)

        sut.setCreditCardRequired(true)
        sut.sectionBillingInfo.bind(BillingInfo())
        sut.selectFirstAvailableCard()
        Db.setUser(null)
    }
}