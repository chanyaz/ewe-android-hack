package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.BaseApiResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.vm.packages.PackageCheckoutViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.io.IOException
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageCheckoutViewModelTest {
    var testViewModel: PackageCheckoutViewModel by Delegates.notNull()
    var activity : Activity by Delegates.notNull()

    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultPackageComponents()
        testViewModel = PackageCheckoutViewModel(activity.application, serviceRule.services!!)
    }

    @Test
    fun testCheckoutPriceChange() {
        val testSubscriber = TestSubscriber.create<TripResponse>()
        testViewModel.checkoutPriceChangeObservable.subscribe(testSubscriber)

        testViewModel.builder.tripId("12312")
        testViewModel.builder.expectedTotalFare("133")
        testViewModel.builder.expectedFareCurrencyCode("USD")
        testViewModel.builder.bedType("123")

        val params = PackageCheckoutParams(getBillingInfo("errorcheckoutpricechange"),
                arrayListOf(getTraveler()), "", "", "", "", "123", true)
        testViewModel.checkoutParams.onNext(params)

        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)

        testSubscriber.assertValueCount(1)
        val packageCheckoutResponse = testSubscriber.onNextEvents[0] as PackageCheckoutResponse
        assertEquals("$464.64", packageCheckoutResponse.oldPackageDetails.pricing.packageTotal.formattedPrice)
        assertEquals("$787.00", packageCheckoutResponse.packageDetails.pricing.packageTotal.formattedPrice)
    }

    fun getBillingInfo(file: String): BillingInfo {
        val info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "asdasd"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111", activity)
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"
        info.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf(file)
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        info.location = location

        return info
    }

    fun getTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "malcolm"
        traveler.lastName = "nguyen"
        traveler.fullName = "malcolm nguyen"
        traveler.email = "malcolmnguyen@gmail.com"
        traveler.gender = Traveler.Gender.MALE
        traveler.phoneNumber = "9163355329"
        traveler.phoneCountryCode = "1"
        traveler.passengerCategory = PassengerCategory.ADULT
        traveler.birthDate = LocalDate.now().minusYears(18)
        traveler.primaryPassportCountry = "usa"
        traveler.assistance = Traveler.AssistanceType.BLIND_WITH_SEEING_EYE_DOG

        return traveler
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCheckoutUnknownError() {
        val testSubscriber = TestSubscriber.create<ApiError>()
        testViewModel.checkoutErrorObservable.subscribe(testSubscriber)

        testViewModel.builder.tripId("12312")
        testViewModel.builder.expectedTotalFare("133")
        testViewModel.builder.expectedFareCurrencyCode("USD")
        testViewModel.builder.bedType("123")

        val params = PackageCheckoutParams(getBillingInfo("errorcheckoutunknown"),
                arrayListOf(getTraveler()), "", "", "", "", "123", true)
        testViewModel.checkoutParams.onNext(params)

        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)

        testSubscriber.assertValueCount(1)
        val packageCheckoutResponse = testSubscriber.onNextEvents[0] as ApiError
        assertEquals(ApiError.Code.PACKAGE_CHECKOUT_UNKNOWN, packageCheckoutResponse.errorCode)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testInvalidCardNumberError() {
        val testSubscriber = TestSubscriber<ApiError>()
        testViewModel.checkoutErrorObservable.subscribe(testSubscriber)

        testViewModel.makeCheckoutResponseObserver().onNext(getCheckoutErrorResponse(ApiError.Code.INVALID_CARD_NUMBER))

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.INVALID_CARD_NUMBER, testSubscriber.onNextEvents[0].errorCode)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTripAlreadyBookedError() {
        val testSubscriber = TestSubscriber<Pair<BaseApiResponse, String>>()
        testViewModel.bookingSuccessResponse.subscribe(testSubscriber)

        testViewModel.email = "email@example.com"
        testViewModel.makeCheckoutResponseObserver().onNext(getCheckoutErrorResponse(ApiError.Code.TRIP_ALREADY_BOOKED))

        testSubscriber.assertValueCount(1)
        assertEquals(ApiError.Code.TRIP_ALREADY_BOOKED, testSubscriber.onNextEvents[0].first.errors[0].errorCode)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMakeCheckoutResponseObserverWhenOnError() {
        val testSubscriber = TestSubscriber<Boolean>()
        testViewModel.showCheckoutDialogObservable.subscribe(testSubscriber)

        testViewModel.makeCheckoutResponseObserver().onError(IOException())

        testSubscriber.assertValueCount(1)
        testSubscriber.assertValue(false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUpdateMayChargeFees() {
        val subscriberForOBFee = TestSubscriber<String>()
        val subscriberForChargeFee = TestSubscriber<String>()
        testViewModel.obFeeDetailsUrlSubject.subscribe(subscriberForOBFee)
        testViewModel.selectedFlightChargesFees.subscribe(subscriberForChargeFee)

        testViewModel.updateMayChargeFees(createFakeFlightLeg())

        assertEquals("", subscriberForOBFee.onNextEvents[1])
        assertEquals("", subscriberForChargeFee.onNextEvents[1])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUpdateMayChargeFeesWhenMayChargeObFeesIsTrue() {
        val subscriberForOBFee = TestSubscriber<String>()
        val subscriberForChargeFee = TestSubscriber<String>()
        testViewModel.obFeeDetailsUrlSubject.subscribe(subscriberForOBFee)
        testViewModel.selectedFlightChargesFees.subscribe(subscriberForChargeFee)

        var flightLeg = createFakeFlightLeg()
        flightLeg.mayChargeObFees = true
        testViewModel.updateMayChargeFees(flightLeg)

        assertEquals("", subscriberForOBFee.onNextEvents[0])
        assertEquals("Payment fees may apply", subscriberForChargeFee.onNextEvents[1])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUpdateMayChargeFeesWhenHasAirlineWithCCfeeIsTrue() {
        val subscriberForOBFee = TestSubscriber<String>()
        val subscriberForChargeFee = TestSubscriber<String>()
        testViewModel.obFeeDetailsUrlSubject.subscribe(subscriberForOBFee)
        testViewModel.selectedFlightChargesFees.subscribe(subscriberForChargeFee)

        var flightLeg = createFakeFlightLeg()
        var messageModel = FlightLeg.AirlineMessageModel()
        messageModel.hasAirlineWithCCfee = true
        flightLeg.airlineMessageModel = messageModel
        testViewModel.updateMayChargeFees(flightLeg)

        assertEquals("", subscriberForOBFee.onNextEvents[0])
        assertEquals("Payment fees may apply", subscriberForChargeFee.onNextEvents[1])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUpdateMayChargeFeesWhenHasAirlineWithCCfeeIsFalse() {
        val subscriberForOBFee = TestSubscriber<String>()
        val subscriberForChargeFee = TestSubscriber<String>()
        testViewModel.obFeeDetailsUrlSubject.subscribe(subscriberForOBFee)
        testViewModel.selectedFlightChargesFees.subscribe(subscriberForChargeFee)

        var flightLeg = createFakeFlightLeg()
        var messageModel = FlightLeg.AirlineMessageModel()
        flightLeg.airlineMessageModel = messageModel
        testViewModel.updateMayChargeFees(flightLeg)

        assertEquals("", subscriberForOBFee.onNextEvents[1])
        assertEquals("", subscriberForChargeFee.onNextEvents[1])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testUpdateMayChargeFeesWithAirlineFeeLinkNotNull() {
        val subscriberForOBFee = TestSubscriber<String>()
        val subscriberForChargeFee = TestSubscriber<String>()
        testViewModel.obFeeDetailsUrlSubject.subscribe(subscriberForOBFee)
        testViewModel.selectedFlightChargesFees.subscribe(subscriberForChargeFee)

        var flightLeg = createFakeFlightLeg()
        var messageModel = FlightLeg.AirlineMessageModel()
        messageModel.hasAirlineWithCCfee = true
        messageModel.airlineFeeLink = "link"
        flightLeg.airlineMessageModel = messageModel
        flightLeg.mayChargeObFees = true
        testViewModel.updateMayChargeFees(flightLeg)

        assertEquals("https://www.expedia.com/link", subscriberForOBFee.onNextEvents[1])
        assertEquals("Payment fees may apply", subscriberForChargeFee.onNextEvents[1])
    }

    private fun getCheckoutErrorResponse(errorCode: ApiError.Code): PackageCheckoutResponse {
        var checkoutErrorResponse: PackageCheckoutResponse = PackageCheckoutResponse()
        var error = ApiError()
        error.errorCode = errorCode
        var list = listOf<ApiError>(error)
        checkoutErrorResponse.errors = list
        return checkoutErrorResponse
    }

    private fun createFakeFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        val airline = Airline("Tom Air", "")

        flightLeg.airlines = listOf(airline)
        flightLeg.durationHour = 13
        flightLeg.durationMinute = 59
        flightLeg.stopCount = 1
        flightLeg.departureDateTimeISO = "2016-03-09T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-03-10T12:20:00.000-07:00"
        flightLeg.elapsedDays = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.urgencyMessage = PackageOfferModel.UrgencyMessage()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$646.00"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "$646.00"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("1200.90", "USD")
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket.roundedAmount = BigDecimal("1200.90")

        return flightLeg
    }
}
