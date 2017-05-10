package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.presenter.flight.FlightConfirmationPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
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
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNull

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

        assertEquals(GONE, presenter.expediaPoints.visibility)
        assertEquals(VISIBLE, presenter.flightSummary?.visibility)
        assertEquals("$rewardPoints points earned", presenter.flightSummary?.pointsEarned?.text)
        assertEquals("1 Traveler", presenter.flightSummary?.numberOfTravelers?.text)
        assertEquals("$100", presenter.flightSummary?.tripPrice?.text)
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

        assertEquals(VISIBLE, presenter.hotelCrossSell.visibility)
        assertEquals(activity.getDrawable(R.drawable.itin_button).colorFilter, presenter.hotelCrossSell.background.colorFilter)
        assertNull(presenter.flightSummary)
    }


    private fun setupPresenter(isNewConfirmationEnabled: Boolean) {
        SettingUtils.save(activity.applicationContext, R.string.preference_enable_additional_content_flight_confirmation, isNewConfirmationEnabled)
        presenter = LayoutInflater.from(activity).inflate(R.layout.flight_confirmation_stub, null) as FlightConfirmationPresenter
        presenter.viewModel = FlightConfirmationViewModel(activity)
        inboundSupplementaryText = presenter.inboundFlightCard.findViewById(R.id.confirmation_title_supplement) as TextView
        outboundSupplementaryText = presenter.outboundFlightCard.findViewById(R.id.confirmation_title_supplement) as TextView
    }

    private fun givenCheckoutResponse() {
        val checkoutResponse = getCheckoutResponse(DateTime.now().plusDays(5).toString(), hasAirAttach = true,  isRoundTrip = true)
        presenter.viewModel.inboundCardVisibility.onNext(true)
        presenter.viewModel.setRewardsPoints.onNext(rewardPoints)
        presenter.showConfirmationInfo(checkoutResponse, "test@mail.com")
    }

    fun getCheckoutResponse(dateOfExpiration: String, hasAirAttach: Boolean = false, isRoundTrip: Boolean = false, numberOfTickets: String = "1") : FlightCheckoutResponse {
        val response = FlightCheckoutResponse()
        response.newTrip = TripDetails("12345", "", "")
        response.details = FlightTripDetails()
        response.details.offer = FlightTripDetails.FlightOffer()
        response.details.offer.numberOfTickets = numberOfTickets
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

        val totalPrice = Money("100", "USD")
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
}