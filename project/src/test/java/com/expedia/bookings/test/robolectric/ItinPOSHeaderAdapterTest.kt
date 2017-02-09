package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.widget.itin.ItinPOSHeader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinPOSHeaderAdapterTest {

    val context = RuntimeEnvironment.application
    lateinit var header: ItinPOSHeader
    lateinit var pos: PointOfSale

    @Before
    fun setup() {
        createSystemUnderTest()
        setCurrentPOS()
    }

    fun createSystemUnderTest() {
        header = ItinPOSHeader(context, null)
    }

    @Test
    fun currentPOSIsDisplayed() {
        pos = PointOfSale.getPointOfSale()

        val currentPOS = pos.threeLetterCountryCode
        val currentPOSUrl = header.posText.text

        assertEquals("USA", currentPOS)
        assertEquals("expedia.com", currentPOSUrl)
        assertEquals("Country United States", header.spinner.selectedView.contentDescription)
    }

    @Test
    fun changePOSDisplayedOnTripsHeader() {
        header.position = 1
        header.onPrivateDataCleared()
        pos = PointOfSale.getPointOfSale()

        val currentPOS = pos.threeLetterCountryCode
        val currentPOSUrl = header.posText.text
        header.setCurrentPOS()

        assertEquals("AUS", currentPOS)
        assertEquals("expedia.com.au", currentPOSUrl)
        assertEquals("Country Australia", header.spinner.selectedView.contentDescription)
    }

    @Test
    fun posNotUpdatedOnDialogCancel() {
        header.position = 1
        header.onDialogCancel()
        pos = PointOfSale.getPointOfSale()

        val currentPOSUrl = header.posText.text

        assertEquals("USA", pos.threeLetterCountryCode)
        assertEquals("expedia.com", currentPOSUrl)
    }

    private fun setCurrentPOS() {
        PointOfSaleTestConfiguration.configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(PointOfSaleId.UNITED_STATES.getId()), false)
        header.setCurrentPOS()
    }
}
