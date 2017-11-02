package com.expedia.bookings.test.robolectric

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.trips.TripBucketItemLX
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.trips.TripBucketItemTransport
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.services.subscribeTestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.isAllowUnknownCardTypesEnabled
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.util.Optional
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PaymentViewModel
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates
import kotlin.test.assertFalse
import kotlin.test.assertTrue


@RunWith(RobolectricRunner::class)
class PaymentViewModelTest {
    var loyaltyServiceRule = ServicesRule(LoyaltyServices::class.java)
        @Rule get

    private var paymentModel: PaymentModel<HotelCreateTripResponse> by Delegates.notNull()
    private var viewModel: PaymentViewModel by notNullAndObservable {
        it.paymentType.subscribe(paymentTypeTestSubscriber)
        it.cardTitle.subscribeTestObserver(cardTitleTestSubscriber)
        it.cardSubtitle.subscribeTestObserver(cardSubtitleTestSubscriber)
        it.pwpSmallIcon.subscribe(pwpSmallIconTestSubscriber)
    }
    val paymentTypeTestSubscriber = TestObserver.create<Drawable>()
    val cardTitleTestSubscriber = TestObserver.create<String>()
    val cardSubtitleTestSubscriber = TestObserver.create<String>()
    val pwpSmallIconTestSubscriber = TestObserver.create<Boolean>()

    private fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Before
    fun setup() {
        viewModel = TestPaymentViewModelClass(getContext())
        Db.getTripBucket().clear()
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }
    
