package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.widget.itin.ItinPOSHeader
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun currentPOSIsDisplayed() {
        pos = PointOfSale.getPointOfSale()

        val currentPOS = pos.threeLetterCountryCode
        val currentPOSUrl = header.posText.text

        assertEquals("USA", currentPOS)
        assertEquals("Expedia.com", currentPOSUrl)
        assertEquals("Country United States", header.spinner.selectedView.contentDescription)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun changePOSDisplayedOnTripsHeader() {
        val testSubscriber = TestSubscriber<Unit>()
        header.onPrivateDataClearedSubject.subscribe(testSubscriber)
        header.position = 1
        header.onPrivateDataCleared()
        pos = PointOfSale.getPointOfSale()
        
        testSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        val currentPOS = pos.threeLetterCountryCode
        val currentPOSUrl = header.posText.text
        header.setCurrentPOS()

        assertEquals("AUS", currentPOS)
        assertEquals("Expedia.com.au", currentPOSUrl)
        assertEquals("Country Australia", header.spinner.selectedView.contentDescription)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun posNotUpdatedOnDialogCancel() {
        header.position = 1
        header.onDialogCancel()
        pos = PointOfSale.getPointOfSale()

        val currentPOSUrl = header.posText.text

        assertEquals("USA", pos.threeLetterCountryCode)
        assertEquals("Expedia.com", currentPOSUrl)
    }

    private fun setCurrentPOS() {
        PointOfSaleTestConfiguration.configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(PointOfSaleId.UNITED_STATES.getId()), false)
        header.setCurrentPOS()
    }
}
