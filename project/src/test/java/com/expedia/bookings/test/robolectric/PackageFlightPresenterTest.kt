package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageResponseStore
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.packages.PackageFlightPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageFlightPresenterTest {
    lateinit private var presenter: PackageFlightPresenter
    lateinit private var activity: Activity
    lateinit var params: PackageSearchParams
    lateinit var flightResponse: BundleSearchResponse
    lateinit var hotelResponse: BundleSearchResponse
    lateinit var roomResponse: PackageOffersResponse
    val context = RuntimeEnvironment.application

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    var hotelObserver = TestSubscriber<BundleSearchResponse>()
    var flightObserver = TestSubscriber<BundleSearchResponse>()
    var offerObserver = TestSubscriber<PackageOffersResponse>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
    }

    @Test
    fun testOutboundFlightsToolbarText() {
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
        presenter.toolbarViewModel.regionNames.onNext(regionName)
        presenter.toolbarViewModel.country.onNext("India")
        presenter.toolbarViewModel.airport.onNext("BLR")
        presenter.toolbarViewModel.lob.onNext(presenter.getLineOfBusiness())
        assertEquals("Outbound to Bengaluru, India (BLR)", presenter.toolbar.title.toString())
    }

    @Test
    fun testInboundFlightsToolbarText() {
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
        presenter.toolbarViewModel.regionNames.onNext(regionName)
        presenter.toolbarViewModel.country.onNext("India")
        presenter.toolbarViewModel.airport.onNext("BLR")
        presenter.toolbarViewModel.lob.onNext(presenter.getLineOfBusiness())
        assertEquals("Inbound to Bengaluru, India (BLR)", presenter.toolbar.title.toString())
    }

    @Test
    fun testOutboundFlightsToolbarWithBreadcrumbsText() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav, R.string.preference_packages_breadcrumbs)
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
        presenter.toolbarViewModel.regionNames.onNext(regionName)
        presenter.toolbarViewModel.country.onNext("India")
        presenter.toolbarViewModel.airport.onNext("BLR")
        presenter.toolbarViewModel.lob.onNext(presenter.getLineOfBusiness())
        assertEquals("Step 2: Flight to Bengaluru", presenter.toolbar.title.toString())
    }

    @Test
    fun testInboundFlightsToolbarWithBreadcrumbsText() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav, R.string.preference_packages_breadcrumbs)
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
        presenter.toolbarViewModel.regionNames.onNext(regionName)
        presenter.toolbarViewModel.country.onNext("India")
        presenter.toolbarViewModel.airport.onNext("BLR")
        presenter.toolbarViewModel.lob.onNext(presenter.getLineOfBusiness())
        assertEquals("Step 3: Flight to Bengaluru", presenter.toolbar.title.toString())
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
        PackageResponseStore.packageParams = params
        searchHotels()
        hotelResponse = hotelObserver.onNextEvents.get(0)
        PackageResponseStore.packageResponse = hotelResponse
    }

    private fun setPackageResponseOutboundFlight() {
        buildPackagesSearchParams()
        searchHotels()
        hotelResponse = hotelObserver.onNextEvents.get(0)
        PackageResponseStore.packageResponse = hotelResponse

        params.packagePIID = hotelResponse.getHotels()[0].hotelId
        params.currentFlights = arrayOf("legs")
        params.ratePlanCode = "flight_outbound_happy"
        params.roomTypeCode = "flight_outbound_happy"
        searchRooms()
        roomResponse = offerObserver.onNextEvents[0]
        PackageResponseStore.setPackageSelectedHotel(hotelResponse.getHotels().get(0), roomResponse.getBundleRoomResponse()[0])

        params.packagePIID = "happy_outbound_flight"
        params.numberOfRooms = "1"
        params.searchProduct = Constants.PRODUCT_FLIGHT
        params.currentFlights = arrayOf("legs")
        params.isOutboundSearch(true)
        PackageResponseStore.packageParams = params
        searchFLights()
        flightResponse = flightObserver.onNextEvents.get(0)
        flightResponse.setCurrentOfferPrice(flightObserver.onNextEvents[0].getFlightLegs()[0].packageOfferModel.price)
        PackageResponseStore.packageResponse = flightResponse
    }

    private fun searchRooms() {
        packageServiceRule.services!!.hotelOffer(params.packagePIID!!, params.startDate.toString(), params.endDate.toString(), params.ratePlanCode!!, params.roomTypeCode, params.adults, params.childAges!![0].toInt()).subscribe(offerObserver)
        offerObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun searchHotels() {
        packageServiceRule.services!!.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(hotelObserver)
        hotelObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun searchFLights() {
        packageServiceRule.services!!.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(flightObserver)
        flightObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }

    private fun getPackageFlightPresenter(): PackageFlightPresenter {
        return LayoutInflater.from(activity).inflate(R.layout.package_flight_activity, null, false)
                as PackageFlightPresenter
    }
}