    @Test
    fun showCardInfoLabelHaveInvalidPaymentWarning() {
        val testSubscriber = TestObserver<Boolean>()
        val flightCreateTripResponse = FlightCreateTripResponse()
        val amexValidFormOfPayment = ValidFormOfPayment()
        amexValidFormOfPayment.name = "AmericanExpress"
        flightCreateTripResponse.validFormsOfPayment = listOf(amexValidFormOfPayment)
        Db.getTripBucket().add(TripBucketItemFlightV2(flightCreateTripResponse))

        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.paymentTypeWarningHandledByCkoView.onNext(true)

        viewModel.showCardFeeInfoLabel.subscribe(testSubscriber)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_AMERICAN_EXPRESS))
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_MASTERCARD))

        testSubscriber.assertValues(true, false)
    }

    @Test
    fun showInvalidPaymentTypeWarning() {
        val testSubscriber = TestObserver<Boolean>()
        viewModel.showInvalidPaymentWarning.subscribe(testSubscriber)

        viewModel.paymentTypeWarningHandledByCkoView.onNext(false)
        viewModel.invalidPaymentTypeWarning.onNext("invalid card")
        viewModel.invalidPaymentTypeWarning.onNext("")

        testSubscriber.assertValues(true, false)
    }

    @Test
    fun dontShowInvalidPaymentTypeWarning() {
        val testSubscriber = TestObserver<Boolean>()
        viewModel.showInvalidPaymentWarning.subscribe(testSubscriber)

        viewModel.paymentTypeWarningHandledByCkoView.onNext(true)
        viewModel.invalidPaymentTypeWarning.onNext("invalid card")
        viewModel.invalidPaymentTypeWarning.onNext("")

        testSubscriber.assertValues(false, false)
    }

    @Test
    fun invalidPaymentTypeWarningHotels() {
        val testSubscriber = TestObserver<String>()
        givenHotelTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_AMERICAN_EXPRESS))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Hotel does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningPackages() {
        val testSubscriber = TestObserver<String>()
        givenPackagesTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_AMERICAN_EXPRESS))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Trip does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningFlights() {
        val testSubscriber = TestObserver<String>()
        givenFlightsTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_AMERICAN_EXPRESS))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Airline does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningLX() {
        val testSubscriber = TestObserver<String>()
        givenLxTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.LX)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_AMERICAN_EXPRESS))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Activity does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningTransport() {
        val testSubscriber = TestObserver<String>()
        givenTransportTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.TRANSPORT)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_AMERICAN_EXPRESS))

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Activity does not accept American Express")
    }

    @Test
    fun testPaymentTileWithoutPWP() {
        //User is paying by manually entering the card Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(false), ContactDetailsCompletenessStatus.COMPLETE))

        //User chooses to pay with a Stored Card
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(true), ContactDetailsCompletenessStatus.COMPLETE))

        //User has not filled billing Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        paymentTypeTestSubscriber.assertValues(ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful), ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_checkout_default_creditcard))
        cardTitleTestSubscriber.assertValues("Visa …1111", "Visa 4111", "Enter payment details")
        cardSubtitleTestSubscriber.assertValues("Tap to edit", "Tap to edit", "")
        pwpSmallIconTestSubscriber.assertValues(false, false, false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPaymentTileWithtPWP() {
        viewModel.isRedeemable.onNext(true)

        //User is paying by manually entering the card Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(false), ContactDetailsCompletenessStatus.COMPLETE))

        //User is paying with points and card
        viewModel.splitsType.onNext(PaymentSplitsType.IS_PARTIAL_PAYABLE_WITH_CARD)

        //User has not filled billing Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        paymentTypeTestSubscriber.assertValues(ContextCompat.getDrawable(getContext(),R.drawable.ic_visa_colorful), ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_checkout_default_creditcard))
        cardTitleTestSubscriber.assertValues("Visa …1111", "Paying with Points & Visa …1111", "Enter payment details")
        cardSubtitleTestSubscriber.assertValues("Tap to edit", "Tap to edit", "")
        pwpSmallIconTestSubscriber.assertValues(false, true, false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPaymentTileWithPaymentABTest() {
        val viewModel = PaymentViewModel(getContext())

        viewModel.cardTitle.subscribeTestObserver(cardTitleTestSubscriber)
        viewModel.cardSubtitle.subscribeTestObserver(cardSubtitleTestSubscriber)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)

        viewModel.isRedeemable.onNext(false)

        //User is paying with points and card
        viewModel.splitsType.onNext(PaymentSplitsType.IS_FULL_PAYABLE_WITH_CARD)

        //User has not filled billing Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.INCOMPLETE))
        cardSubtitleTestSubscriber.assertValues("","Enter missing payment details")
        cardTitleTestSubscriber.assertValues("Enter payment details", "Enter payment details")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPaymentTileWhenPwpGetsOff() {
        viewModel.isRedeemable.onNext(true)

        //User has PWP but not chosen the payment mode
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        //User chooses to pay fully from points
        viewModel.splitsType.onNext(PaymentSplitsType.IS_FULL_PAYABLE_WITH_POINT)

        //User logs out or chooses a hotel which does not has PWP
        viewModel.isRedeemable.onNext(false)

        paymentTypeTestSubscriber.assertValues(ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful), ContextCompat.getDrawable(getContext(), R.drawable.pwp_icon),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_visa_colorful))
        cardTitleTestSubscriber.assertValues("Enter payment details", "Paying with Points", "Enter payment details")
        cardSubtitleTestSubscriber.assertValues("", "Tap to edit", "")
        pwpSmallIconTestSubscriber.assertValues(false, false, false)

    }

    @Test
    fun testNewCheckoutBehavior() {
        val testNewCheckoutSubscriber = TestObserver.create<Boolean>()
        viewModel.newCheckoutIsEnabled.subscribe(testNewCheckoutSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.LX)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.NONE)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)

        testNewCheckoutSubscriber.assertValues(false, false, true, false, false, false, true)
    }

    @Test
    fun testMenuButtonBehaviorOnCardSelection() {
        val testMenuVisibilitySubscriber = TestObserver.create<Boolean>()
        val testEnableMenuSubscriber = TestObserver.create<Boolean>()
        viewModel.menuVisibility.subscribe(testMenuVisibilitySubscriber)
        viewModel.enableMenuItem.subscribe(testEnableMenuSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        viewModel.onStoredCardChosen.onNext(Unit)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.onStoredCardChosen.onNext(Unit)

        testEnableMenuSubscriber.assertValues(true, false)
        testMenuVisibilitySubscriber.assertValues(true, false)
    }

    @Test
    fun testToggleOnAllowUnknownCardTypes() {
        toggleAllowUnknownCardTypesABTest(true)
        assertTrue(isAllowUnknownCardTypesEnabled(getContext()))
    }

    @Test
    fun testToggleOffAllowUnknownCardTypes() {
        toggleAllowUnknownCardTypesABTest(false)
        assertFalse(isAllowUnknownCardTypesEnabled(getContext()))
    }


    @Test
    fun testAllowUnknownCardTypesNoPaymentTypeWarning() {
        toggleAllowUnknownCardTypesABTest(true)
        val invalidPaymentTypeWarningTestSubscriber = TestObserver<String>()
        givenFlightsTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(invalidPaymentTypeWarningTestSubscriber)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_UNKNOWN))

        invalidPaymentTypeWarningTestSubscriber.assertValueCount(1)
        invalidPaymentTypeWarningTestSubscriber.assertValue("")
    }

    @Test
    fun testDontAllowUnknownCardTypesNoPaymentTypeWarning() {
        toggleAllowUnknownCardTypesABTest(false)
        val invalidPaymentTypeWarningTestSubscriber = TestObserver<String>()
        givenFlightsTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(invalidPaymentTypeWarningTestSubscriber)
        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.cardTypeSubject.onNext(Optional(PaymentType.CARD_UNKNOWN))

        invalidPaymentTypeWarningTestSubscriber.assertValueCount(1)
        invalidPaymentTypeWarningTestSubscriber.assertValue("Airline does not accept Unknown Card")
    }

    private fun givenTransportTrip() {
        val createTripResponse = LXCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemTransport(createTripResponse))
    }

    private fun givenLxTrip() {
        val createTripResponse = LXCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemLX(createTripResponse))
    }

    private fun givenFlightsTrip() {
        val flightCreateTripResponse = FlightCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemFlightV2(flightCreateTripResponse))
    }

    private fun givenPackagesTrip() {
        val packageCreateTripResponse = PackageCreateTripResponse()
        val tripBucketItemPackages = TripBucketItemPackages(packageCreateTripResponse)
        Db.getTripBucket().add(tripBucketItemPackages)
    }

    private fun givenHotelTrip() {
        val hotelCreateTripResponse = HotelCreateTripResponse()
        val tripBucketItemHotel = TripBucketItemHotelV2(hotelCreateTripResponse)
        Db.getTripBucket().add(tripBucketItemHotel)
    }

    private fun getBillingInfo(hasStoredCard: Boolean): BillingInfo {
        val info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111", getContext())
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

    private fun toggleAllowUnknownCardTypesABTest(toggleOn: Boolean) {
        if (toggleOn) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
        }
        else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
        }
    }

    class TestPaymentViewModelClass(context: Context): PaymentViewModel(context) {
        override fun getScheduler(): Scheduler {
            return Schedulers.trampoline()
        }
    }
}
