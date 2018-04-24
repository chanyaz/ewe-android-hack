package com.expedia.bookings.packages.activity

import android.content.Intent
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.multiitem.ProductType
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(RobolectricRunner::class)
class PackageHotelActivityTest {

    val context = RuntimeEnvironment.application
    val baseMidResponse = PackageTestUtil.getMockMIDResponse(offers = emptyList(),
            hotels = mapOf("1" to PackageTestUtil.dummyMidHotelRoomOffer()))
    val midResponseWithError = PackageTestUtil.getMockMIDResponse(offers = emptyList(),
            hotels = mapOf("1" to PackageTestUtil.dummyMidHotelRoomOffer()),
            errors = arrayListOf(MultiItemError("description", "MIS_FLIGHT_PRODUCT_NOT_FOUND", ProductType.Bundle)))

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    @Test
    fun testMidEnabledDeeplinkedHotelResetsDbPackageResponse() {
        setupPackageDb(baseMidResponse, midResponseWithError)
        givenPackageHotelActivity()
        assertNotEquals(midResponseWithError.errors?.size, (Db.getPackageResponse() as MultiItemApiSearchResponse).errors?.size)
        assertEquals(baseMidResponse.errors?.size, (Db.getPackageResponse() as MultiItemApiSearchResponse).errors?.size)
    }

    @Test
    fun testHotelResponseLoadedFromResponseStaticFile() {
        val expectedPackageSearchResponse = mockPackageServiceRule.getMIDHotelResponse()
        val latch = CountDownLatch(1)
        PackageResponseUtils.savePackageResponse(context, expectedPackageSearchResponse, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE, { _ ->
            latch.countDown()
        })
        latch.await(2, TimeUnit.SECONDS)
        fireActivityIntent()
        assertEquals(expectedPackageSearchResponse, Db.getPackageResponse())
    }

    private fun fireActivityIntent() {
        val intent = Intent(context, PackageFlightActivity::class.java)
        intent.putExtra(Constants.PACKAGE_LOAD_HOTEL_ROOM, true)
        Robolectric.buildActivity(PackageHotelActivity::class.java, intent).create().get()
    }

    private fun givenPackageHotelActivity() {
        Ui.getApplication(context).defaultPackageComponents()
        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
        Robolectric.buildActivity(PackageHotelActivity::class.java, intent).create().get()
    }

    private fun setupPackageDb(baseMidResponse: BundleSearchResponse, midResponseWithError: BundleSearchResponse) {
        Db.setPackageResponse(baseMidResponse)
        PackageResponseUtils.savePackageResponse(context, baseMidResponse, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE)
        Db.setPackageResponse(midResponseWithError)
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams())
        PackageTestUtil.setDbPackageSelectedHotel()
    }
}
