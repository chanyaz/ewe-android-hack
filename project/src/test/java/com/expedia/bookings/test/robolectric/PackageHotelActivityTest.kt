package com.expedia.bookings.test.robolectric

import android.content.Intent
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemError
import com.expedia.bookings.data.multiitem.ProductType
import com.expedia.bookings.test.robolectric.RoboTestHelper.getContext
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.Ui
import com.expedia.ui.PackageHotelActivity
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

    @Test
    fun testMidEnabledDeeplinkedHotelResetsDbPackageResponse() {
        setupPackageDb(baseMidResponse, midResponseWithError)
        givenPackageHotelActivity(enableMidTest = true)

        assertNotEquals(midResponseWithError.errors?.size, (Db.getPackageResponse() as MultiItemApiSearchResponse).errors?.size)
        assertEquals(baseMidResponse.errors?.size, (Db.getPackageResponse() as MultiItemApiSearchResponse).errors?.size)
    }

    @Test
    fun testMidDisabledDeeplinkedHotelDoesNotResetDbPackageResponse() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppPackagesMidApi, AbacusVariant.CONTROL.value)
        setupPackageDb(baseMidResponse, midResponseWithError)
        givenPackageHotelActivity(enableMidTest = false)

        assertEquals(midResponseWithError.errors?.size, (Db.getPackageResponse() as MultiItemApiSearchResponse).errors?.size)
    }

    private fun givenPackageHotelActivity(enableMidTest: Boolean) {
        if (enableMidTest) {
            AbacusTestUtils.bucketTestAndEnableRemoteFeature(getContext(), AbacusUtils.EBAndroidAppPackagesMidApi)
        }
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
