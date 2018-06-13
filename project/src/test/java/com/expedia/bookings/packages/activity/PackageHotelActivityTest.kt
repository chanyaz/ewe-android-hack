package com.expedia.bookings.packages.activity

import android.content.Intent
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.multiitem.ProductType
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Ui
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
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

    private fun givenPackageHotelActivity() {
        Ui.getApplication(context).defaultPackageComponents()
        val intent = Intent(context, PackageHotelActivity::class.java)
        intent.putExtra(Codes.TAG_EXTERNAL_SEARCH_PARAMS, true)
        Robolectric.buildActivity(PackageHotelActivity::class.java, intent).create().get()
    }

    private fun setupPackageDb(baseMidResponse: BundleSearchResponse, midResponseWithError: BundleSearchResponse) {
        Db.setPackageResponse(baseMidResponse)
        PackageResponseUtils.recentPackageHotelsResponse = baseMidResponse
        Db.setPackageResponse(midResponseWithError)
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams())
        PackageTestUtil.setDbPackageSelectedHotel()
    }
}
