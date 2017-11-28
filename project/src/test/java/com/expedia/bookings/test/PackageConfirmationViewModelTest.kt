package com.expedia.bookings.test

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.ui.LOBWebViewActivity
import com.expedia.vm.packages.PackageConfirmationViewModel
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
import rx.observers.TestSubscriber
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
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "100"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValue("$userPoints Expedia+ Points")
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun zeroPkgLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = "0"
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun nullPkgLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestSubscriber<String>()
        val userPoints = null
        vm = PackageConfirmationViewModel(activity)
        vm.rewardPointsObservable.subscribe(expediaPointsSubscriber)
        vm.setRewardsPoints.onNext(userPoints)

        expediaPointsSubscriber.assertValueCount(0)
    }

    @Test
    fun noShowPkgLoyaltyPoints() {
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaPointsSubscriber = TestSubscriber<String>()
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
        val destinationTestSubscriber = TestSubscriber<String>()
        val destinationTitleTestSubscriber = TestSubscriber<String>()
        val destinationSubTitleTestSubscriber = TestSubscriber<String>()
        val outboundFlightCardTestSubscriber = TestSubscriber<String>()
        val outboundFlightCardSubTitleTestSubscriber = TestSubscriber<String>()
        val inboundFlightCardTitleTestSubscriber = TestSubscriber<String>()
        val inboundFlightCardSubTitleTestSubscriber = TestSubscriber<String>()
        val itinNumberMessageOTestSubscriber = TestSubscriber<String>()

        viewModel.destinationObservable.subscribe(destinationTestSubscriber)
        viewModel.destinationTitleObservable.subscribe(destinationTitleTestSubscriber)
        viewModel.destinationSubTitleObservable.subscribe(destinationSubTitleTestSubscriber)
        viewModel.outboundFlightCardTitleObservable.subscribe(outboundFlightCardTestSubscriber)
        viewModel.outboundFlightCardSubTitleObservable.subscribe(outboundFlightCardSubTitleTestSubscriber)
        viewModel.inboundFlightCardTitleObservable.subscribe(inboundFlightCardTitleTestSubscriber)
        viewModel.inboundFlightCardSubTitleObservable.subscribe(inboundFlightCardSubTitleTestSubscriber)
        viewModel.itinNumberMessageObservable.subscribe(itinNumberMessageOTestSubscriber)

        viewModel.showConfirmation.onNext(Pair("11111111", "expedia.imt@gmail.com"))

        destinationTestSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)

        assertEquals(destinationTestSubscriber.onNextEvents[0], "London")
        assertEquals(destinationTitleTestSubscriber.onNextEvents[0], "London")
        assertEquals(destinationSubTitleTestSubscriber.onNextEvents[0], "Feb 2 - Feb 4, 1 guest")
        assertEquals(outboundFlightCardTestSubscriber.onNextEvents[0], "Flight to (happyDest) London")
        assertEquals(outboundFlightCardSubTitleTestSubscriber.onNextEvents[0], "Jul 10 at 08:20:00, 1 traveler")
        assertEquals(inboundFlightCardTitleTestSubscriber.onNextEvents[0], "Flight to (happyOrigin) Paris")
        assertEquals(inboundFlightCardSubTitleTestSubscriber.onNextEvents[0], "Jul 22 at 08:20:00, 1 traveler")
        assertEquals(itinNumberMessageOTestSubscriber.onNextEvents[0], "#11111111 sent to expedia.imt@gmail.com")
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
        vm.searchForCarRentalsForTripObserver(activity).onNext(null)
        val intent = shadowApplication!!.nextStartedActivity
        val intentUrl = intent.getStringExtra("ARG_URL")

        assertEquals(LOBWebViewActivity::class.java.name, intent.component.className)
        assertTrue(intentUrl.startsWith(PointOfSale.getPointOfSale().carsTabWebViewURL))
    }

    private fun setUpConfirmationData() {
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)
        setupTripBucket()
        setUpSelectedFlight()
        viewModel = TestPackageConfirmationViewModel(activity)
        val mockItineraryManager = viewModel.mockItineraryManager
        Mockito.doNothing().
                `when`(mockItineraryManager).addGuestTrip("expedia.imt@gmail.com", "11111111")
    }

    private fun setupTripBucket() {
        val createTripResponse = mockPackageServiceRule.getPSSCreateTripResponse("create_trip")
        Db.getTripBucket().add(TripBucketItemPackages(createTripResponse))
        val hotel = createTripResponse!!.packageDetails.hotel
        val dbHotel = Hotel()
        dbHotel.hotelId = "forOmnitureStability"
        dbHotel.city = "London"
        dbHotel.localizedName = "London"
        Db.setPackageSelectedHotel(dbHotel, hotel.hotelRoomResponse)
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
