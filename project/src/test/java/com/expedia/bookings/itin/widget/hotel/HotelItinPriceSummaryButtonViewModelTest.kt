package com.expedia.bookings.itin.widget.hotel

import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.HasHotel
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.utils.IWebViewLauncher
import com.expedia.bookings.itin.utils.StringSource
import com.mobiata.mocke3.mockObject
import org.junit.Test
import kotlin.test.assertEquals

class HotelItinPriceSummaryButtonViewModelTest {

    @Test
    fun testNoPriceDetails() {
        val scope = TestHotelDetailsScopeNoPriceDetails()
        val viewModel = HotelItinPriceSummaryButtonViewModel(scope)

        assertEquals(R.drawable.ic_itin_credit_card_icon, viewModel.iconImage)

        assertEquals(R.string.itin_hotel_details_price_summary_rewards_heading, scope.mockStrings.lastSeenFetchArgs)
        assertEquals("someString", viewModel.headingText)

        assertEquals(null, viewModel.subheadingText)
        assertEquals(null, scope.mockStrings.lastSeenFetchWithMapArgs)

        viewModel.cardClickListener.invoke()
        assertEquals("1103274148635", scope.mockWebViewLauncher.lastSeenTripId)
        assertEquals("https://www.expedia.com/trips/1103274148635", scope.mockWebViewLauncher.lastSeenURL)
        assertEquals(R.string.itin_hotel_details_price_summary_heading, scope.mockWebViewLauncher.lastSeenTitle)
    }

    @Test
    fun testHappyPath() {
        val scope = TestHotelDetailsScopeHappy()
        val viewModel = HotelItinPriceSummaryButtonViewModel(scope)

        assertEquals(R.drawable.ic_itin_credit_card_icon, viewModel.iconImage)

        assertEquals(R.string.itin_hotel_details_price_summary_rewards_heading, scope.mockStrings.lastSeenFetchArgs)
        assertEquals("someString", viewModel.headingText)

        assertEquals("somePhraseString", viewModel.subheadingText)
        assertEquals(Pair(R.string.itin_hotel_details_price_summary_pay_later_TEMPLATE, mapOf("amount" to ItinMocker.hotelDetailsHappy.firstHotel()?.totalPriceDetails?.totalFormatted!!)), scope.mockStrings.lastSeenFetchWithMapArgs)

        viewModel.cardClickListener.invoke()
        assertEquals("7280999576135", scope.mockWebViewLauncher.lastSeenTripId)
        assertEquals("https://www.expedia.com/trips/7280999576135", scope.mockWebViewLauncher.lastSeenURL)
        assertEquals(R.string.itin_hotel_details_price_summary_heading, scope.mockWebViewLauncher.lastSeenTitle)
    }
}

class TestHotelDetailsScopeNoPriceDetails : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher {
    override val itin: Itin = ItinMocker.hotelDetailsNoPriceDetails
    override val hotel: ItinHotel = ItinMocker.hotelDetailsNoPriceDetails.firstHotel()!!
    val mockStrings = MockStringProvider()
    override val strings: StringSource = mockStrings
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
}

class TestHotelDetailsScopeHappy : HasItin, HasHotel, HasStringProvider, HasWebViewLauncher {
    override val itin: Itin = ItinMocker.hotelDetailsHappy
    override val hotel: ItinHotel = ItinMocker.hotelDetailsHappy.firstHotel()!!
    val mockStrings = MockStringProvider()
    override val strings: StringSource = mockStrings
    val mockWebViewLauncher = MockWebViewLauncher()
    override val webViewLauncher: IWebViewLauncher = mockWebViewLauncher
}

object ItinMocker {
    val hotelDetailsNoPriceDetails = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details.json")?.itin!!
    val hotelDetailsHappy = mockObject(ItinDetailsResponse::class.java, "api/trips/hotel_trip_details_for_mocker.json")?.itin!!
}

class MockWebViewLauncher : IWebViewLauncher {
    var lastSeenTitle: Int? = null
    var lastSeenURL: String? = null
    var lastSeenTripId: String? = null
    override fun launchWebViewActivity(title: Int, url: String, anchor: String?, tripId: String) {
        lastSeenTitle = title
        lastSeenURL = url
        lastSeenTripId = tripId
    }
}

class MockStringProvider : StringSource {
    var lastSeenFetchArgs: Int? = null
    override fun fetch(stringResource: Int): String {
        lastSeenFetchArgs = stringResource
        return "someString"
    }

    var lastSeenFetchWithMapArgs: Pair<Int, Map<String, String>>? = null
    override fun fetch(stringResource: Int, map: Map<String, String>): String {
        lastSeenFetchWithMapArgs = Pair(stringResource, map)
        return "somePhraseString"
    }
}
