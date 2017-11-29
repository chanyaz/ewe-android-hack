package com.expedia.bookings.presenter.`package`

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.packages.PackageOverviewPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.packages.BundleOverviewViewModel
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import kotlin.properties.Delegates

@RunWith(RobolectricRunner :: class)
class PackageOverviewPresenterTest {
    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var overviewPresenter: PackageOverviewPresenter

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultPackageComponents()
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalTextBeforeCreateTripForJapanPOS() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        setPointOfSale(PointOfSaleId.JAPAN)
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
        assertEquals("Trip total (with taxes & fee)", overviewPresenter.totalPriceWidget.bundleTotalText.text.toString())
        setPointOfSale(initialPOSID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutViewOpenedWithMIDCheckoutEnabled() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppMIDCheckout, R.string.preference_enable_mid_checkout)
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)
        overviewPresenter.checkoutButton.performClick()

        assertEquals(View.VISIBLE, overviewPresenter.webCheckoutView.visibility)
        assertEquals(View.GONE, overviewPresenter.getCheckoutPresenter().visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testWebViewURLOpenedWithMIDCheckoutEnabled() {
        val testSubscriber = TestSubscriber.create<String>()
        setUpPackageDb()
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppMIDCheckout, R.string.preference_enable_mid_checkout)
        setupOverviewPresenter()
        overviewPresenter.webCheckoutView.viewModel.webViewURLObservable.subscribe(testSubscriber)
        createTripResponse()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.show(overviewPresenter.webCheckoutView)
        overviewPresenter.checkoutButton.performClick()

        assertEquals("https://www.expedia.com/MultiItemCheckout?tripid=5a72db42-e190-47f5-bdd9-5f316f214dc9", testSubscriber.onNextEvents[0])

        val packageTripResponse = mockPackageServiceRule.getPSSCreateTripResponse("create_trip")
        packageTripResponse!!.packageDetails.tripId = "AAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA"
        val createTripResponse = Optional(packageTripResponse as TripResponse)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().createTripResponseObservable.onNext(createTripResponse)

        assertEquals("https://www.expedia.com/MultiItemCheckout?tripid=AAAAAA-AAAA-AAAA-AAAA-AAAAAAAAAAAA", testSubscriber.onNextEvents[1])
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutViewOpenedWithMIDCheckoutDisabled() {
        setUpPackageDb()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.checkoutButton.performClick()

        assertEquals(View.VISIBLE, overviewPresenter.getCheckoutPresenter().visibility)
        assertEquals(View.GONE, overviewPresenter.webCheckoutView.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBundleTotalTextAfterCreateTrip() {
        val initialPOSID = PointOfSale.getPointOfSale().pointOfSaleId
        val testSubscriber = TestSubscriber.create<PackageCreateTripResponse>()
        val params = PackageCreateTripParams("create_trip", "1234", 1, false, emptyList())
        packageServiceRule.services!!.createTrip(params).subscribe(testSubscriber)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])
        assertEquals("Bundle total", overviewPresenter.totalPriceWidget.bundleTotalText.text)
        setPointOfSale(PointOfSaleId.JAPAN)
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().updateOverviewUiObservable.onNext(testSubscriber.onNextEvents[0])
        assertEquals("Trip total", overviewPresenter.totalPriceWidget.bundleTotalText.text)
        setPointOfSale(initialPOSID)
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(activity, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
    }

    private fun setUpPackageDb() {
        val hotel = Hotel()
        hotel.packageOfferModel = PackageOfferModel()
        var hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelRoomResponse.supplierType = "MERCHANT"
        Db.setPackageSelectedHotel(hotel, hotelRoomResponse)
        val outboundFlight = FlightLeg()
        Db.setPackageSelectedOutboundFlight(outboundFlight)
        setPackageSearchParams(1, emptyList(), false)
        val createTripResponse = mockPackageServiceRule.getPSSCreateTripResponse("create_trip")
        Db.getTripBucket().add(TripBucketItemPackages(createTripResponse))
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
        return PackageSearchParams.Builder(12, 329).infantSeatingInLap(infantsInLap)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(origin)
                .destination(destination)
                .adults(adults)
                .children(children)
                .build() as PackageSearchParams
    }

    private fun setupOverviewPresenter() {
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.test_package_overview_presenter, null) as PackageOverviewPresenter
        overviewPresenter.bundleWidget.viewModel = BundleOverviewViewModel(activity, packageServiceRule.services!!)
    }

    private fun createTripResponse() {
        val createTripResponse = mockPackageServiceRule.getPSSCreateTripResponse("create_trip")
        overviewPresenter.getCheckoutPresenter().getCreateTripViewModel().createTripResponseObservable.onNext(Optional(createTripResponse as TripResponse))
    }
}
