package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.packages.BundleTotalPriceTopWidget
import com.expedia.util.Optional
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class PackageFlightPresenterTest {
    private lateinit var presenter: PackageFlightPresenter
    private lateinit var activity: Activity
    lateinit var params: PackageSearchParams
    lateinit var flightResponse: BundleSearchResponse
    lateinit var hotelResponse: BundleSearchResponse
    lateinit var roomResponse: PackageOffersResponse
    val context = RuntimeEnvironment.application

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
    }

    @Test
    fun testBundleWidgetTopVisibleOutboundFlights() {
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        assertEquals(View.VISIBLE, presenter.bundlePriceWidgetTop.visibility)
    }

    @Test
    fun testBundleWidgetTopNotVisibleOutboundFlights() {
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        assertNull(presenter.findViewById<BundleTotalPriceTopWidget>(R.id.bundle_total_top_view))
    }

    @Test
    fun testBundleWidgetTopVisibleInboundFlights() {
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        presenter.resultsPresenter.outboundFlightSelectedSubject.onNext(createFakeFlightLeg())

        assertEquals(View.VISIBLE, presenter.bundlePriceWidgetTop.visibility)
    }

    @Test
    fun testBundleWidgetTopNotVisibleInboundFlights() {
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesMoveBundleOverviewForBreadcrumbs)

        presenter = getPackageFlightPresenter()
        presenter.resultsPresenter.outboundFlightSelectedSubject.onNext(createFakeFlightLeg())
        assertNull(presenter.findViewById<BundleTotalPriceTopWidget>(R.id.bundle_total_top_view))
    }

    @Test
    fun testOutboundFlightsToolbarText() {
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(true)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "Bengaluru, India (BLR - Kempegowda Intl.)"
        regionName.displayName = "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India"
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Outbound to Bengaluru, India (BLR)", presenter.toolbar.title.toString())
    }

    @Test
    fun testInboundFlightsToolbarText() {
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(false)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "Bengaluru, India (BLR - Kempegowda Intl.)"
        regionName.displayName = "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India"
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Inbound to Bengaluru, India (BLR)", presenter.toolbar.title.toString())
    }

    @Test
    fun testOutboundFlightsToolbarWithBreadcrumbsText() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(true)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "Bengaluru, India (BLR - Kempegowda Intl.)"
        regionName.displayName = "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India"
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Step 2: Flight to Bengaluru", presenter.toolbar.title.toString())
    }

    @Test
    fun testInboundFlightsToolbarWithBreadcrumbsText() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()

        presenter = getPackageFlightPresenter()
        presenter.toolbarViewModel.refreshToolBar.onNext(true)
        presenter.toolbarViewModel.isOutboundSearch.onNext(false)
        presenter.toolbarViewModel.travelers.onNext(1)
        presenter.toolbarViewModel.date.onNext(LocalDate.now())
        val regionName = SuggestionV4.RegionNames()
        regionName.shortName = "Bengaluru, India (BLR - Kempegowda Intl.)"
        regionName.displayName = "Bengaluru, India (BLR - Kempegowda Intl.)<I><B> near </B></I>Bangalore Palace, Bengaluru, India"
        presenter.toolbarViewModel.regionNames.onNext(Optional(regionName))
        presenter.toolbarViewModel.country.onNext(Optional("India"))
        presenter.toolbarViewModel.airport.onNext(Optional("BLR"))
        assertEquals("Step 3: Flight to Bengaluru", presenter.toolbar.title.toString())
    }

    @Test
    fun testPaymentLegalMessageOnResultsPage() {
        PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_with_airline_payment_fees.json")
        setPackageResponseHotels()
        setPackageResponseOutboundFlight()
        presenter = getPackageFlightPresenter()
        assertEquals("There may be an additional fee based on your payment method.",
                presenter.resultsPresenter.getAirlinePaymentFeesTextView().text)
        assertEquals(View.VISIBLE, presenter.resultsPresenter.getAirlinePaymentFeesTextView().visibility)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "London"
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = "happy"
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }

    private fun buildPackagesSearchParams() {
        params = PackageSearchParams.Builder(26, 329)
                .infantSeatingInLap(true)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .children(listOf(16, 10, 1))
                .build() as PackageSearchParams
    }

    private fun setPackageResponseHotels() {
        buildPackagesSearchParams()
        Db.setPackageParams(params)
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)
    }

    private fun setPackageResponseOutboundFlight() {
        buildPackagesSearchParams()
        hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)

        params.packagePIID = hotelResponse.getHotels()[0].hotelId
        params.currentFlights = arrayOf("legs")
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        params.latestSelectedOfferInfo.roomTypeCode = "flight_outbound_happy"
        roomResponse = mockPackageServiceRule.getPSSOffersSearchResponse("package_happy")
        Db.setPackageSelectedHotel(hotelResponse.getHotels().get(0), roomResponse.getBundleRoomResponse()[0])

        params.packagePIID = "happy_outbound_flight"
        params.numberOfRooms = "1"
        params.searchProduct = Constants.PRODUCT_FLIGHT
        params.currentFlights = arrayOf("legs")
        params.isOutboundSearch(true)
        Db.setPackageParams(params)
        flightResponse = mockPackageServiceRule.getPSSFlightOutboundSearchResponse("happy_outbound_flight")!!
        flightResponse.setCurrentOfferPrice(flightResponse.getFlightLegs()[0].packageOfferModel.price)
        Db.setPackageResponse(flightResponse)
    }

    private fun getPackageFlightPresenter(): PackageFlightPresenter {
        return LayoutInflater.from(activity).inflate(R.layout.package_flight_activity, null, false)
                as PackageFlightPresenter
    }

    private fun createFakeFlightLeg(): FlightLeg {
        val flightLeg = FlightLeg()
        val airline = Airline("United Airlines", "")

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
