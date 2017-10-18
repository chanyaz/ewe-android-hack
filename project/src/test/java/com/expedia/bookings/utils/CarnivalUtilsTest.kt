package com.expedia.bookings.utils

import com.carnival.sdk.AttributeMap
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class CarnivalUtilsTest : CarnivalUtils() {

    private lateinit var attributesToSend : AttributeMap
    private lateinit var eventNameToLog: String
    private val context = RuntimeEnvironment.application

    @Before
    fun setup() {
        SettingUtils.save(context, R.string.preference_new_carnival_notifications, true)
        initialize(context)
        attributesToSend = AttributeMap()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelSearch() {
        reset()

        val hotelParams = HotelSearchTrackingData()
        hotelParams.city = "Las Vegas"
        hotelParams.stateProvinceCode = "NV"
        hotelParams.numberOfAdults = 1
        hotelParams.checkInDate = LocalDate.now()
        hotelParams.duration = 3

        this.trackHotelSearch(hotelParams)

        assertEquals(eventNameToLog, "search_hotel")
        assertEquals(attributesToSend.get("search_hotel_destination"),"Las Vegas, NV")
        assertEquals(attributesToSend.get("search_hotel_number_of_adults"), 1)
        assertEquals(attributesToSend.get("search_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("search_hotel_length_of_stay"), 3)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackHotelInfoSite() {
        reset()

        var hotelOfferResponse = HotelOffersResponse()
        hotelOfferResponse.hotelName = "Twin Lotus Koh Lanta by Burasari"
        hotelOfferResponse.hotelCity = "Krabi, Thailand"
        var searchParams = HotelSearchParams(SuggestionV4(), LocalDate.now(), LocalDate.now().plusDays(3), 2, listOf(0), false, false, null, null)

        this.trackHotelInfoSite(hotelOfferResponse, searchParams)

        assertEquals(eventNameToLog, "product_view_hotel")
        assertEquals(attributesToSend.get("product_view_hotel_destination"), "Krabi, Thailand")
        assertEquals(attributesToSend.get("product_view_hotel_hotel_name"), "Twin Lotus Koh Lanta by Burasari")
        assertEquals(attributesToSend.get("product_view_hotel_number_of_adults"), 2)
        assertEquals(attributesToSend.get("product_view_hotel_check-in_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("product_view_hotel_length_of_stay"), 3)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackLxConfirmation() {
        reset()

        this.trackLxConfirmation("Disney World", "2017-10-18 08:00:00")

        assertEquals(eventNameToLog, "confirmation_lx")
        assertEquals(attributesToSend.get("confirmation_lx_activity_name"), "Disney World")
        assertEquals(attributesToSend.get("confirmation_lx_date_of_activity"), DateUtils.yyyyMMddHHmmssToLocalDate("2017-10-18 08:00:00").toDate())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackPackagesConfirmation() {
        reset()

        val v4 = SuggestionV4()
        v4.regionNames = SuggestionV4.RegionNames()
        v4.regionNames.fullName = "New York"
        val packageParams = PackageSearchParams(SuggestionV4(),v4, LocalDate.now(),LocalDate.now().plusDays(3),1, listOf(),false)

        this.trackPackagesConfirmation(packageParams)

        assertEquals(eventNameToLog, "confirmation_pkg")
        assertEquals(attributesToSend.get("confirmation_pkg_destination"), "New York")
        assertEquals(attributesToSend.get("confirmation_pkg_departure_date"), LocalDate.now().toDate())
        assertEquals(attributesToSend.get("confirmation_pkg_length_of_stay"), 3)
    }

    private fun reset() {
        eventNameToLog = ""
        attributesToSend.clear()
    }

    override fun setAttributes(attributes: AttributeMap, eventName: String) {
        //Don't actually send anything up to carnival
        eventNameToLog = eventName
        attributesToSend = attributes
    }
}
