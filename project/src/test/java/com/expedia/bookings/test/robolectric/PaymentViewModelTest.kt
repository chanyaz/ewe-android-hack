package com.expedia.bookings.test.robolectric

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.data.cars.CarCreateTripResponse
import com.expedia.bookings.data.cars.CarVendor
import com.expedia.bookings.data.cars.CreateTripCarOffer
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.lx.LXCreateTripResponse
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.payment.PaymentModel
import com.expedia.bookings.data.payment.PaymentSplitsType
import com.expedia.bookings.data.trips.TripBucketItemCar
import com.expedia.bookings.data.trips.TripBucketItemHotelV2
import com.expedia.bookings.data.trips.TripBucketItemLX
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.trips.TripBucketItemTransport
import com.expedia.bookings.services.LoyaltyServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.util.notNullAndObservable
import com.expedia.vm.PaymentViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.properties.Delegates


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

    @Before
    fun setup() {
        viewModel = PaymentViewModel(getContext())
        Db.getTripBucket().clear()
        paymentModel = PaymentModel<HotelCreateTripResponse>(loyaltyServiceRule.services!!)
    }

    @Test
    fun invalidPaymentTypeWarningCars() {
        val testSubscriber = TestSubscriber<String>()
        val expectedCarVendorName = "Avis"
        givenCarTrip(expectedCarVendorName)

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.CARS)
        viewModel.cardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("$expectedCarVendorName does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningHotels() {
        val testSubscriber = TestSubscriber<String>()
        givenHotelTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.HOTELS)
        viewModel.cardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Hotel does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningPackages() {
        val testSubscriber = TestSubscriber<String>()
        givenPackagesTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        viewModel.cardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("American Express is not accepted for this trip")
    }

    @Test
    fun invalidPaymentTypeWarningFlights() {
        val testSubscriber = TestSubscriber<String>()
        givenFlightsTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        viewModel.cardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("Airline does not accept American Express")
    }

    @Test
    fun invalidPaymentTypeWarningLX() {
        val testSubscriber = TestSubscriber<String>()
        givenLxTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.LX)
        viewModel.cardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("American Express not accepted")
    }

    @Test
    fun invalidPaymentTypeWarningTransport() {
        val testSubscriber = TestSubscriber<String>()
        givenTransportTrip()

        viewModel.invalidPaymentTypeWarning.subscribe(testSubscriber)

        viewModel.lineOfBusiness.onNext(LineOfBusiness.TRANSPORT)
        viewModel.cardTypeSubject.onNext(PaymentType.CARD_AMERICAN_EXPRESS)

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue("American Express not accepted")
    }

    @Test
    fun testPaymentTileWithoutPWP() {
        //User is paying by manually entering the card Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(false), ContactDetailsCompletenessStatus.COMPLETE))

        //User chooses to pay with a Stored Card
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(getBillingInfo(true), ContactDetailsCompletenessStatus.COMPLETE))

        //User has not filled billing Info
        viewModel.billingInfoAndStatusUpdate.onNext(Pair(null, ContactDetailsCompletenessStatus.DEFAULT))

        paymentTypeTestSubscriber.assertValues(ContextCompat.getDrawable(getContext(), R.drawable.ic_tablet_checkout_visa), ContextCompat.getDrawable(getContext(), R.drawable.ic_tablet_checkout_visa),
                ContextCompat.getDrawable(getContext(), R.drawable.cars_checkout_cc_default_icon))
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

        paymentTypeTestSubscriber.assertValues(ContextCompat.getDrawable(getContext(),R.drawable.ic_tablet_checkout_visa), ContextCompat.getDrawable(getContext(), R.drawable.ic_tablet_checkout_visa),
                ContextCompat.getDrawable(getContext(), R.drawable.cars_checkout_cc_default_icon))
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

        paymentTypeTestSubscriber.assertValues(ContextCompat.getDrawable(getContext(), R.drawable.ic_tablet_checkout_visa), ContextCompat.getDrawable(getContext(), R.drawable.pwp_icon),
                ContextCompat.getDrawable(getContext(), R.drawable.ic_tablet_checkout_visa))
        cardTitleTestSubscriber.assertValues("Payment Method", "Paying with Points", "Payment Method")
        cardSubtitleTestSubscriber.assertValues("Credit card, pay with points", "Tap to edit", "Enter credit card")
        pwpSmallIconTestSubscriber.assertValues(false, false, false)

    }

    @Test
    fun testCreditCardSentToBillingInfo() {
        val cardIOTestSubscriber = TestSubscriber.create<String>()
        viewModel.cardIO.subscribe(cardIOTestSubscriber)
        val info = getBillingInfoFromCard()
        viewModel.cardIO.onNext(info.number)
        viewModel.cardIO.onNext(info.securityCode)
        viewModel.cardIO.onNext(info.nameOnCard)
        viewModel.cardIO.onNext(info.location.postalCode)
        viewModel.cardIO.onNext(info.expirationDate.monthOfYear.toString())
        viewModel.cardIO.onNext(info.expirationDate.year.toString())
        viewModel.cardIO.onNext(info.isCardIO.toString())

        cardIOTestSubscriber.assertValues("4111111111111111", "111", "Joe Smith", "99999", 12.toString(), 2017.toString(), "true")
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

    private fun givenCarTrip(expectedCarVendorName: String) {
        val carTripResponse = CarCreateTripResponse()
        carTripResponse.carProduct = CreateTripCarOffer()
        carTripResponse.carProduct.vendor = CarVendor()
        carTripResponse.carProduct.vendor.name = expectedCarVendorName
        val tripBucketItemCar = TripBucketItemCar(carTripResponse)
        Db.getTripBucket().add(tripBucketItemCar)
    }

    private fun getBillingInfoFromCard() : BillingInfo {
        val info = BillingInfo()
        info.number = "4111111111111111"
        info.securityCode = "111"
        info.nameOnCard = "Joe Smith"
        val localDateForExp = LocalDate.now().withYear(2017).withMonthOfYear(12)
        info.expirationDate = localDateForExp
        info.isCardIO = true;
        val location = Location()
        location.postalCode = "99999"
        info.location = location
        viewModel.cardIOBillingInfo.onNext(info)
        return info
    }

    private fun getBillingInfo(hasStoredCard: Boolean): BillingInfo {
        val info = BillingInfo()
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
