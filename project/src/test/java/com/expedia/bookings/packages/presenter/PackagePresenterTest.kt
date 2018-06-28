package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.activity.PackageActivity
import com.expedia.bookings.packages.activity.PackageFlightActivity
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.robolectric.FlightTestUtil
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackagePresenterTest {

    val context = RuntimeEnvironment.application
    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    private lateinit var packagePresenter: PackagePresenter
    private lateinit var activity: Activity

    @Before
    fun setup() {
        setup(false)
    }

    private fun setup(isCrossSellPackageOnFSREnabled: Boolean) {
        val intent = Intent()
        intent.putExtra(Constants.INTENT_PERFORM_HOTEL_SEARCH, isCrossSellPackageOnFSREnabled)

        activity = Robolectric.buildActivity(PackageActivity::class.java, intent).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()

        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null, false) as PackagePresenter
    }

    @Test
    fun testSearchParamsObservable() {
        val searchParams = PackageTestUtil.getMIDPackageSearchParams()

        val errorPresenterParamsSubjectObserver = TestObserver<PackageSearchParams>()
        val bundlePresenterHotelParamsObserver = TestObserver<PackageSearchParams>()

        packagePresenter.errorPresenter.viewmodel.paramsSubject.subscribe(errorPresenterParamsSubjectObserver)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.hotelParamsObservable.subscribe(bundlePresenterHotelParamsObserver)

        packagePresenter.searchPresenter.searchViewModel.searchParamsObservable.onNext(searchParams)

        assertEquals(searchParams, errorPresenterParamsSubjectObserver.values()[0])
        assertEquals(searchParams, bundlePresenterHotelParamsObserver.values()[0])
        assertNull(Db.getPackageSelectedHotel())
        assertNull(Db.getPackageFlightBundle())
    }

    @Test
    fun testShowBundleTotalObservableWithShowSavings() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        val packagePrice = Money("200", "USD")
        val packageSavings = Money("30", "USD")

        val currentOfferPrice = PackageOfferModel.PackagePrice()
        currentOfferPrice.packageTotalPrice = packagePrice
        currentOfferPrice.tripSavings = packageSavings
        currentOfferPrice.showTripSavings = true

        hotelResponse.setCurrentOfferPrice(currentOfferPrice)
        Db.setPackageResponse(hotelResponse)

        val totalPriceObservable = TestObserver<Money>()
        val packageSavingsObservable = TestObserver<Money>()
        val showSavingsObservable = TestObserver<Boolean>()
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.total.subscribe(totalPriceObservable)
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.savings.subscribe(packageSavingsObservable)
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.shouldShowSavings.subscribe(showSavingsObservable)

        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)

        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.totalPriceWidget.visibility)
        assertTrue(showSavingsObservable.values()[0])
        assertEquals(packagePrice, totalPriceObservable.values()[0])
        assertEquals(packageSavings, packageSavingsObservable.values()[0])
    }

    @Test
    fun testShowBundleTotalObservableWithNullPackagePrice() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        val currentOfferPrice = PackageOfferModel.PackagePrice()
        currentOfferPrice.packageTotalPrice = null

        hotelResponse.setCurrentOfferPrice(currentOfferPrice)
        Db.setPackageResponse(hotelResponse)

        val totalPriceObservable = TestObserver<Money>()
        val packageSavingsObservable = TestObserver<Money>()
        val showSavingsObservable = TestObserver<Boolean>()
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.total.subscribe(totalPriceObservable)
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.savings.subscribe(packageSavingsObservable)
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.shouldShowSavings.subscribe(showSavingsObservable)

        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)

        assertEquals(View.GONE, packagePresenter.bundlePresenter.totalPriceWidget.visibility)
        showSavingsObservable.assertNoValues()
        totalPriceObservable.assertNoValues()
        packageSavingsObservable.assertNoValues()
    }

    @Test
    fun testShowBundleTotalObservableWithoutShowSavings() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()

        val packagePrice = Money("200", "USD")
        val packageSavings = Money("3", "USD")

        val currentOfferPrice = PackageOfferModel.PackagePrice()
        currentOfferPrice.packageTotalPrice = packagePrice
        currentOfferPrice.tripSavings = packageSavings
        currentOfferPrice.showTripSavings = false

        hotelResponse.setCurrentOfferPrice(currentOfferPrice)
        Db.setPackageResponse(hotelResponse)

        val showSavingsObservable = TestObserver<Boolean>()
        packagePresenter.bundlePresenter.totalPriceWidget.viewModel.shouldShowSavings.subscribe(showSavingsObservable)

        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.onNext(true)

        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.totalPriceWidget.visibility)
        assertFalse(showSavingsObservable.values()[0])
    }

    @Test
    fun testDefaultErrorObservable() {
        val searchParams = PackageTestUtil.getMIDPackageSearchParams()
        Db.setPackageParams(searchParams)

        val outboundShowLoadingStateObserver = TestObserver<Boolean>()
        val inboundShowLoadingStateObserver = TestObserver<Boolean>()

        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.showLoadingStateObservable.subscribe(outboundShowLoadingStateObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.showLoadingStateObservable.subscribe(inboundShowLoadingStateObserver)

        packagePresenter.searchPresenter.searchViewModel.searchParamsObservable.onNext(searchParams)
        packagePresenter.errorPresenter.viewmodel.defaultErrorObservable.onNext(Unit)

        assertEquals(false, outboundShowLoadingStateObserver.values()[0])
        assertEquals(false, inboundShowLoadingStateObserver.values()[0])
    }

    @Test
    fun testHandleHotelOffersAPIError() {
        assertHandleHotelOffersAndInfositeAPIError(errorKey = null,
                errorString = null,
                isErrorFromInfositeCall = false,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.UNKNOWN_ERROR,
                expectedErrorCode = "UNKNOWN_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_ROOM")

        assertHandleHotelOffersAndInfositeAPIError(errorKey = "",
                errorString = "",
                isErrorFromInfositeCall = false,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.UNKNOWN_ERROR,
                expectedErrorCode = "",
                expectedApiCall = "PACKAGE_HOTEL_ROOM")

        assertHandleHotelOffersAndInfositeAPIError(errorKey = "UNKNOWN_ERROR",
                errorString = "UNKNOWN_ERROR",
                isErrorFromInfositeCall = false,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.UNKNOWN_ERROR,
                expectedErrorCode = "UNKNOWN_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_ROOM")

        assertHandleHotelOffersAndInfositeAPIError(errorKey = "PACKAGE_SEARCH_ERROR",
                errorString = "PACKAGE_SEARCH_ERROR",
                isErrorFromInfositeCall = false,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR,
                expectedErrorCode = "PACKAGE_SEARCH_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_ROOM")
    }

    @Test
    fun testHandleChangeHotelOffersAPIError() {
        assertHandleHotelOffersAndInfositeAPIError(errorKey = "PACKAGE_SEARCH_ERROR",
                errorString = "PACKAGE_SEARCH_ERROR",
                isErrorFromInfositeCall = false,
                isChangeSearch = true,
                expectedApiErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR,
                expectedErrorCode = "PACKAGE_SEARCH_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_ROOM_CHANGE")
    }

    @Test
    fun testHandleHotelInfositeAPIError() {
        assertHandleHotelOffersAndInfositeAPIError(errorKey = null,
                errorString = null,
                isErrorFromInfositeCall = true,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.UNKNOWN_ERROR,
                expectedErrorCode = "UNKNOWN_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_INFOSITE")

        assertHandleHotelOffersAndInfositeAPIError(errorKey = "",
                errorString = "",
                isErrorFromInfositeCall = true,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.UNKNOWN_ERROR,
                expectedErrorCode = "",
                expectedApiCall = "PACKAGE_HOTEL_INFOSITE")

        assertHandleHotelOffersAndInfositeAPIError(errorKey = "UNKNOWN_ERROR",
                errorString = "UNKNOWN_ERROR",
                isErrorFromInfositeCall = true,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.UNKNOWN_ERROR,
                expectedErrorCode = "UNKNOWN_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_INFOSITE")

        assertHandleHotelOffersAndInfositeAPIError(errorKey = "PACKAGE_SEARCH_ERROR",
                errorString = "PACKAGE_SEARCH_ERROR",
                isErrorFromInfositeCall = true,
                isChangeSearch = false,
                expectedApiErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR,
                expectedErrorCode = "PACKAGE_SEARCH_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_INFOSITE")
    }

    @Test
    fun testHandleChangeHotelInfositeAPIError() {
        assertHandleHotelOffersAndInfositeAPIError(errorKey = "PACKAGE_SEARCH_ERROR",
                errorString = "PACKAGE_SEARCH_ERROR",
                isErrorFromInfositeCall = true,
                isChangeSearch = true,
                expectedApiErrorCode = ApiError.Code.PACKAGE_SEARCH_ERROR,
                expectedErrorCode = "PACKAGE_SEARCH_ERROR",
                expectedApiCall = "PACKAGE_HOTEL_INFOSITE_CHANGE")
    }

    @Test
    fun testHandleHotelFilterAPIError() {
        assertHandleHotelFilterAPIError(filterSearchErrorString = null,
                expectedApiErrorCode = PackageApiError.Code.pkg_error_code_not_mapped,
                expectedErrorCode = Constants.UNKNOWN_ERROR_CODE,
                expectedApiCall = "PACKAGE_FILTERS_SEARCH")

        assertHandleHotelFilterAPIError(filterSearchErrorString = "",
                expectedApiErrorCode = PackageApiError.Code.pkg_error_code_not_mapped,
                expectedErrorCode = "",
                expectedApiCall = "PACKAGE_FILTERS_SEARCH")

        assertHandleHotelFilterAPIError(filterSearchErrorString = "mid_no_offers_post_filtering",
                expectedApiErrorCode = PackageApiError.Code.mid_no_offers_post_filtering,
                expectedErrorCode = "mid_no_offers_post_filtering",
                expectedApiCall = "PACKAGE_FILTERS_SEARCH")

        assertHandleHotelFilterAPIError(filterSearchErrorString = "pkg_error_code_not_mapped",
                expectedApiErrorCode = PackageApiError.Code.pkg_error_code_not_mapped,
                expectedErrorCode = "pkg_error_code_not_mapped",
                expectedApiCall = "PACKAGE_FILTERS_SEARCH")
    }

    @Test
    fun testHotelSelectedSuccessfully() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hotelRoomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        hotelResponse.setCurrentOfferPrice(hotelResponse.getHotels().first().packageOfferModel.price)
        Db.setPackageResponse(hotelResponse)

        Db.setPackageSelectedHotel(hotelResponse.getHotels().first(), hotelRoomResponse.getBundleRoomResponse().first())

        val packageParams = Db.sharedInstance.packageParams
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.updateHotelParams(packageParams)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.updateHotelParams(packageParams)

        val performMIDCreateTripObserver = TestObserver<Unit>()
        val selectedHotelObserver = TestObserver<Unit>()
        val showBundleTotalObserver = TestObserver<Boolean>()
        val flightParamsObserver = TestObserver<PackageSearchParams>()
        packagePresenter.bundlePresenter.bundleWidget.viewModel.flightParamsObservable.subscribe(flightParamsObserver)
        packagePresenter.bundlePresenter.viewModel.performMIDCreateTripSubject.subscribe(performMIDCreateTripObserver)
        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.subscribe(selectedHotelObserver)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe(showBundleTotalObserver)

        packagePresenter.hotelSelectedSuccessfully()

        flightParamsObserver.assertValueCount(1)
        flightParamsObserver.assertValue(packageParams)
        performMIDCreateTripObserver.assertValueCount(0)
        selectedHotelObserver.assertValueCount(1)
        showBundleTotalObserver.assertValue(true)
        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.visibility)

        assertEquals(packagePresenter.bundlePresenter, packagePresenter.backStack.pop())
        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.putExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM, true)
        intent.putExtra(Constants.REQUEST, Constants.HOTEL_REQUEST_CODE)
        assertTrue(intent.filterEquals(packagePresenter.backStack.peek() as Intent))
    }

    @Test
    fun testChangeHotelSelectedSuccessfully() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hotelRoomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        hotelResponse.setCurrentOfferPrice(hotelResponse.getHotels().first().packageOfferModel.price)
        Db.setPackageResponse(hotelResponse)

        Db.setPackageSelectedHotel(hotelResponse.getHotels().first(), hotelRoomResponse.getBundleRoomResponse().first())

        val packageParams = Db.sharedInstance.packageParams
        packageParams.pageType = Constants.PACKAGE_CHANGE_HOTEL
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.updateHotelParams(packageParams)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.updateHotelParams(packageParams)

        val performMIDCreateTripObserver = TestObserver<Unit>()
        val selectedHotelObserver = TestObserver<Unit>()
        val showBundleTotalObserver = TestObserver<Boolean>()
        packagePresenter.bundlePresenter.viewModel.performMIDCreateTripSubject.subscribe(performMIDCreateTripObserver)
        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.subscribe(selectedHotelObserver)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe(showBundleTotalObserver)

        packagePresenter.hotelSelectedSuccessfully()

        performMIDCreateTripObserver.assertValueCount(1)
        selectedHotelObserver.assertValueCount(1)
        showBundleTotalObserver.assertValue(false)
        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.visibility)
    }

    @Test
    fun testFlightOutboundSelectedSuccessfully() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hotelRoomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        hotelResponse.setCurrentOfferPrice(hotelResponse.getHotels().first().packageOfferModel.price)

        Db.setPackageSelectedHotel(hotelResponse.getHotels().first(), hotelRoomResponse.getBundleRoomResponse().first())
        val midFlightsResponse = mockPackageServiceRule.getMIDFlightsResponse()
        Db.setPackageSelectedOutboundFlight(midFlightsResponse.getFlightLegs().first())
        Db.setPackageResponse(midFlightsResponse)

        val packageParams = Db.sharedInstance.packageParams
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.updateHotelParams(packageParams)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.updateHotelParams(packageParams)

        val performMIDCreateTripObserver = TestObserver<Unit>()
        val selectedFlightObservable = TestObserver<PackageProductSearchType>()
        val showBundleTotalObserver = TestObserver<Boolean>()
        val flightParamsObserver = TestObserver<PackageSearchParams>()
        val flightObserver = TestObserver<FlightLeg>()
        packagePresenter.bundlePresenter.bundleWidget.viewModel.flightParamsObservable.subscribe(flightParamsObserver)
        packagePresenter.bundlePresenter.viewModel.performMIDCreateTripSubject.subscribe(performMIDCreateTripObserver)
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.subscribe(selectedFlightObservable)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe(showBundleTotalObserver)
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.flight.subscribe(flightObserver)

        packagePresenter.flightOutboundSelectedSuccessfully()

        flightParamsObserver.assertValueCount(1)
        flightParamsObserver.assertValue(packageParams)
        performMIDCreateTripObserver.assertValueCount(0)
        selectedFlightObservable.assertValue(PackageProductSearchType.MultiItemOutboundFlights)
        showBundleTotalObserver.assertValue(true)
        flightObserver.assertValue(midFlightsResponse.getFlightLegs().first())

        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.putExtra(Constants.PACKAGE_LOAD_OUTBOUND_FLIGHT, true)
        intent.putExtra(Constants.REQUEST, Constants.PACKAGE_FLIGHT_OUTBOUND_REQUEST_CODE)
        assertTrue(intent.filterEquals(packagePresenter.backStack.peek() as Intent))
    }

    @Test
    fun testFlightInboundSelectedSuccessfully() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hotelRoomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        hotelResponse.setCurrentOfferPrice(hotelResponse.getHotels().first().packageOfferModel.price)

        Db.setPackageSelectedHotel(hotelResponse.getHotels().first(), hotelRoomResponse.getBundleRoomResponse().first())
        val midFlightsResponse = mockPackageServiceRule.getMIDFlightsResponse()
        Db.setPackageSelectedOutboundFlight(midFlightsResponse.getFlightLegs().first())
        Db.setPackageFlightBundle(midFlightsResponse.getFlightLegs()[0], midFlightsResponse.getFlightLegs()[1])
        Db.setPackageResponse(midFlightsResponse)

        val packageParams = Db.sharedInstance.packageParams
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.updateHotelParams(packageParams)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.updateHotelParams(packageParams)

        val performMIDCreateTripObserver = TestObserver<Unit>()
        val selectedFlightObservable = TestObserver<PackageProductSearchType>()
        val showBundleTotalObserver = TestObserver<Boolean>()
        val flightObserver = TestObserver<FlightLeg>()

        packagePresenter.bundlePresenter.viewModel.performMIDCreateTripSubject.subscribe(performMIDCreateTripObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.subscribe(selectedFlightObservable)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe(showBundleTotalObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.flight.subscribe(flightObserver)

        packagePresenter.flightInboundSelectedSuccessfully()

        performMIDCreateTripObserver.assertValueCount(1)
        selectedFlightObservable.assertValue(PackageProductSearchType.MultiItemInboundFlights)
        showBundleTotalObserver.assertValue(false)
        flightObserver.assertValue(midFlightsResponse.getFlightLegs()[1])
        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.visibility)

        assertEquals(packagePresenter.bundlePresenter, packagePresenter.backStack.pop())
        assertEquals(packagePresenter.bundlePresenter, packagePresenter.backStack.pop())
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.putExtra(Constants.PACKAGE_LOAD_INBOUND_FLIGHT, true)
        intent.putExtra(Constants.REQUEST, Constants.PACKAGE_FLIGHT_RETURN_REQUEST_CODE)
        assertTrue(intent.filterEquals(packagePresenter.backStack.peek() as Intent))
    }

    @Test
    fun testChangeFlightInboundSelectedSuccessfully() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hotelRoomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        hotelResponse.setCurrentOfferPrice(hotelResponse.getHotels().first().packageOfferModel.price)

        Db.setPackageSelectedHotel(hotelResponse.getHotels().first(), hotelRoomResponse.getBundleRoomResponse().first())
        val midFlightsResponse = mockPackageServiceRule.getMIDFlightsResponse()
        Db.setPackageSelectedOutboundFlight(midFlightsResponse.getFlightLegs().first())
        Db.setPackageFlightBundle(midFlightsResponse.getFlightLegs()[0], midFlightsResponse.getFlightLegs()[1])
        Db.setPackageResponse(midFlightsResponse)

        val packageParams = Db.sharedInstance.packageParams
        packageParams.pageType = Constants.PACKAGE_CHANGE_FLIGHT
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.updateHotelParams(packageParams)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.updateHotelParams(packageParams)

        val performMIDCreateTripObserver = TestObserver<Unit>()
        val selectedFlightObservable = TestObserver<PackageProductSearchType>()
        val showBundleTotalObserver = TestObserver<Boolean>()
        val flightObserver = TestObserver<FlightLeg>()

        packagePresenter.bundlePresenter.viewModel.performMIDCreateTripSubject.subscribe(performMIDCreateTripObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.subscribe(selectedFlightObservable)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.showBundleTotalObservable.subscribe(showBundleTotalObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.flight.subscribe(flightObserver)

        packagePresenter.flightInboundSelectedSuccessfully()

        performMIDCreateTripObserver.assertValueCount(1)
        selectedFlightObservable.assertValue(PackageProductSearchType.MultiItemInboundFlights)
        showBundleTotalObserver.assertValue(false)
        flightObserver.assertValue(midFlightsResponse.getFlightLegs()[1])
        assertEquals(View.VISIBLE, packagePresenter.bundlePresenter.visibility)

        assertEquals(packagePresenter.bundlePresenter, packagePresenter.backStack.peek())
    }

    @Test
    fun testHandleActivityCanceledWhenChangeSearch() {
        val packageParams = PackageTestUtil.getMIDPackageSearchParams()
        packageParams.pageType = Constants.PACKAGE_CHANGE_HOTEL
        Db.setPackageParams(packageParams)

        packagePresenter.handleActivityCanceled()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun testHandleActivityCanceledWhenBackToSelectedOutboundIntent() {
        val packageParams = PackageTestUtil.getMIDPackageSearchParams()
        Db.setPackageParams(packageParams)

        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.putExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM, true)
        intent.putExtra(Constants.REQUEST, Constants.HOTEL_REQUEST_CODE)

        val packagePresenterSpy = Mockito.spy(packagePresenter)
        val mockBundlePresenter = Mockito.spy(packagePresenterSpy.bundlePresenter)
        Mockito.`when`(packagePresenterSpy.bundlePresenter).thenReturn(mockBundlePresenter)
        Mockito.doNothing().`when`(mockBundlePresenter).resetToLoadedOutboundFlights()

        packagePresenterSpy.backStack.add(intent)

        packagePresenterSpy.handleActivityCanceled()
        Mockito.verify(mockBundlePresenter, Mockito.times(1)).resetToLoadedOutboundFlights()
    }

    @Test
    fun testHandleActivityCanceledWhenBackToSelectedHotelIntent() {
        val packageParams = PackageTestUtil.getMIDPackageSearchParams()
        Db.setPackageParams(packageParams)

        val packagePresenterSpy = Mockito.spy(packagePresenter)
        val mockBundlePresenter = Mockito.spy(packagePresenterSpy.bundlePresenter)
        Mockito.`when`(packagePresenterSpy.bundlePresenter).thenReturn(mockBundlePresenter)
        Mockito.doNothing().`when`(mockBundlePresenter).resetToLoadedHotels()

        packagePresenterSpy.backStack.add(packagePresenterSpy.bundlePresenter)

        packagePresenterSpy.handleActivityCanceled()
        Mockito.verify(mockBundlePresenter, Mockito.times(1)).resetToLoadedHotels()
    }

    @Test
    fun testHandleActivityCanceledWhenCrossSell() {
        Db.setFlightSearchParams(FlightTestUtil.getFlightSearchParams(isRoundTrip = true))
        setup(true)
        packagePresenter.handleActivityCanceled()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun testBackWhenChangeSearch() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        val hotelRoomResponse = mockPackageServiceRule.getMIDRoomsResponse()
        hotelResponse.setCurrentOfferPrice(hotelResponse.getHotels().first().packageOfferModel.price)

        Db.setPackageSelectedHotel(hotelResponse.getHotels().first(), hotelRoomResponse.getBundleRoomResponse().first())
        val midFlightsResponse = mockPackageServiceRule.getMIDFlightsResponse()
        val outboundLeg1 = midFlightsResponse.getFlightLegs()[0]
        val outboundLeg2 = midFlightsResponse.getFlightLegs()[2]

        Db.setPackageSelectedOutboundFlight(outboundLeg2)
        Db.sharedInstance.packageParams.currentFlights = arrayOf(outboundLeg2.legId, "")
        Db.setPackageFlightBundle(outboundLeg1, midFlightsResponse.getFlightLegs()[1])
        Db.setPackageResponse(midFlightsResponse)

        val flightTestObserver = TestObserver<FlightLeg>()
        val cancelSearchTestObserver = TestObserver<Unit>()
        val performMIDCreateTripTestObserver = TestObserver<Unit>()
        val selectedHotelTestObserver = TestObserver<Unit>()
        val selectedOutboundFlightTestObserver = TestObserver<PackageProductSearchType>()
        val selectedInboundFlightTestObserver = TestObserver<PackageProductSearchType>()
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.flight.subscribe(flightTestObserver)
        packagePresenter.bundlePresenter.bundleWidget.viewModel.cancelSearchObservable.subscribe(cancelSearchTestObserver)
        packagePresenter.bundlePresenter.viewModel.performMIDCreateTripSubject.subscribe(performMIDCreateTripTestObserver)
        packagePresenter.bundlePresenter.bundleWidget.bundleHotelWidget.viewModel.selectedHotelObservable.subscribe(selectedHotelTestObserver)
        packagePresenter.bundlePresenter.bundleWidget.outboundFlightWidget.viewModel.selectedFlightObservable.subscribe(selectedOutboundFlightTestObserver)
        packagePresenter.bundlePresenter.bundleWidget.inboundFlightWidget.viewModel.selectedFlightObservable.subscribe(selectedInboundFlightTestObserver)

        packagePresenter.backStack.add(packagePresenter.bundlePresenter)
        packagePresenter.changedOutboundFlight = true
        Db.sharedInstance.packageParams.pageType = Constants.PACKAGE_CHANGE_FLIGHT

        assertEquals(outboundLeg2.legId, Db.sharedInstance.packageParams.currentFlights[0])
        assertEquals(outboundLeg2, Db.sharedInstance.packageSelectedOutboundFlight)

        packagePresenter.back()

        assertFalse(packagePresenter.changedOutboundFlight)
        assertEquals(outboundLeg1, Db.sharedInstance.packageSelectedOutboundFlight)
        assertEquals(outboundLeg1.legId, Db.sharedInstance.packageParams.currentFlights[0])
        flightTestObserver.assertValue(outboundLeg1)
        cancelSearchTestObserver.assertValueCount(1)
        performMIDCreateTripTestObserver.assertValueCount(1)
        selectedHotelTestObserver.assertValueCount(1)
        selectedOutboundFlightTestObserver.assertValue(PackageProductSearchType.MultiItemOutboundFlights)
        selectedInboundFlightTestObserver.assertValue(PackageProductSearchType.MultiItemInboundFlights)
    }

    @Test
    fun testBackWhenWebViewIsOpen() {
        Db.setPackageParams(PackageTestUtil.getMIDPackageSearchParams())
        packagePresenter.bundlePresenter.webCheckoutView.visibility = View.VISIBLE

        val packagePresenterSpy = Mockito.spy(packagePresenter)
        val bundlePresenterSpy = Mockito.spy(packagePresenterSpy.bundlePresenter)
        val webCheckoutViewSpy = Mockito.spy(bundlePresenterSpy.webCheckoutView)
        Mockito.`when`(packagePresenterSpy.bundlePresenter).thenReturn(bundlePresenterSpy)
        Mockito.`when`(bundlePresenterSpy.webCheckoutView).thenReturn(webCheckoutViewSpy)
        Mockito.doNothing().`when`(webCheckoutViewSpy).back()

        packagePresenterSpy.back()
        Mockito.verify(webCheckoutViewSpy, Mockito.times(1)).back()
    }

    @Test
    fun testBack() {
        assertEquals(1, packagePresenter.backStack.size)
        packagePresenter.back()
        assertEquals(0, packagePresenter.backStack.size)
    }

    private fun assertHandleHotelFilterAPIError(filterSearchErrorString: String?,
                                                expectedApiErrorCode: PackageApiError.Code,
                                                expectedErrorCode: String,
                                                expectedApiCall: String) {
        Db.setUnfilteredResponse(PackageTestUtil.getMockMIDResponse())
        Db.setPackageResponse(null)

        val testObserver = TestObserver<Pair<PackageApiError.Code, ApiCallFailing>>()
        packagePresenter.filterSearchErrorObservable.subscribe(testObserver)

        packagePresenter.handleHotelFilterAPIError(filterSearchErrorString)

        val (apiErrorCode, apiCallFailing) = testObserver.values().first()
        assertEquals(expectedApiErrorCode, apiErrorCode)
        assertEquals(expectedErrorCode, apiCallFailing.errorCode)
        assertEquals(expectedApiCall, apiCallFailing.apiCall)
        assertNotNull(Db.getPackageResponse())
    }

    private fun assertHandleHotelOffersAndInfositeAPIError(errorKey: String?,
                                                           errorString: String?,
                                                           isErrorFromInfositeCall: Boolean,
                                                           isChangeSearch: Boolean,
                                                           expectedApiErrorCode: ApiError.Code,
                                                           expectedErrorCode: String,
                                                           expectedApiCall: String) {
        val searchParams = PackageTestUtil.getMIDPackageSearchParams()
        searchParams.pageType = if (isChangeSearch) Constants.PACKAGE_CHANGE_HOTEL else null
        Db.setPackageParams(searchParams)

        val testObserver = TestObserver<Pair<ApiError.Code, ApiCallFailing>>()
        packagePresenter.hotelOffersErrorObservable.subscribe(testObserver)

        packagePresenter.handleHotelOffersAndInfositeAPIError(errorKey, errorString, isErrorFromInfositeCall)

        val (apiErrorCode, apiCallFailing) = testObserver.values().first()
        assertEquals(expectedApiErrorCode, apiErrorCode)
        assertEquals(expectedErrorCode, apiCallFailing.errorCode)
        assertEquals(expectedApiCall, apiCallFailing.apiCall)
    }
}
