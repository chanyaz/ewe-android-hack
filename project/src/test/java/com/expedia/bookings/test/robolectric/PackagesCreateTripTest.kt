package com.expedia.bookings.test.robolectric

import com.expedia.bookings.OmnitureTestUtils
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper.getContext
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.ui.PackageActivity
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowDialog
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class PackagesCreateTripTest {

    var activity: PackageActivity by Delegates.notNull()

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(PackageActivity::class.java).create().get()
    }

    @Test
    fun testMultiItemCreateTripParamsFromSearchParams() {
        val searchParams = getDummySearchParams()
        val fromPackageSearchParams = MultiItemCreateTripParams.fromPackageSearchParams(searchParams)

        assertEquals("mid_create_trip", fromPackageSearchParams.flightPIID)
        assertEquals(1, fromPackageSearchParams.adults)
        assertEquals(LocalDate.now(), fromPackageSearchParams.startDate)
        assertEquals(LocalDate.now().plusDays(2), fromPackageSearchParams.endDate)
        assertEquals("hotelID", fromPackageSearchParams.hotelID)
        assertEquals("roomTypeCode", fromPackageSearchParams.roomTypeCode)
        assertEquals("ratePlanCode", fromPackageSearchParams.ratePlanCode)
        assertEquals("inventoryType", fromPackageSearchParams.inventoryType)
        assertEquals(BigDecimal(300.50), fromPackageSearchParams.totalPrice.packageTotalPrice.amount)
        assertEquals("1,14", fromPackageSearchParams.childAges)
        assertEquals(true, fromPackageSearchParams.infantsInSeats)
    }

    @Test
    fun testExceptionThrownInMultiItemCreateTripParams() {
        val searchParams = getDummySearchParams()
        searchParams.latestSelectedOfferInfo.roomTypeCode = null
        try {
            MultiItemCreateTripParams.fromPackageSearchParams(searchParams)
        } catch (e: Exception) {
            assertEquals(IllegalArgumentException().javaClass, e.javaClass)
        }
    }

    @Test
    fun testMultiItemCreateTripFiredWhenMIDAPIOFF() {
        val testSubscriber = TestObserver<Unit>()
        activity.getCreateTripViewModel().performCreateTrip.subscribe(testSubscriber)

        val params = getDummySearchParams()
        Db.setPackageParams(params)
        val dbHotel = Hotel()
        dbHotel.hotelId = "forOmnitureStability"
        dbHotel.city = "London"
        dbHotel.localizedName = "London"
        dbHotel.packageOfferModel = PackageOfferModel()
        Db.setPackageSelectedHotel(dbHotel, HotelOffersResponse.HotelRoomResponse())
        activity.packageCreateTrip()

        testSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testMultiItemCreateTripFiredWhenMIDAPION() {
        val createTripSubscriber = TestObserver<MultiItemApiCreateTripResponse>()
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)
        activity.packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().multiItemResponseSubject.subscribe(createTripSubscriber)
        setUpPackageDb()
        val hotelResponse = mockPackageServiceRule.getPSSHotelSearchResponse()
        Db.setPackageResponse(hotelResponse)
        val params = getDummySearchParams()
        Db.setPackageParams(params)

        activity.packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().packageServices = packageServiceRule.services!!
        val baseMidResponse = PackageTestUtil.getMockMIDResponse(offers = emptyList(),
                hotels = mapOf("1" to PackageTestUtil.dummyMidHotelRoomOffer()))
        baseMidResponse.setCurrentOfferPrice(setPackagePrice())
        Db.setPackageResponse(baseMidResponse)
        PackageTestUtil.setDbPackageSelectedHotel()
        activity.packageCreateTrip()
        createTripSubscriber.awaitValueCount(1, 10, TimeUnit.SECONDS)

        createTripSubscriber.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMidCreateTripErrorTracking() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        PackagesTracking().trackMidCreateTripError("MID_CREATETRIP_INVALID_REQUEST")
        val linkName = "App.Package.Checkout.Error"

        OmnitureTestUtils.assertStateTracked(linkName,
                Matchers.allOf(OmnitureMatchers.withProps(mapOf(36 to "MID_CREATETRIP_INVALID_REQUEST")),
                        OmnitureMatchers.withEvars(mapOf(18 to linkName))),
                mockAnalyticsProvider)
    }

    @Test
    fun testMultiItemCreateTripErrorHandled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)
        val showErrorAlertObserver = TestObserver<Unit>()
        val createTripViewModel = activity.packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel()
        createTripViewModel.showMIDCreateTripErrorAlertObservable.subscribe(showErrorAlertObserver)
        createTripViewModel.packageServices = packageServiceRule.services!!

        val errorParams = MultiItemCreateTripParams.fromPackageSearchParams(getDummySearchParams("error"))
        showErrorAlertObserver.assertValueCount(0)

        createTripViewModel.packageServices.multiItemCreateTrip(errorParams).subscribe(createTripViewModel.makeMultiItemCreateTripResponseObserver())

        showErrorAlertObserver.assertValueCount(1)
    }

    @Test
    fun testShowMIDCreateTripErrorDialogDisplayed() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)

        activity.packagePresenter.bundlePresenter.showCheckout()
        val createTripViewModel = activity.packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel()
        val testCreateTripObserver = TestObserver<Unit>()

        createTripViewModel.showMIDCreateTripErrorAlertObservable.onNext(Unit)

        val errorDialog = ShadowDialog.getLatestDialog()
        val changeFlight = errorDialog.findViewById<TextView>(R.id.change_flight)
        assertEquals("Change flights", changeFlight.text)

        val retry = errorDialog.findViewById<TextView>(R.id.retry)
        assertEquals("Retry", retry.text)

        retry.performClick()

        createTripViewModel.performMultiItemCreateTripSubject.subscribe(testCreateTripObserver)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDCreateTripShowErrorPresenter() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)
        val showErrorPresenterTestSubscriber = TestObserver<ApiError>()
        val createTripViewModel = activity.packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel()
        createTripViewModel.createTripErrorObservable.subscribe(showErrorPresenterTestSubscriber)
        createTripViewModel.packageServices = packageServiceRule.services!!

        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money()
        val errorParams = MultiItemCreateTripParams("", "", "", "", "", packagePrice, LocalDate(), LocalDate(), 0, null, null)
        showErrorPresenterTestSubscriber.assertValueCount(0)

        createTripViewModel.packageServices.multiItemCreateTrip(errorParams).subscribe(createTripViewModel.makeMultiItemCreateTripResponseObserver())

        showErrorPresenterTestSubscriber.assertValueCount(1)
    }

    private fun getDummySearchParams(response: String = "mid_create_trip"): PackageSearchParams {
        val originDestSuggestions = getOriginDestSuggestions()
        val date = LocalDate.now()
        val params = PackageSearchParams.Builder(0, 0)
                .destination(originDestSuggestions.first)
                .origin(originDestSuggestions.second)
                .adults(1)
                .children(listOf(1, 14))
                .startDate(date)
                .endDate(date.plusDays(2))
                .build() as PackageSearchParams
        params.latestSelectedOfferInfo.hotelId = "hotelID"
        params.latestSelectedOfferInfo.flightPIID = response
        params.packagePIID = "packagePIID"
        params.latestSelectedOfferInfo.ratePlanCode = "ratePlanCode"
        params.latestSelectedOfferInfo.roomTypeCode = "roomTypeCode"
        params.latestSelectedOfferInfo.inventoryType = "inventoryType"
        params.latestSelectedOfferInfo.productOfferPrice = PackageOfferModel.PackagePrice()
        params.latestSelectedOfferInfo.productOfferPrice?.packageTotalPrice = Money(BigDecimal(300.50), "USD")
        params.packagePIID = "923012"
        params.searchProduct = Constants.PRODUCT_FLIGHT
        params.currentFlights = arrayOf("legs")
        return params
    }

    fun getOriginDestSuggestions(): Pair<SuggestionV4, SuggestionV4> {
        val suggestionDest = SuggestionV4()
        val suggestionOrigin = SuggestionV4()
        suggestionDest.gaiaId = "12345"
        suggestionDest.regionNames = SuggestionV4.RegionNames()
        suggestionDest.regionNames.displayName = "London"
        suggestionDest.regionNames.fullName = "London, England"
        suggestionDest.regionNames.shortName = "London"
        suggestionDest.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestionDest.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestionDest.hierarchyInfo!!.airport!!.airportCode = "happyDest"
        suggestionDest.hierarchyInfo!!.airport!!.multicity = "happyDest"
        suggestionDest.coordinates = SuggestionV4.LatLng()

        suggestionOrigin.coordinates = SuggestionV4.LatLng()
        suggestionOrigin.gaiaId = "67891"
        suggestionOrigin.regionNames = SuggestionV4.RegionNames()
        suggestionOrigin.regionNames.displayName = "Paris"
        suggestionOrigin.regionNames.fullName = "Paris, France"
        suggestionOrigin.regionNames.shortName = "Paris"
        suggestionOrigin.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestionOrigin.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestionOrigin.hierarchyInfo!!.airport!!.airportCode = "happyOrigin"
        suggestionOrigin.hierarchyInfo!!.airport!!.multicity = "happyOrigin"

        return Pair<SuggestionV4, SuggestionV4>(suggestionDest, suggestionOrigin)
    }

    private fun setUpPackageDb() {
        val hotel = Hotel()
        hotel.packageOfferModel = PackageOfferModel()
        hotel.city = "Detroit"
        hotel.countryCode = "USA"
        hotel.stateProvinceCode = "MI"
        hotel.largeThumbnailUrl = "https://"
        var hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelRoomResponse.supplierType = "MERCHANT"
        Db.setPackageSelectedHotel(hotel, hotelRoomResponse)
    }

    private fun setPackagePrice(): PackageOfferModel.PackagePrice {
        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money("200", "USD")
        return packagePrice
    }
}
