package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.presenter.packages.BundleWidget
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.DateUtils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.PackageCheckoutPresenter
import com.expedia.vm.packages.BundleOverviewViewModel
import com.squareup.phrase.Phrase
import okhttp3.mockwebserver.MockWebServer
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ))
class PackageOverviewTest {

    val server = MockWebServer()
    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    private var checkout: PackageCheckoutPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private var overview: PackageOverviewPresenter by Delegates.notNull()
    val testTravelerInfoText = "Jun 29 at 9:00 am, 1 Traveler"
    private var bundleWidget: BundleWidget by Delegates.notNull()

    @Before fun before() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        setUpPackageDb()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        overview = activity.findViewById(R.id.package_overview_presenter) as PackageOverviewPresenter
        setUpCheckout()
    }

    @Test
    fun testOvervviewRowsContentDescrition() {
        createTrip()
        //Initially when all rows are collapsed
        assertEquals(getExpectedHotelRowContDescription("Button to expand"), bundleWidget.bundleHotelWidget.rowContainer.contentDescription)
        assertEquals(getExpectedFlightRowContDescription("Button to expand"), bundleWidget.outboundFlightWidget.rowContainer.contentDescription)

        //When Hotel is expanded
        bundleWidget.bundleHotelWidget.expandSelectedHotel()
        assertEquals(getExpectedHotelRowContDescription("Button to collapse"), bundleWidget.bundleHotelWidget.rowContainer.contentDescription)

        //When hlight is expanded and hotels gets collapsed
        bundleWidget.outboundFlightWidget.expandFlightDetails(false)
        assertEquals(getExpectedHotelRowContDescription("Button to expand"), bundleWidget.bundleHotelWidget.rowContainer.contentDescription)
        assertEquals(getExpectedFlightRowContDescription("Button to collapse"), bundleWidget.outboundFlightWidget.rowContainer.contentDescription)

        //When hotel is expanded and flights gets collapsed
        bundleWidget.bundleHotelWidget.expandSelectedHotel()
        assertEquals(getExpectedHotelRowContDescription("Button to collapse"), bundleWidget.bundleHotelWidget.rowContainer.contentDescription)
        assertEquals(getExpectedFlightRowContDescription("Button to expand"), bundleWidget.outboundFlightWidget.rowContainer.contentDescription)
    }

    @Test
    fun testFromPackageSearchParamsUsesMultiCityForPOIDestinationType() {
        val packageSearchParams = givenPackageSearchParamsWithPiid()
        setDestinationTypeAndMultiCity(packageSearchParams)
        val createTripParams = PackageCreateTripParams.fromPackageSearchParams(packageSearchParams)

        assertEquals("Seattle", createTripParams.destinationId)

        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.checkoutButton.visibility)
    }

    @Test
    fun testFromPackageSearchParamsUsesGaiaIdDefault() {
        val packageSearchParams = givenPackageSearchParamsWithPiid()
        val createTripParams = PackageCreateTripParams.fromPackageSearchParams(packageSearchParams)

        assertEquals("12345", createTripParams.destinationId)

        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)
        assertEquals(View.VISIBLE, overview.bottomCheckoutContainer.checkoutButton.visibility)
    }

    private fun getExpectedHotelRowContDescription(expandState: String): String {
        val params = Db.getPackageParams()
        return Phrase.from(activity, R.string.select_hotel_selected_cont_desc_TEMPLATE)
                .put("hotel", Db.getPackageSelectedHotel()?.localizedName ?: "")
                .put("startdate", LocaleBasedDateFormatUtils.localDateToMMMd(params.startDate))
                .put("enddate", LocaleBasedDateFormatUtils.localDateToMMMd(params.endDate!!))
                .put("guests", StrUtils.formatGuestString(activity, params.guests))
                .put("expandstate", expandState)
                .format()
                .toString()
    }

    private fun getExpectedFlightRowContDescription(expandState: String): String {
        val params = Db.getPackageParams()
        return Phrase.from(activity, R.string.select_flight_selected_cont_desc_TEMPLATE)
                .put("flight", StrUtils.formatAirportCodeCityName(params.origin))
                .put("datetraveler", testTravelerInfoText)
                .put("expandstate", expandState)
                .format()
                .toString()
    }

    private fun createTrip() {
        val createTripParams = PackageCreateTripParams("create_trip", "", 1, false, emptyList())
        checkout.getCreateTripViewModel().tripParams.onNext(createTripParams)
    }

    private fun setUpCheckout() {
        checkout = overview.getCheckoutPresenter()
        bundleWidget = overview.bundleWidget
        checkout.getCreateTripViewModel().packageServices = packageServiceRule.services!!
        checkout.show(BaseCheckoutPresenter.CheckoutDefault(), Presenter.FLAG_CLEAR_BACKSTACK)
        bundleWidget.viewModel = BundleOverviewViewModel(activity.applicationContext, packageServiceRule.services!!)
        bundleWidget.viewModel.hotelParamsObservable.onNext(getPackageSearchParams(1, emptyList(), false))
        bundleWidget.outboundFlightWidget.viewModel.travelInfoTextObservable.onNext(testTravelerInfoText)
    }

    private fun setUpPackageDb() {
        val hotel = Hotel()
        hotel.packageOfferModel = PackageOfferModel()
        Db.setPackageSelectedHotel(hotel, HotelOffersResponse.HotelRoomResponse())
        val outboundFlight = FlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)
        setPackageSearchParams(1, emptyList(), false)
    }

    private fun setPackageSearchParams(adults: Int, children: List<Int>, infantsInLap: Boolean) {
        Db.setPackageParams(getPackageSearchParams(adults, children, infantsInLap))
    }

    private fun getPackageSearchParams(adults: Int, children: List<Int>, infantsInLap: Boolean): PackageSearchParams {
        val origin = SuggestionV4()
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "SFO"
        hierarchyInfo.airport = airport
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = "San Francisco"
        regionNames.shortName = "SFO"
        regionNames.fullName = "SFO - San Francisco"

        origin.hierarchyInfo = hierarchyInfo
        val destination = SuggestionV4()
        destination.hierarchyInfo = hierarchyInfo
        destination.regionNames = regionNames
        destination.type = "city"
        destination.gaiaId = "12345"
        origin.regionNames = regionNames
        return PackageSearchParams.Builder(12, 329).infantSeatingInLap(infantsInLap).startDate(LocalDate.now().plusDays(1)).endDate(LocalDate.now().plusDays(2)).origin(origin).destination(destination).adults(adults).children(children).build() as PackageSearchParams
    }

    private fun setDestinationTypeAndMultiCity(packageSearchParams: PackageSearchParams) {
            packageSearchParams.destination?.type = "POI"
            packageSearchParams.destination?.hierarchyInfo = SuggestionV4.HierarchyInfo()
            packageSearchParams.destination?.hierarchyInfo?.airport = SuggestionV4.Airport()
            packageSearchParams.destination?.hierarchyInfo?.airport?.multicity = "Seattle"
    }

    private fun givenPackageSearchParamsWithPiid() : PackageSearchParams {
        val searchParams = Db.getPackageParams()
        searchParams.packagePIID = "123"
        return searchParams
    }
}
