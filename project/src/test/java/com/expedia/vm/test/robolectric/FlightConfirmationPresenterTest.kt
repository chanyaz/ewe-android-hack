package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.payment.Traveler
import com.expedia.bookings.presenter.flight.FlightConfirmationPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.TextView
import com.expedia.vm.flights.FlightConfirmationViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.util.ArrayList
import java.util.Locale
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.repeat

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightConfirmationPresenterTest {

    private var presenter: FlightConfirmationPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    private lateinit var inboundSupplementaryText : TextView
    private lateinit var outboundSupplementaryText: TextView

    val firstLegDeparture = DateTime.now().plusDays(5).toString()
    val firstLegArrival = DateTime.now().plusDays(6).toString()
    val secondLegDeparture = DateTime.now().plusDays(10).toString()
    val secondLegArrival = DateTime.now().plusDays(11).toString()

    val arrivalAirportCode = "OAX"
    val arrivalCity = "Oakland"
    val departureAirportCode = "SEA"
    val departureCity = "Seattle"
    val rewardPoints = "999"

    @Before fun before() {
        activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        SettingUtils.save(activity.applicationContext, R.string.preference_enable_additional_content_flight_confirmation, false)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing)
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser(), activity)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNewFlightConfirmationVisibility() {
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse()
        val tripTotalText = presenter.flightSummary?.findViewById(R.id.trip_total_text) as TextView

        assertEquals(VISIBLE, presenter.outboundFlightCard.visibility)
        assertEquals(VISIBLE, presenter.inboundFlightCard.visibility)
        assertEquals(VISIBLE, outboundSupplementaryText.visibility)
        assertEquals(VISIBLE, inboundSupplementaryText.visibility)

        assertEquals(VISIBLE, presenter.hotelCrossSell.visibility)
        assertEquals(activity.getDrawable(R.color.air_attach_crystal_background).colorFilter, presenter.hotelCrossSell.background.colorFilter)

        assertNotNull(presenter.toolbar)
        assertEquals(VISIBLE, presenter.toolbar?.visibility)
        assertEquals(true, presenter.toolbar?.navigationIcon?.isVisible)

        val navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(activity, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        assertEquals(navIcon, presenter.toolbar?.navigationIcon)

        assertEquals(GONE, presenter.expediaPoints.visibility)
        assertEquals("Your trip is booked!", presenter.tripBookedMessage.text as String)
        assertEquals(VISIBLE, presenter.flightSummary?.visibility)
        assertEquals("$rewardPoints points earned", presenter.flightSummary?.pointsEarned?.text)
        assertEquals("1 Traveler", presenter.flightSummary?.numberOfTravelers?.text)
        assertEquals("$100.95", presenter.flightSummary?.tripPrice?.text)
        assertEquals(VISIBLE, tripTotalText.visibility)
    }

    @Test
    fun testOldFlightConfirmationVisibility() {
        setupPresenter(isNewConfirmationEnabled = false)
        givenCheckoutResponse()

        assertEquals(VISIBLE, presenter.inboundFlightCard.visibility)
        assertEquals(VISIBLE, presenter.outboundFlightCard.visibility)
        assertEquals(GONE, inboundSupplementaryText.visibility)
        assertEquals(GONE, outboundSupplementaryText.visibility)

        assertNull(presenter.toolbar)

        assertEquals(VISIBLE, presenter.hotelCrossSell.visibility)
        assertEquals(activity.getDrawable(R.drawable.itin_button).colorFilter, presenter.hotelCrossSell.background.colorFilter)
        assertEquals("You're going to", presenter.tripBookedMessage.text as String)
        assertNull(presenter.flightSummary)
    }

    @Test
    fun testNewConfirmationToolbarShowsNoMenuWhenControl() {
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse()

        assertFalse(presenter.toolbar?.menuItem?.isVisible ?: false)
        assertNull(presenter.toolbar?.menuItem)
    }

    @Test
    fun testNewConfirmationToolbarShowsIconWhenBucketedVariantOne() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing, 1)
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse()

        assertTrue(presenter.toolbar?.menuItem?.icon?.isVisible ?: false)
    }

    @Test
    fun testNewConfirmationToolbarShowsTextWhenBucketedVariantTwo() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing, 2)
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse()

        assertEquals("Share", presenter.toolbar?.menuItem?.title)
        assertFalse(presenter.toolbar?.menuItem?.icon?.isVisible ?: false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNewConfirmationToolbarShareOneWay() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing, 1)
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse(isRoundTrip = false)

        val flightItinDetailsResponse = generateFlightItinDetailsResponse(false)

        var shareMessage = presenter.toolbar?.viewModel?.getShareMessage(flightItinDetailsResponse)
        var expectedShareMessage = "I'm flying to Oakland on 5/20/17!" + "\n" + "www.expedia_test_outbound.com"
        assertEquals(expectedShareMessage, shareMessage)

        Locale.setDefault(Locale.CHINA)
        shareMessage = presenter.toolbar?.viewModel?.getShareMessage(flightItinDetailsResponse)
        expectedShareMessage = "www.expedia_test_outbound.com"
        assertEquals(expectedShareMessage, shareMessage)
        Locale.setDefault(Locale.US)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNewConfirmationToolbarShareRoundTrip() {
        AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing, 1)
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse()

        val flightItinDetailsResponse = generateFlightItinDetailsResponse(true)

        var shareMessage = presenter.toolbar?.viewModel?.getShareMessage(flightItinDetailsResponse)
        var expectedShareMessage = "I'm flying roundtrip from Seattle to Oakland on 5/20/17 - 5/24/17!" + "\n" +
            "Outbound: www.expedia_test_outbound.com" + "\n" + "Inbound: www.expedia_test_inbound.com"
        assertEquals(expectedShareMessage, shareMessage)

        Locale.setDefault(Locale.CHINA)
        shareMessage = presenter.toolbar?.viewModel?.getShareMessage(flightItinDetailsResponse)
        expectedShareMessage = "www.expedia_test_outbound.com" + "\n" + "www.expedia_test_inbound.com"
        assertEquals(expectedShareMessage, shareMessage)
        Locale.setDefault(Locale.US)
    }

    @Test
    fun testNumberOfTravelersText() {
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse(numberOfTravelers = 3)

        assertEquals("3 Travelers", presenter.flightSummary?.numberOfTravelers?.text)
    }

    @Test
    fun testItinNumberContentDescription() {
        setupPresenter(isNewConfirmationEnabled = true)
        givenCheckoutResponse()
        var itinNumber = presenter.itinNumber
        assertEquals("Confirmation Number: 12345", itinNumber.contentDescription)
    }

    private fun setupPresenter(isNewConfirmationEnabled: Boolean) {
        SettingUtils.save(activity.applicationContext, R.string.preference_enable_additional_content_flight_confirmation, isNewConfirmationEnabled)
        presenter = LayoutInflater.from(activity).inflate(R.layout.flight_confirmation_stub, null) as FlightConfirmationPresenter
        presenter.viewModel = FlightConfirmationViewModel(activity)
        inboundSupplementaryText = presenter.inboundFlightCard.findViewById(R.id.confirmation_title_supplement) as TextView
        outboundSupplementaryText = presenter.outboundFlightCard.findViewById(R.id.confirmation_title_supplement) as TextView
    }

    private fun givenCheckoutResponse(isRoundTrip: Boolean = true, numberOfTravelers: Int = 1) {
        val checkoutResponse = getCheckoutResponse(DateTime.now().plusDays(5).toString(), hasAirAttach = true,  isRoundTrip = isRoundTrip, numberOfTravelers = numberOfTravelers)
        presenter.viewModel.inboundCardVisibility.onNext(true)
        presenter.viewModel.setRewardsPoints.onNext(rewardPoints)
        presenter.showConfirmationInfo(checkoutResponse, "test@mail.com")
    }

    private fun getCheckoutResponse(dateOfExpiration: String, hasAirAttach: Boolean = false, isRoundTrip: Boolean = false, numberOfTravelers: Int = 1) : FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
        response.details = FlightTripDetails()
        response.details.offer = FlightTripDetails.FlightOffer()
        response.passengerDetails = getPassengerDetailsList(numberOfTravelers)
        val flightLegs = ArrayList<FlightLeg>()
        flightLegs.add(makeFlightLeg(firstLegDeparture, firstLegArrival))

        if (isRoundTrip) {
            flightLegs.add(makeFlightLeg(secondLegDeparture, secondLegArrival, true))
        }

        response.details.legs = flightLegs
        val qualifierObject = FlightCheckoutResponse.AirAttachInfo()
        val offerTimeField = FlightCheckoutResponse.AirAttachInfo.AirAttachExpirationInfo()

        val field = response.javaClass.getDeclaredField("airAttachInfo")
        field.isAccessible = true

        val boolField = qualifierObject.javaClass.getDeclaredField("hasAirAttach")
        boolField.isAccessible = true

        val timeRemainingField = qualifierObject.javaClass.getDeclaredField("offerExpirationTimes")
        timeRemainingField.isAccessible = true

        val timeField = offerTimeField.javaClass.getDeclaredField("fullExpirationDate")
        timeField.isAccessible = true

        timeField.set(offerTimeField , dateOfExpiration)
        boolField.set(qualifierObject, hasAirAttach)
        timeRemainingField.set(qualifierObject, offerTimeField )

        val totalPrice = Money("100.95", "USD")
        val priceField = response.javaClass.getDeclaredField("totalChargesPrice")
        priceField.isAccessible = true
        priceField.set(response, totalPrice)
        field.set(response, qualifierObject)

        return response
    }

    private fun makeFlightLeg(departureTime: String, arrivalTime: String = "", isInbound: Boolean = false) : FlightLeg {
        val flight = FlightLeg()
        flight.segments = java.util.ArrayList<FlightLeg.FlightSegment>()

        val segment = if (isInbound) {
            makeFlightSegment(departureAirportCode, departureCity, arrivalAirportCode, arrivalCity)
        } else {
            makeFlightSegment(arrivalAirportCode, arrivalCity, departureAirportCode, departureCity)
        }

        segment.departureDateTimeISO = departureTime
        segment.arrivalDateTimeISO = arrivalTime

        segment.departureTimeRaw = departureTime
        segment.arrivalTimeRaw = arrivalTime
        flight.segments.add(0, segment)
        flight.departureDateTimeISO = departureTime

        return flight
    }

    private fun makeFlightSegment(arrivalAirportCode: String, arrivalCity: String, departureAirportCode: String, departureCity: String) :  FlightLeg.FlightSegment{
        val arrivalSegment = FlightLeg.FlightSegment()
        arrivalSegment.arrivalAirportAddress = FlightLeg.FlightSegment.AirportAddress()
        arrivalSegment.departureAirportAddress = FlightLeg.FlightSegment.AirportAddress()

        arrivalSegment.arrivalAirportCode = arrivalAirportCode
        arrivalSegment.arrivalAirportAddress.city = arrivalCity

        arrivalSegment.departureAirportCode = departureAirportCode
        arrivalSegment.departureAirportAddress.city = departureCity

        return arrivalSegment
    }

    private fun generateFlightItinDetailsResponse(isRoundTrip: Boolean): FlightItinDetailsResponse {
        val outboundLeg = FlightItinDetailsResponse.Flight.Leg()
        outboundLeg.sharableFlightLegURL = "www.expedia_test_outbound.com"
        val outboundSegments = ArrayList<FlightItinDetailsResponse.Flight.Leg.Segment>()
        val outboundSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
        outboundSegment.departureTime = AbstractItinDetailsResponse.Time()
        outboundSegment.departureTime.localizedShortDate = "5/20/17"
        outboundSegments.add(outboundSegment)
        outboundLeg.segments = outboundSegments

        val legs = ArrayList<FlightItinDetailsResponse.Flight.Leg>()
        legs.add(outboundLeg)

        if (isRoundTrip) {
            val inboundLeg = FlightItinDetailsResponse.Flight.Leg()
            inboundLeg.sharableFlightLegURL = "www.expedia_test_inbound.com"
            val inboundSegments = ArrayList<FlightItinDetailsResponse.Flight.Leg.Segment>()
            val inboundSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
            inboundSegment.arrivalTime = AbstractItinDetailsResponse.Time()
            inboundSegment.arrivalTime.localizedShortDate = "5/24/17"
            inboundSegments.add(inboundSegment)
            inboundLeg.segments = inboundSegments
            legs.add(inboundLeg)
        }

        val flight = FlightItinDetailsResponse.Flight()
        flight.legs = legs

        val flights = ArrayList<FlightItinDetailsResponse.Flight>()
        flights.add(flight)

        var response = FlightItinDetailsResponse()
        response.responseData = FlightItinDetailsResponse.FlightResponseData()
        response.responseData.flights = flights
        return response
    }

    private fun getPassengerDetailsList(numberOfTravelers: Int): List<Traveler> {
        val travelerList = ArrayList<Traveler>()
        repeat(numberOfTravelers) {
            val passengerDetails = Traveler("Test", "Traveler", "1", "1234567", "test@aol.com")
            travelerList.add(passengerDetails)
        }
        return travelerList
    }

}