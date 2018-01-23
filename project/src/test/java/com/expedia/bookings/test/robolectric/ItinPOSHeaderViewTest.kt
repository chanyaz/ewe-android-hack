package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.itin.ItinPOSHeader
import com.expedia.vm.ItinPOSHeaderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class ItinPOSHeaderViewTest {

    lateinit var context: Context
    lateinit var itinPosHeader: ItinPOSHeader

    private lateinit var pointOfSaleStateModel: PointOfSaleStateModel

    @Before
    fun setup() {
        context = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(context).defaultTripComponents()

        createSystemUnderTest()
        setPointOfSale(PointOfSaleId.UNITED_STATES)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun currentPOSIsDisplayed() {
        val pos = PointOfSale.getPointOfSale()

        assertEquals("USA", pos.threeLetterCountryCode)
        assertEquals("Expedia.com", itinPosHeader.pointOfSaleUrlTextView.text)
        assertFlagDrawable(R.drawable.ic_flag_us_icon)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun correctPointOfSaleOnChange() {
        var pos = PointOfSale.getPointOfSale()

        assertEquals("USA", pos.threeLetterCountryCode)
        assertEquals("Expedia.com", itinPosHeader.pointOfSaleUrlTextView.text)
        assertFlagDrawable(R.drawable.ic_flag_us_icon)

        setPointOfSale(PointOfSaleId.AUSTRALIA)
        pos = PointOfSale.getPointOfSale()

        assertEquals("AUS", pos.threeLetterCountryCode)
        assertEquals("Expedia.com.au", itinPosHeader.pointOfSaleUrlTextView.text)
        assertFlagDrawable(R.drawable.ic_flag_au_icon)
    }

    private fun assertFlagDrawable(expectedDrawable: Int) {
        val drawableShadow = shadowOf(itinPosHeader.imageView.drawable)
        assertEquals(expectedDrawable, drawableShadow.createdFromResId)
    }

    private fun createSystemUnderTest() {
        itinPosHeader = ItinPOSHeader(context, null)
        pointOfSaleStateModel = PointOfSaleStateModel()
        itinPosHeader.itinPOSHeaderViewModel = ItinPOSHeaderViewModel(pointOfSaleStateModel)
        itinPosHeader.onAttachedToWindow()
    }

    private fun setPointOfSale(posId: PointOfSaleId) {
        PointOfSaleTestConfiguration.configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(posId.id), false)
        pointOfSaleStateModel.pointOfSaleChangedSubject.onNext(PointOfSale.getPointOfSale())
    }
}
