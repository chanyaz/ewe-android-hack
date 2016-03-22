package com.expedia.bookings.test.robolectric

import android.content.Context
import android.graphics.drawable.Drawable
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PaymentViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates
import android.content.res.Resources
import com.expedia.bookings.data.payment.ProgramName


@RunWith(RobolectricRunner::class)
class PaymentViewModelTest {
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var viewModel: PaymentViewModel by notNullAndObservable {
        it.paymentType.subscribe(paymentTypeTestSubscriber)
        it.cardTitle.subscribe(cardTitleTestSubscriber)
        it.cardSubtitle.subscribe(cardSubtitleTestSubscriber)
        it.pwpSmallIcon.subscribe(pwpSmallIconTestSubscriber)
    }
    val paymentTypeTestSubscriber = TestSubscriber.create<Drawable>()
    val cardTitleTestSubscriber = TestSubscriber.create<String>()
    val cardSubtitleTestSubscriber = TestSubscriber.create<String>()
    val pwpSmallIconTestSubscriber = TestSubscriber.create<Boolean>()

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    private fun getResources(): Resources {
        return getContext().resources
    }

    @Before
    fun setup() {
        viewModel = PaymentViewModel(getContext())
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
        viewModel.programName.onNext(ProgramName.ExpediaRewards)
    }

    @Test
    fun testPaymentTileWithoutPWP() {
        //User is paying by manually entering the card Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(false), ContactDetailsCompletenessStatus.COMPLETE))

        //User chooses to pay with a Stored Card
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(true), ContactDetailsCompletenessStatus.COMPLETE))

        //User has not filled billing Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        paymentTypeTestSubscriber.assertValues(getResources().getDrawable(R.drawable.ic_tablet_checkout_visa), getResources().getDrawable(R.drawable.ic_tablet_checkout_visa),
                getResources().getDrawable(R.drawable.cars_checkout_cc_default_icon))
        cardTitleTestSubscriber.assertValues("Visa …1111", "Visa 4111", "Payment Method")
        cardSubtitleTestSubscriber.assertValues("Tap to edit", "Tap to edit", "Enter credit card")
        pwpSmallIconTestSubscriber.assertValues(false, false, false)
    }

    @Test
    fun testPaymentTileWithtPWP() {
        viewModel.isRedeemable.onNext(true)

        //User is paying by manually entering the card Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(false), ContactDetailsCompletenessStatus.COMPLETE))

        //User is paying with points and card
        viewModel.splitsType.onNext(PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD)

        //User has not filled billing Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        paymentTypeTestSubscriber.assertValues(getResources().getDrawable(R.drawable.ic_tablet_checkout_visa), getResources().getDrawable(R.drawable.ic_tablet_checkout_visa),
                getResources().getDrawable(R.drawable.cars_checkout_cc_default_icon))
        cardTitleTestSubscriber.assertValues("Visa …1111", "Paying with Points & Visa …1111", "Payment Method")
        cardSubtitleTestSubscriber.assertValues("Tap to edit", "Tap to edit", "Credit card, pay with points")
        pwpSmallIconTestSubscriber.assertValues(false, true, false)
    }

    @Test
    fun testPaymentTileWhenPwpGetsOff() {
        viewModel.isRedeemable.onNext(true)

        //User has PWP but not chosen the payment mode
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        //User chooses to pay fully from points
        viewModel.splitsType.onNext(PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT)

        //User logs out or chooses a hotel which does not has PWP
        viewModel.isRedeemable.onNext(false)

        paymentTypeTestSubscriber.assertValues(getResources().getDrawable(R.drawable.ic_tablet_checkout_visa), getResources().getDrawable(R.drawable.pwp_icon),
                getResources().getDrawable(R.drawable.ic_tablet_checkout_visa))
        cardTitleTestSubscriber.assertValues("Payment Method", "Paying with Points", "Payment Method")
        cardSubtitleTestSubscriber.assertValues("Credit card, pay with points", "Tap to edit", "Enter credit card")
        pwpSmallIconTestSubscriber.assertValues(false, false, false)
    }

    fun getBillingInfo(hasStoredCard: Boolean): BillingInfo {
        var info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111")
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"

        if (hasStoredCard) {
            val card = StoredCreditCard()
            card.cardNumber = "4111111111111111"
            card.type = PaymentType.CARD_VISA
            card.description = "Visa 4111"
            info.storedCard = card
        }
        return info
    }
}