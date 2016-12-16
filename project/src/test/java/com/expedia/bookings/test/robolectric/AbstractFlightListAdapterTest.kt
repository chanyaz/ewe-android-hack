package com.expedia.bookings.test.robolectric

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.widget.packages.FlightAirlineWidget
import com.expedia.bookings.widget.shared.AbstractFlightListAdapter
import com.expedia.vm.AbstractFlightViewModel
import com.expedia.vm.flights.FlightViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class AbstractFlightListAdapterTest {

    val context = RuntimeEnvironment.application
    lateinit var sut: AbstractFlightListAdapter
    lateinit var flightSelectedSubject: PublishSubject<FlightLeg>
    lateinit var isRoundTripSubject: BehaviorSubject<Boolean>
    lateinit var flightLeg: FlightLeg

    @Before
    fun setup() {
        flightSelectedSubject = PublishSubject.create<FlightLeg>()
        isRoundTripSubject = BehaviorSubject.create()
    }

    fun createTestFlightListAdapter() {
        sut = TestFlightListAdapter(context, flightSelectedSubject, isRoundTripSubject)
    }

    @Test
    fun testMoreThanThreeCarriersResultsInMultipleCarriersAirlineView() {
        createTestFlightListAdapter()
        createFlightLegWithFourAirlines()

        val flightViewModel = sut.makeFlightViewModel(context, flightLeg)
        val flightViewHolder = createFlightViewHolder()
        flightViewHolder.bind(flightViewModel)

        assert(flightViewHolder.flightAirlineWidget.childCount == 1)

        val airlineView = flightViewHolder.flightAirlineWidget.getChildAt(0) as FlightAirlineWidget.AirlineView
        assertEquals(airlineView.airlineName.text, "Multiple Carriers")
    }

    @Test
    fun testFlightEarnMessageVisiblity() {
        createTestFlightListAdapter()
        val flightViewHolder = createFlightViewHolder()
        assertEquals(flightViewHolder.flightEarnMessage.text, "")
    }

    @Test
    fun testThreeCarriersResultsInThreeAirlineViews() {
        createTestFlightListAdapter()
        createFlightLegWithThreeAirlines()

        val flightViewModel = sut.makeFlightViewModel(context, flightLeg)
        val flightViewHolder = createFlightViewHolder()
        flightViewHolder.bind(flightViewModel)

        assert(flightViewHolder.flightAirlineWidget.childCount == 3)

        val airlineView1 = flightViewHolder.flightAirlineWidget.getChildAt(0) as FlightAirlineWidget.AirlineView
        val airlineView2 = flightViewHolder.flightAirlineWidget.getChildAt(1) as FlightAirlineWidget.AirlineView
        val airlineView3 = flightViewHolder.flightAirlineWidget.getChildAt(2) as FlightAirlineWidget.AirlineView

        assertEquals(airlineView1.airlineName.text, "United")
        assertEquals(airlineView2.airlineName.text, "Delta")
        assertEquals(airlineView3.airlineName.text, "Korean Air")
    }

    @Test
    fun testSeatsLeftUrgencyMessageWhenBucketedForABTest() {
        SettingUtils.save(context, R.string.preference_enable_urgency_messaging_on_flights, true)

        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppFlightUrgencyMessage)
        createTestFlightListAdapter()

        //When seatsLeft are less than 6
        createFlightLegWithUrgencyMessage(4)
        var flightViewHolder = bindFlightViewHolderAndModel()

        assertEquals(flightViewHolder.urgencyMessageContainer.visibility, View.VISIBLE)
        assertEquals(flightViewHolder.urgencyMessageTextView.text, "4 left at this price")

        //When seatsLeft are more than 6
        createFlightLegWithUrgencyMessage(8)
        flightViewHolder = bindFlightViewHolderAndModel()
        assertEquals(flightViewHolder.urgencyMessageContainer.visibility, View.GONE)
    }

    @Test
    fun testUrgencyMessageVisibilityWhenNotBucketedForABTest() {
        SettingUtils.save(context, R.string.preference_enable_urgency_messaging_on_flights, true)

        createTestFlightListAdapter()
        RoboTestHelper.controlTests(AbacusUtils.EBAndroidAppFlightUrgencyMessage)
        createFlightLegWithUrgencyMessage(4)
        val flightViewHolder = bindFlightViewHolderAndModel()

        assertEquals(flightViewHolder.urgencyMessageContainer.visibility, View.GONE)
    }

    private fun bindFlightViewHolderAndModel(): AbstractFlightListAdapter.FlightViewHolder {
        val flightViewModel = sut.makeFlightViewModel(context, flightLeg)
        val flightViewHolder = createFlightViewHolder()
        flightViewHolder.bind(flightViewModel)
        return flightViewHolder
    }

    private fun createFlightLeg() {
        flightLeg = FlightLeg()
        flightLeg.elapsedDays = 1
        flightLeg.durationHour = 19
        flightLeg.durationMinute = 10
        flightLeg.departureTimeShort = "1:10AM"
        flightLeg.arrivalTimeShort = "12:20PM"
        flightLeg.departureDateTimeISO = "2016-07-10T01:10:00.000-05:00"
        flightLeg.arrivalDateTimeISO = "2016-07-10T12:20:00.000-07:00"
        flightLeg.stopCount = 1
        flightLeg.packageOfferModel = PackageOfferModel()
        flightLeg.packageOfferModel.price = PackageOfferModel.PackagePrice()
        flightLeg.packageOfferModel.price.packageTotalPrice = Money("111", "USD")
        flightLeg.packageOfferModel.price.deltaPositive = true
        flightLeg.packageOfferModel.price.differentialPriceFormatted = "$11"
        flightLeg.packageOfferModel.price.pricePerPersonFormatted = "200.0"
        flightLeg.packageOfferModel.price.averageTotalPricePerTicket = Money("200.0", "USD")
        flightLeg.packageOfferModel.price.pricePerPerson = Money("200.0", "USD")
    }

    private fun createFlightLegWithUrgencyMessage(seatsLeft: Int) {
        createFlightLegWithThreeAirlines()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = seatsLeft
        flightLeg.packageOfferModel.urgencyMessage = urgencyMessage
    }

    private fun createFlightLegWithFourAirlines() {
        createFlightLeg()

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        val airline3 = Airline("Korean Air", null)
        val airline4 = Airline("Asiana", null)

        val segments = ArrayList<FlightLeg.FlightSegment>()
        val airlineSegment1 = createFlightSegment("U", "America", "America")
        val airlineSegment2 = createFlightSegment("D", "America", "Korea")
        val airlineSegment3 = createFlightSegment("KA", "Korea", "China")
        val airlineSegment4 = createFlightSegment("A", "China", "Malaysia")

        segments.add(airlineSegment1)
        segments.add(airlineSegment2)
        segments.add(airlineSegment3)
        segments.add(airlineSegment4)

        airlines.add(airline1)
        airlines.add(airline2)
        airlines.add(airline3)
        airlines.add(airline4)

        flightLeg.airlines = airlines
        flightLeg.flightSegments = segments
    }

    private fun createFlightLegWithThreeAirlines() {
        createFlightLeg()

        val airlines = ArrayList<Airline>()
        val airline1 = Airline("United", null)
        val airline2 = Airline("Delta", null)
        val airline3 = Airline("Korean Air", null)

        val segments = ArrayList<FlightLeg.FlightSegment>()
        val airlineSegment1 = createFlightSegment("U", "America", "America")
        val airlineSegment2 = createFlightSegment("D", "America", "Korea")
        val airlineSegment3 = createFlightSegment("KA", "Korea", "China")

        segments.add(airlineSegment1)
        segments.add(airlineSegment2)
        segments.add(airlineSegment3)

        airlines.add(airline1)
        airlines.add(airline2)
        airlines.add(airline3)

        flightLeg.airlines = airlines
        flightLeg.flightSegments = segments
    }

    private fun createFlightSegment(airlineCode: String, departureAirportCode: String, arrivalAirportCode: String) : FlightLeg.FlightSegment {
        val airlineSegment = FlightLeg.FlightSegment()
        airlineSegment.airlineCode = airlineCode
        airlineSegment.departureAirportCode = departureAirportCode
        airlineSegment.arrivalAirportCode = arrivalAirportCode
        airlineSegment.durationHours = 2
        airlineSegment.durationMinutes = 2
        airlineSegment.layoverDurationHours = 0
        airlineSegment.layoverDurationMinutes = 0
        return airlineSegment
    }

    private fun createFlightViewHolder(): AbstractFlightListAdapter.FlightViewHolder {
        return sut.onCreateViewHolder(FrameLayout(context), AbstractFlightListAdapter.ViewTypes.FLIGHT_CELL_VIEW.ordinal) as AbstractFlightListAdapter.FlightViewHolder
    }

    private class TestFlightListAdapter(context: Context, flightSelectedSubject: PublishSubject<FlightLeg>, isRoundTripSearch: BehaviorSubject<Boolean>) : AbstractFlightListAdapter(context, flightSelectedSubject, isRoundTripSearch) {
        override fun shouldAdjustPricingMessagingForAirlinePaymentMethodFee(): Boolean {
            return false
        }

        override fun showAllFlightsHeader(): Boolean {
            return false
        }

        override fun adjustPosition(): Int {
            return 1
        }

        override fun makeFlightViewModel(context: Context, flightLeg: FlightLeg): AbstractFlightViewModel {
            return FlightViewModel(context, flightLeg)
        }
    }
}