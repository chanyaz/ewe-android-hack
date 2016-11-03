package com.expedia.vm.test.rail

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.rail.responses.RailCreateTripResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.data.TicketDeliveryOption
import com.expedia.bookings.data.rail.requests.RailCheckoutParams
import com.expedia.bookings.data.rail.responses.RailCheckoutResponse
import com.expedia.bookings.services.RailServices
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
import rx.observers.TestSubscriber
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
    fun testTotalPriceText() {
        val testSubscriber = TestSubscriber<CharSequence>()
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
        val priceChangeTestSub = TestSubscriber<Unit>()
        val pricingSubjectTestSub = TestSubscriber<RailCreateTripResponse>()

        testViewModel.priceChangeObservable.subscribe(priceChangeTestSub)
        testViewModel.updatePricingSubject.subscribe(pricingSubjectTestSub)

        testViewModel.checkoutParams.onNext(getPriceChangeRequest())

        assertNotNull(priceChangeTestSub.onNextEvents[0])
        assertEquals(expectedPriceChangeTripId, pricingSubjectTestSub.onNextEvents[0].tripId)
        assertEquals(expectedPriceChangeTripId, testViewModel.createTripId)
    }

    @Test
    fun testRailCheckout() {
        val checkoutTestSub = TestSubscriber<Pair<RailCheckoutResponse, String>>()
        testViewModel.bookingSuccessSubject.subscribe(checkoutTestSub)
        testViewModel.travelerCompleteObserver.onNext(buildMockTraveler())

        testViewModel.checkoutParams.onNext(getCheckoutRequest())

        assertEquals(expectedCheckoutItinNumber, checkoutTestSub.onNextEvents[0].first.newTrip.itineraryNumber)
    }

    private fun getPriceChangeRequest() : RailCheckoutParams {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail_price_change_cko_request.json")
        val checkoutParams = resourceReader.constructUsingGson(RailCheckoutParams::class.java)
        return checkoutParams
    }

    private fun getCheckoutRequest() : RailCheckoutParams {
        val resourceReader = JSONResourceReader("src/test/resources/raw/rail_cko_request.json")
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
}