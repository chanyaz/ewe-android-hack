package com.expedia.bookings.packages.vm

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.MIDItinDetailsResponse
import com.expedia.bookings.data.HotelItinDetailsResponse
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.ui.LOBWebViewActivity
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class PackageConfirmationViewModelTest {

    private var vm: PackageConfirmationViewModel by Delegates.notNull()
    private var shadowApplication: ShadowApplication? = null
    private var activity: Activity by Delegates.notNull()
    lateinit var hotelResponse: BundleSearchResponse
    lateinit var viewModel: TestPackageConfirmationViewModel

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        shadowApplication = ShadowApplication.getInstance()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun pkgLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = "100"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValue("$userPoints Expedia Rewards Points")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun zeroPkgLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = "0"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun noShowPkgLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestObserver<String>()
        val userPoints = "100"
        vm = PackageConfirmationViewModel(activity)
        //adding test POS configuration without rewards enabled
        PointOfSaleTestConfiguration.configurePointOfSale(activity, "MockSharedData/pos_with_show_rewards_false.json", false)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageConfirmationDetails() {
        setUpConfirmationData()
        val destinationTestSubscriber = TestObserver<String>()
        val destinationTitleTestSubscriber = TestObserver<String>()
        val destinationSubTitleTestSubscriber = TestObserver<String>()
        val outboundFlightCardTestSubscriber = TestObserver<String>()
        val outboundFlightCardSubTitleTestSubscriber = TestObserver<String>()
        val inboundFlightCardTitleTestSubscriber = TestObserver<String>()
        val inboundFlightCardSubTitleTestSubscriber = TestObserver<String>()
        val itinNumberMessageOTestSubscriber = TestObserver<String>()

        viewModel.destinationObservable.subscribe(destinationTestSubscriber)
        viewModel.destinationTitleObservable.subscribe(destinationTitleTestSubscriber)
        viewModel.destinationSubTitleObservable.subscribe(destinationSubTitleTestSubscriber)
        viewModel.outboundFlightCardTitleObservable.subscribe(outboundFlightCardTestSubscriber)
        viewModel.outboundFlightCardSubTitleObservable.subscribe(outboundFlightCardSubTitleTestSubscriber)
        viewModel.inboundFlightCardTitleObservable.subscribe(inboundFlightCardTitleTestSubscriber)
        viewModel.inboundFlightCardSubTitleObservable.subscribe(inboundFlightCardSubTitleTestSubscriber)
        viewModel.itinNumberMessageObservable.subscribe(itinNumberMessageOTestSubscriber)

        val midItinDetailsResponse = MIDItinDetailsResponse()
        val responseData = MIDItinDetailsResponse.MIDResponseData()
        val hotels = mutableListOf<HotelItinDetailsResponse.Hotels>()
        val hotel = HotelItinDetailsResponse.Hotels()
        val hotelPropertyInfo = HotelItinDetailsResponse.Hotels.HotelPropertyInfo()
        val address = HotelItinDetailsResponse.Hotels.HotelPropertyInfo.Address()
        address.city = "London"
        hotelPropertyInfo.address = address
        hotelPropertyInfo.name = "London"
        hotel.hotelPropertyInfo = hotelPropertyInfo
        hotel.checkInDateTime = DateTime(2018, 2, 2, 0, 0, 0)
        hotel.checkOutDateTime = DateTime(2018, 2, 4, 0, 0, 0)
        hotels.add(hotel)

        val flights = mutableListOf<FlightItinDetailsResponse.Flight>()
        val flight = FlightItinDetailsResponse.Flight()
        val passengers = mutableListOf<FlightItinDetailsResponse.Flight.Passengers>()
        val passenger = FlightItinDetailsResponse.Flight.Passengers()
        passenger.emailAddress = "expedia.imt@gmail.com"
        passengers.add(passenger)
        passengers.add(passenger)
        passengers.add(passenger)
        passengers.add(passenger)

        val legs = mutableListOf<FlightItinDetailsResponse.Flight.Leg>()
        val outboundLeg = FlightItinDetailsResponse.Flight.Leg()
        val obSegments = mutableListOf<FlightItinDetailsResponse.Flight.Leg.Segment>()
        val obSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
        val obLocation = FlightItinDetailsResponse.Flight.Leg.Segment.Location()
        obLocation.airportCode = "LHR"
        obLocation.city = "LHR"
        val onDepartureTime = AbstractItinDetailsResponse.Time()
        onDepartureTime.raw = "2018-07-10T08:20:00Z"
        obSegment.arrivalLocation = obLocation
        obSegment.departureTime = onDepartureTime
        obSegments.add(obSegment)
        outboundLeg.segments = obSegments

        val inboundLeg = FlightItinDetailsResponse.Flight.Leg()
        val ibSegments = mutableListOf<FlightItinDetailsResponse.Flight.Leg.Segment>()
        val ibSegment = FlightItinDetailsResponse.Flight.Leg.Segment()
        val ibLocation = FlightItinDetailsResponse.Flight.Leg.Segment.Location()
        ibLocation.airportCode = "happy"
        ibLocation.city = "happy"
        val ibDepartureTime = AbstractItinDetailsResponse.Time()
        ibDepartureTime.raw = "2018-07-22T08:20:00Z"
        ibSegment.arrivalLocation = ibLocation
        ibSegment.departureTime = ibDepartureTime
        ibSegments.add(ibSegment)
        inboundLeg.segments = ibSegments

        legs.add(outboundLeg)
        legs.add(inboundLeg)
        flight.passengers = passengers
        flight.legs = legs
        flights.add(flight)

        responseData.hotels = hotels
        responseData.flights = flights
        responseData.tripNumber = 11111111
        midItinDetailsResponse.responseData = responseData
        viewModel.itinDetailsResponseObservable.onNext(midItinDetailsResponse)

        destinationTestSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)

        assertEquals(destinationTestSubscriber.values()[0], "London")
        assertEquals(destinationTitleTestSubscriber.values()[0], "London")
        assertEquals(destinationSubTitleTestSubscriber.values()[0], "Feb 2 - Feb 4, 4 guests")
        assertEquals("Flight to (LHR) LHR", outboundFlightCardTestSubscriber.values()[0])
        assertEquals(outboundFlightCardSubTitleTestSubscriber.values()[0], "Jul 10 at 08:20:00, 4 travelers")
        assertEquals(inboundFlightCardTitleTestSubscriber.values()[0], "Flight to (happy) happy")
        assertEquals(inboundFlightCardSubTitleTestSubscriber.values()[0], "Jul 22 at 08:20:00, 4 travelers")
        assertEquals(itinNumberMessageOTestSubscriber.values()[0], "#11111111 sent to expedia.imt@gmail.com")
    }

    @Test
    fun addCarToBookingHappyCase() {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val destination = Mockito.mock(SuggestionV4::class.java)
        val checkInDate = LocalDate()
        val checkOutDate = LocalDate()
        val vm = PackageConfirmationViewModel(activity)
        val params = PackageSearchParams(origin, destination, checkInDate, checkOutDate, 1, ArrayList<Int>(), false)
        Db.setPackageParams(params)
        setUpSelectedFlight()
        vm.searchForCarRentalsForTripObserver(activity).onNext(Unit)
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")

        assertEquals(LOBWebViewActivity::class.java.name, intent.component.className)
        assertTrue(intentUrl.startsWith(PointOfSale.getPointOfSale().carsTabWebViewURL))
    }

    private fun setUpConfirmationData() {
        hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)
        setupTripBucket()
        setUpSelectedFlight()
        viewModel = TestPackageConfirmationViewModel(activity)
        val mockItineraryManager = viewModel.mockItineraryManager
        Mockito.doNothing()
                .`when`(mockItineraryManager).addGuestTrip("expedia.imt@gmail.com", "11111111")
    }

    private fun setupTripBucket() {
        val createTripResponse = mockPackageServiceRule.getMIDCreateTripResponse()
        Db.getTripBucket().add(TripBucketItemPackages(createTripResponse))
        val dbHotel = Hotel()
        dbHotel.hotelId = "forOmnitureStability"
        dbHotel.city = "London"
        dbHotel.localizedName = "London"
        Db.setPackageSelectedHotel(dbHotel, null)
    }

    private fun setUpSelectedFlight() {
        val leg1 = FlightLeg()
        val leg2 = FlightLeg()

        leg1.destinationAirportCode = "SEA"
        leg1.departureDateTimeISO = "2016-07-10T08:20:00.000-07:00"
        leg1.destinationAirportLocalName = "Tacoma Intl."

        leg2.destinationAirportCode = "LHR"
        leg2.departureDateTimeISO = "2016-07-22T08:20:00.000-07:00"
        leg2.destinationAirportLocalName = "Heathrow Intl."

        Db.setPackageFlightBundle(leg1, leg2)
    }

    class TestPackageConfirmationViewModel(context: Context) : PackageConfirmationViewModel(context) {
        var mockItineraryManager: ItineraryManager = Mockito.spy(ItineraryManager.getInstance())

        override fun getItineraryManager(): ItineraryManager {
            return mockItineraryManager
        }
    }
}
