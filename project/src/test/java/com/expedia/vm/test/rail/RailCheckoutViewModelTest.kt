package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.*
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.services.RailServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.testutils.JSONResourceReader
import com.expedia.vm.rail.RailCheckoutViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class RailCheckoutViewModelTest {
    lateinit var testViewModel: RailCheckoutViewModel
    val testPrice = Money(20, "USD")
    val expectedSlideToPurchaseText = "Your card will be charged $20"

    val expectedPriceChangeTripId = "2584783d-7b84-406e-9fef-3e8e847d4d87"
    val expectedCheckoutItinNumber = "7938604594"

    var railServicesRule = ServicesRule(RailServices::class.java)
        @Rule get


    @Before
    fun setUp() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultRailComponents()

        testViewModel = RailCheckoutViewModel(activity)
        testViewModel.railServices = railServicesRule.services!!
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY))
    fun testTotalPriceText() {
        val testSubscriber = TestObserver<CharSequence>()
        testViewModel.sliderPurchaseTotalText.subscribe(testSubscriber)

        testViewModel.totalPriceObserver.onNext(testPrice)
        assertEquals(expectedSlideToPurchaseText, testSubscriber.onNextEvents[0])
    }

    @Test
    fun testTravelerObserver() {
        val mockTraveler = buildMockTraveler()
        testViewModel.travelerCompleteObserver.onNext(mockTraveler)

        // ALL fields are required for valid rail booking, and should be called at least once building checkout params.
        Mockito.verify(mockTraveler, Mockito.times(1)).firstName
        Mockito.verify(mockTraveler, Mockito.times(1)).lastName
        Mockito.verify(mockTraveler, Mockito.times(1)).phoneCountryCode
        Mockito.verify(mockTraveler, Mockito.times(1)).phoneNumber
        Mockito.verify(mockTraveler, Mockito.times(2)).email
    }

    @Test
    fun testBillingInfoObserver() {
        val mockBilling = buildMockBillingInfo()
        testViewModel.paymentCompleteObserver.onNext(mockBilling)

        // ALL fields are required for valid rail booking, and should be called at least once building checkout params.
        Mockito.verify(mockBilling, Mockito.times(1)).number
        Mockito.verify(mockBilling, Mockito.times(2)).expirationDate
        Mockito.verify(mockBilling, Mockito.times(1)).securityCode
        Mockito.verify(mockBilling, Mockito.times(1)).nameOnCard
        Mockito.verify(mockBilling.location, Mockito.times(1)).streetAddressString
        Mockito.verify(mockBilling.location, Mockito.times(1)).city
        Mockito.verify(mockBilling.location, Mockito.times(1)).stateCode
        Mockito.verify(mockBilling.location, Mockito.times(1)).postalCode
        Mockito.verify(mockBilling.location, Mockito.times(1)).countryCode
    }

    @Test
    fun testTicketDeliveryObserverForMail() {
        val mockTDO = buildMockTdoByMail()
        testViewModel.ticketDeliveryCompleteObserver.onNext(mockTDO)

        // ALL fields are required for valid rail booking, and should be called at least once building checkout params.
        Mockito.verify(mockTDO.deliveryAddress, Mockito.times(1))!!.city
        Mockito.verify(mockTDO.deliveryAddress, Mockito.times(1))!!.streetAddressString
        Mockito.verify(mockTDO.deliveryAddress, Mockito.times(1))!!.postalCode
        Mockito.verify(mockTDO.deliveryAddress, Mockito.times(1))!!.countryCode
        Mockito.verify(mockTDO, Mockito.times(1)).deliveryOptionToken
    }

    @Test
    fun testTicketDeliveryObserverForStationPickup() {
        val mockTDO = buildMockTdoPickupAtStation()
        testViewModel.ticketDeliveryCompleteObserver.onNext(mockTDO)

        // ALL fields are required for valid rail booking, and should be called at least once building checkout params.
        Mockito.verify(mockTDO, Mockito.times(1)).deliveryOptionToken
        assertNull(mockTDO.deliveryAddress)
    }

    @Test
    fun testPriceChange() {
        val priceChangeTestSub = TestObserver<Pair<Money, Money>>()
        val pricingSubjectTestSub = TestObserver<RailCreateTripResponse>()

        val oldMockResponse = RailCreateTripResponse()
        oldMockResponse.totalPrice = Money("12120", "USD")
        testViewModel.tripResponseObservable.onNext(oldMockResponse)

        testViewModel.priceChangeObservable.subscribe(priceChangeTestSub)
        testViewModel.updatePricingSubject.subscribe(pricingSubjectTestSub)

        testViewModel.checkoutParams.onNext(buildMockCheckoutParams("pricechange"))

        assertNotNull(priceChangeTestSub.onNextEvents[0])
        assertEquals(expectedPriceChangeTripId, pricingSubjectTestSub.onNextEvents[0].tripId)
        assertEquals(expectedPriceChangeTripId, testViewModel.createTripId)
    }

    @Test
    fun testInvalidInputError() {
        val errorTestSub = TestObserver<ApiError>()
        testViewModel.checkoutErrorObservable.subscribe(errorTestSub)

        testViewModel.checkoutParams.onNext(buildMockCheckoutParams("invalidinput"))

        assertNotNull(errorTestSub.onNextEvents[0])
        assertEquals(ApiError.Code.INVALID_INPUT, errorTestSub.onNextEvents[0].errorCode)
    }

    @Test
    fun testUnknownApiError() {
        val errorTestSub = TestObserver<ApiError>()
        testViewModel.checkoutErrorObservable.subscribe(errorTestSub)

        testViewModel.checkoutParams.onNext(buildMockCheckoutParams("unknownpayment"))

        assertNotNull(errorTestSub.onNextEvents[0])
        assertEquals(ApiError.Code.RAIL_UNKNOWN_CKO_ERROR, errorTestSub.onNextEvents[0].errorCode)
    }

    @Test
    fun testOtherUnknownErrors() {
        val errorTestSub = TestObserver<ApiError>()
        testViewModel.checkoutErrorObservable.subscribe(errorTestSub)

        testViewModel.checkoutParams.onNext(buildMockCheckoutParams("unknown"))

        assertNotNull(errorTestSub.onNextEvents[0])
        assertEquals(ApiError.Code.UNKNOWN_ERROR, errorTestSub.onNextEvents[0].errorCode)
    }

    @Test
    fun testRailCheckout() {
        val checkoutTestSub = TestObserver<Pair<RailCheckoutResponse, String>>()
        testViewModel.bookingSuccessSubject.subscribe(checkoutTestSub)
        testViewModel.travelerCompleteObserver.onNext(buildMockTraveler())

        testViewModel.checkoutParams.onNext(getCheckoutRequest())

        assertEquals(expectedCheckoutItinNumber, checkoutTestSub.onNextEvents[0].first.newTrip.itineraryNumber)
    }

    private fun getCheckoutRequest() : RailCheckoutParams {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail/rail_cko_request.json")
        val checkoutParams = resourceReader.constructUsingGson(RailCheckoutParams::class.java)
        return checkoutParams
    }

    private fun buildMockBillingInfo(): BillingInfo {
        val mockBilling = Mockito.mock(BillingInfo::class.java)
        Mockito.`when`(mockBilling.number).thenReturn("")
        Mockito.`when`(mockBilling.expirationDate).thenReturn(LocalDate.now().plusDays(20))
        Mockito.`when`(mockBilling.securityCode).thenReturn("")
        Mockito.`when`(mockBilling.nameOnCard).thenReturn("")
        val location = buildMockLocation()
        Mockito.`when`(mockBilling.location).thenReturn(location)
        return mockBilling
    }

    private fun buildMockLocation(): Location {
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.streetAddressString).thenReturn("")
        Mockito.`when`(location.city).thenReturn("")
        Mockito.`when`(location.stateCode).thenReturn("")
        Mockito.`when`(location.postalCode).thenReturn("")
        Mockito.`when`(location.countryCode).thenReturn("")
        return location
    }

    private fun buildMockTraveler(): Traveler {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.firstName).thenReturn("")
        Mockito.`when`(mockTraveler.lastName).thenReturn("")
        Mockito.`when`(mockTraveler.phoneCountryCode).thenReturn("")
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn("")
        Mockito.`when`(mockTraveler.email).thenReturn("")
        return mockTraveler
    }

    private fun buildMockTdoByMail(): TicketDeliveryOption {
        val mockTDO = Mockito.mock(TicketDeliveryOption::class.java)
        Mockito.`when`(mockTDO.deliveryOptionToken).thenReturn(RailCreateTripResponse.RailTicketDeliveryOptionToken.SEND_BY_EXPRESS_POST_UK)
        val location = Mockito.mock(Location::class.java)
        Mockito.`when`(location.city).thenReturn("Manchester")
        Mockito.`when`(location.countryCode).thenReturn("GBR")
        Mockito.`when`(location.postalCode).thenReturn("02452")
        Mockito.`when`(mockTDO.deliveryAddress).thenReturn(location)
        return mockTDO
    }

    private fun buildMockTdoPickupAtStation(): TicketDeliveryOption {
        val mockTDO = Mockito.mock(TicketDeliveryOption::class.java)
        Mockito.`when`(mockTDO.deliveryOptionToken).thenReturn(RailCreateTripResponse.RailTicketDeliveryOptionToken.PICK_UP_AT_TICKETING_OFFICE_NONE)
        Mockito.`when`(mockTDO.deliveryAddress).thenReturn(null)
        return mockTDO
    }

    private fun buildMockCheckoutParams(key: String): RailCheckoutParams {
        val travelers = listOf(RailCheckoutParams.Traveler(key, "Travler", "+1", "111111", "mock@mobiata.com"))
        val tdo = RailCheckoutParams.TicketDeliveryOption("PICK_UP_AT_TICKETING_OFFICE_NONE")
        val cardDetails = RailCheckoutParams.CardDetails(creditCardNumber = "444444444444444",
                expirationDateYear = "12", expirationDateMonth = "5", cvv = "123", nameOnCard = "Test Card",
                address1 = "123 Seasme St", city = "New York", postalCode = "60567",
                currencyCode = "USD", country = "USA")
        cardDetails.state = "IL"
        val paymentInfo = RailCheckoutParams.PaymentInfo(listOf(cardDetails))
        val tripDetails = RailCheckoutParams.TripDetails("Happy_Man", "12123.33", "USD", true)

        return RailCheckoutParams(travelers, tripDetails, paymentInfo, tdo)
    }
}