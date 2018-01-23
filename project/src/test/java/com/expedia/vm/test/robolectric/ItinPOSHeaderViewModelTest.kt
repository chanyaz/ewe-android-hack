package com.expedia.vm.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.model.PointOfSaleStateModel
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.vm.ItinPOSHeaderViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver

@RunWith(RobolectricRunner::class)
class ItinPOSHeaderViewModelTest {

    val context = RuntimeEnvironment.application

    private lateinit var sut: ItinPOSHeaderViewModel
    private lateinit var pointOfSaleModel: PointOfSaleStateModel

    @Before
    fun setup() {
        setupCurrentPointOfSale()
        pointOfSaleModel = PointOfSaleStateModel()
        sut = ItinPOSHeaderViewModel(pointOfSaleModel)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun updateSelectedPOS() {
        val textTestSubscriber = TestObserver<String>()
        val imageTestSubscriber = TestObserver<Int>()
        sut.posTextViewSubject.subscribe(textTestSubscriber)
        sut.posImageViewSubject.subscribe(imageTestSubscriber)

        firePointOfSaleChange()

        // fired twice: once on creation and the second triggered from new POS
        textTestSubscriber.assertValues("USA", "USA")
        imageTestSubscriber.assertValues(R.drawable.ic_flag_us_icon, R.drawable.ic_flag_us_icon)
    }

    private fun firePointOfSaleChange() {
        pointOfSaleModel.pointOfSaleChangedSubject.onNext(PointOfSale.getPointOfSale())
    }

    private fun setupCurrentPointOfSale() {
        PointOfSaleTestConfiguration.configurePOS(context, "ExpediaSharedData/ExpediaPointOfSaleConfig.json", Integer.toString(PointOfSaleId.UNITED_STATES.id), false)
    }
}
