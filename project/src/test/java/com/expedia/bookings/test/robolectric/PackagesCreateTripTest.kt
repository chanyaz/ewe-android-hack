package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.ui.PackageActivity
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class PackagesCreateTripTest {

    var activity: PackageActivity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(PackageActivity::class.java).create().get()
    }

    @Test
    fun testCreateTripFiredWhenMIDAPION() {
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)

        val testSubscriber = TestSubscriber<MultiItemCreateTripParams>()
        activity.getCreateTripViewModel().performMultiItemCreateTrip.subscribe(testSubscriber)

        val params = getDummySearchParams()
        Db.setPackageParams(params)
        activity.packageCreateTrip()

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    @Test
    fun testMultiItemCreateTripParamsFromSearchParams() {
        val searchParams = getDummySearchParams()
        val fromPackageSearchParams = MultiItemCreateTripParams.fromPackageSearchParams(searchParams)

        assertEquals("latestSelectedFlightPID", fromPackageSearchParams.flightPIID)
        assertEquals(1, fromPackageSearchParams.adults)
        assertEquals(LocalDate.now(), fromPackageSearchParams.startDate)
        assertEquals(LocalDate.now().plusDays(2), fromPackageSearchParams.endDate)
        assertEquals("hotelID", fromPackageSearchParams.hotelID)
        assertEquals("roomTypeCode", fromPackageSearchParams.roomTypeCode)
        assertEquals("ratePlanCode", fromPackageSearchParams.ratePlanCode)
        assertEquals("inventoryType", fromPackageSearchParams.inventoryType)
    }

    @Test
    fun testMultiItemCreateTripFiredWhenMIDAPIOFF() {
        val testSubscriber = TestSubscriber<Unit>()
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

        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS)
        testSubscriber.assertValueCount(1)
    }

    private fun getDummySearchParams(): PackageSearchParams {
        val originDestSuggestions = getOriginDestSuggestions()
        val date = LocalDate.now()
        val params = PackageSearchParams.Builder(0, 0).destination(originDestSuggestions.first).origin(originDestSuggestions.second)
                .adults(1).children(listOf(12, 14)).startDate(date).endDate(date.plusDays(2)).build() as PackageSearchParams
        params.hotelId = "hotelID"
        params.latestSelectedFlightPIID = "latestSelectedFlightPID"
        params.packagePIID = "packagePIID"
        params.ratePlanCode = "ratePlanCode"
        params.roomTypeCode = "roomTypeCode"
        params.inventoryType = "inventoryType"
        params.latestSelectedProductOfferPrice = PackageOfferModel.PackagePrice()
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


}