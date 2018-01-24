package com.expedia.bookings.test.robolectric

import android.view.LayoutInflater
import android.view.View
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
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.ui.PackageActivity
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
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
    fun testErrorHandlingInMultiItemCreateTripParams() {
        val testSubscriber = TestObserver<ApiError.Code>()
        val packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
        packagePresenter.bundlePresenter.showErrorPresenter.subscribe(testSubscriber)
        packagePresenter.show(packagePresenter.bundlePresenter)
        val searchParams = getDummySearchParams()
        searchParams.roomTypeCode = null
        Db.setPackageParams(searchParams)
        packagePresenter.bundlePresenter.getCheckoutPresenter().getCreateTripViewModel().performMultiItemCreateTripSubject.onNext(Unit)

        assertEquals(ApiError.Code.UNKNOWN_ERROR, testSubscriber.values()[0])
        assertEquals(View.VISIBLE, packagePresenter.errorPresenter.visibility)
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
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
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
    fun testMIDCreateTripShowErrorPresenter() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
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

    private fun getDummySearchParams(): PackageSearchParams {
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
        params.hotelId = "hotelID"
        params.latestSelectedFlightPIID = "mid_create_trip"
        params.packagePIID = "packagePIID"
        params.ratePlanCode = "ratePlanCode"
        params.roomTypeCode = "roomTypeCode"
        params.inventoryType = "inventoryType"
        params.latestSelectedProductOfferPrice = PackageOfferModel.PackagePrice()
        params.latestSelectedProductOfferPrice?.packageTotalPrice = Money(BigDecimal(300.50), "USD")
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
